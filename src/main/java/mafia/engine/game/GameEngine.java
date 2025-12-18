package mafia.engine.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.ability.AbilityEngine;
import mafia.engine.config.GameConfiguration;
import mafia.engine.game.event.NightEvent;
import mafia.engine.game.phase.GameResultPhaseContext;
import mafia.engine.game.phase.MorningPhaseContext;
import mafia.engine.game.phase.NightPhaseContext;
import mafia.engine.game.phase.PhaseChannel;
import mafia.engine.game.phase.VotingPhaseContext;
import mafia.engine.game.phase.VotingResultPhaseContext;
import mafia.engine.game.vote.PlayerVote;
import mafia.engine.game.vote.VoteResult;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerEngine;
import mafia.engine.player.PlayerState;
import mafia.engine.presets.Preset;
import mafia.engine.role.Role;
import mafia.engine.rule.DistributionEngine;
import mafia.engine.rule.RuleEngine;
import mafia.engine.util.StreamUtils;

@Accessors(fluent = true)
public class GameEngine {

    @NonNull @Getter
    private volatile GameState gameState = GameState.INITIALIZING;

    @NonNull @Getter @Setter
    private List<Player> players;
    
    @NonNull
    private List<Role> roles;

    @NonNull @Getter
    private GameConfiguration configuration;

    @NonNull @Getter @Setter
    private Preset preset;
    
    @Getter
    private final Map<String, Object> gameStatus = new HashMap<>();

    private PhaseChannel<NightPhaseContext> nightPhaseChannel;
    private PhaseChannel<MorningPhaseContext> morningPhaseChannel;
    private PhaseChannel<VotingPhaseContext> votingPhaseChannel;
    private PhaseChannel<VotingResultPhaseContext> votingResultPhaseChannel;
    private PhaseChannel<GameResultPhaseContext> gameResultPhaseChannel;

    private PlayerEngine playerEngine = new PlayerEngine();
    private RuleEngine ruleEngine = new RuleEngine();
    private AbilityEngine abilityEngine = new AbilityEngine();
    private DistributionEngine distributionEngine = new DistributionEngine();
    
    public GameEngine(
        @NonNull List<Player> players,
        @NonNull List<Role> roles,
        @NonNull Preset preset
    ) {
        this.players = players;
        this.roles = roles;
        this.preset = preset;
    }


    public GameEngine configure(GameConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public GameEngine setChannels(
        PhaseChannel<NightPhaseContext> nightPhaseChannel,
        PhaseChannel<MorningPhaseContext> morningPhaseChannel,
        PhaseChannel<VotingPhaseContext> votingPhaseChannel,
        PhaseChannel<VotingResultPhaseContext> votingResultPhaseChannel,
        PhaseChannel<GameResultPhaseContext> gameResultPhaseChannel
    ) {
        this.nightPhaseChannel = nightPhaseChannel;
        this.morningPhaseChannel = morningPhaseChannel;
        this.votingPhaseChannel = votingPhaseChannel;
        this.votingResultPhaseChannel = votingResultPhaseChannel;
        this.gameResultPhaseChannel = gameResultPhaseChannel;
        return this;
    }
    
    public Object getGameStatus(String key) {
        return gameStatus.get(key);
    }

    public void start() {
        var durations = getDurations();
        gameStatus.put("nightCounter", 1);
        gameStatus.put("nightTimeLeft", durations.get("nightTimeActionTimer"));
        gameStatus.put("discussionTimeLeft", durations.get("daytimeDiscussionTimer"));
        gameStatus.put("votingTimeLeft", durations.get("dayTimeVotingTimer"));

        gameState = GameState.LOADING;

        distributionEngine.distributeRoles(preset, roles, players);
        
        gameState = GameState.NIGHT;

        while (gameState != GameState.ENDED) {
            switch (gameState) {
                case NIGHT      -> handleNightPhase(durations.get("nightTimeActionTimer"));
                case DAY        -> handleDayPhase();
                case DISCUSSION -> handleDiscussionPhase(durations.get("daytimeDiscussionTimer"));
                case VOTING     -> handleVotingPhase(durations.get("dayTimeVotingTimer"));
                default -> {}
            }
        }
    }

    private Map<String, Long> getDurations() {
        return Map.of(
            "nightTimeActionTimer", configuration.getConfiguration(
                                        "general", 
                                        "nightTimeActionTimer"
                                    ).getDurationValue()
                                    .toSeconds(),
            "daytimeDiscussionTimer", configuration.getConfiguration(
                                        "general", 
                                        "daytimeDiscussionTimer"
                                    ).getDurationValue()
                                    .toSeconds(),
            "dayTimeVotingTimer", configuration.getConfiguration(
                                        "general", 
                                        "dayTimeVotingTimer"
                                    ).getDurationValue()
                                    .toSeconds()
        );
    }

    private void handleVotingPhase(long votingDuration) {
        gameStatus.put("votingTimeLeft", votingDuration);
        for (var i = votingDuration; i >= 0; i--) {
            sleep(1000);
            gameStatus.put("votingTimeLeft", i);
        }
        
        if (!votingPhaseChannel.hasSent()) {
            gameStatus.put("votingTimeLeft", 0);
            return;
        }

        var votes = votingPhaseChannel.receive().getVotes();
        votingPhaseChannel.clear();
        
        var result = processVoteResult(votes);
        votingResultPhaseChannel.send(new VotingResultPhaseContext(result));
        
        concludeRound();
    }

    private void handleDiscussionPhase(long discussionDuration) {
        gameStatus.put("discussionTimeLeft", discussionDuration);
        for (var i = discussionDuration; i >= 0; i--) {
            sleep(1000);
            gameStatus.put("discussionTimeLeft", i);
        }

        setState(GameState.VOTING);
    }

    @SuppressWarnings("unchecked")
    private void handleDayPhase() {
        var contexts = nightPhaseChannel.receive().getContexts();

        if (contexts.isEmpty()) {
            nightPhaseChannel.clear();
            return;
        }

        nightPhaseChannel.clear();
        playerEngine.updatePlayersState(contexts, players);

        var killedThisNight = StreamUtils.filter(
            players,
            p -> p.state() == PlayerState.KILLED
        );

        var healedThisNight = StreamUtils.filter(
            players,
            p -> p.state() == PlayerState.SAVED
        );
        
        killedThisNight.forEach(p -> p.state(PlayerState.DEAD));
        healedThisNight.forEach(p -> p.state(PlayerState.ALIVE));

        var killed = StreamUtils.mapToList(killedThisNight, p -> new NightEvent(PlayerState.KILLED, p));
        var healed = StreamUtils.mapToList(healedThisNight, p -> new NightEvent(PlayerState.SAVED, p));

        var morningPhaseContext = new MorningPhaseContext();
        morningPhaseContext.setContexts(StreamUtils.combineLists(killed, healed));
        morningPhaseChannel.send(morningPhaseContext);
        
        setState(GameState.DISCUSSION);
    }

    private void handleNightPhase(long nightDuration) {
        gameStatus.put("nightTimeLeft", nightDuration);
        for (var i = nightDuration; i >= 0; i--) {
            sleep(1000);
            gameStatus.put("nightTimeLeft", i);
        }
        
        setState(GameState.DAY);
    }

    private void setState(GameState state) {
        gameState = state;
        sleep(100);
    }

    private void concludeRound() {
        var aliveGoodPlayers = StreamUtils.filter(players, player -> player.state() == PlayerState.ALIVE && player.side().equals("Good"));
        var aliveNeutralPlayers = StreamUtils.filter(players, player -> player.state() == PlayerState.ALIVE && player.side().equals("Neutral"));
        var aliveEvilPlayers = StreamUtils.filter(players, player -> player.state() == PlayerState.ALIVE && player.side().equals("Evil"));
        
        if (aliveGoodPlayers.size() + aliveNeutralPlayers.size() <= 2) {
            var gameResult = new GameResult("Evil wins");
            var gameResultContext = new GameResultPhaseContext(gameResult);
            gameResultPhaseChannel.send(gameResultContext);
            gameState = GameState.ENDED;
        } else if (aliveGoodPlayers.size() + aliveNeutralPlayers.size() == aliveEvilPlayers.size()) {
            var gameResult = new GameResult("Evil wins");
            var gameResultContext = new GameResultPhaseContext(gameResult);
            gameResultPhaseChannel.send(gameResultContext);
            gameState = GameState.ENDED;
        } else if (aliveEvilPlayers.size() > 0) {
            gameState = GameState.NIGHT;
            var nightCounter = (int) gameStatus.get("nightCounter");
            gameStatus.put("nightCounter", nightCounter + 1);
        } else {
            var gameResult = new GameResult("Good wins");
            var gameResultContext = new GameResultPhaseContext(gameResult);
            gameResultPhaseChannel.send(gameResultContext);
            gameState = GameState.ENDED;
        }
    }

    private VoteResult processVoteResult(@NonNull List<PlayerVote> votes) {
        Map<Player, Integer> voteCount = new HashMap<>();

        for (var vote : votes) {
            voteCount.putIfAbsent(vote.target(), 0);
            voteCount.put(vote.target(), voteCount.get(vote.target()) + 1);
        }

        Player playerWithMaxVotes = voteCount.entrySet().stream()
            .max(Entry.comparingByValue())
            .get()
            .getKey();

        VoteResult result;
        if (playerWithMaxVotes == null) {
            result = new VoteResult(null, 0, null);
        } else {
            result = new VoteResult(playerWithMaxVotes, voteCount.get(playerWithMaxVotes), null);
            playerWithMaxVotes.state(PlayerState.DEAD);
        }
        return result;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
