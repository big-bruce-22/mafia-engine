package mafia.engine.core;

import static mafia.engine.util.StreamUtils.combineLists;
import static mafia.engine.util.StreamUtils.filter;
import static mafia.engine.util.StreamUtils.mapToList;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import mafia.engine.ability.AbilityEngine;
import mafia.engine.expression.ExpressionEngine;
import mafia.engine.game.event.NightEvent;
import mafia.engine.game.phase.GameResultPhaseContext;
import mafia.engine.game.phase.MorningPhaseContext;
import mafia.engine.game.phase.NightPhaseContext;
import mafia.engine.game.phase.PhaseChannel;
import mafia.engine.game.phase.RoleRevealPhaseContext;
import mafia.engine.game.phase.VotingPhaseContext;
import mafia.engine.game.phase.VotingResultPhaseContext;
import mafia.engine.game.vote.VoteResult;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerEngine;
import mafia.engine.player.PlayerState;
import mafia.engine.presets.Preset;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;
import mafia.engine.role.DistributionEngine;
import mafia.engine.role.Role;
import mafia.engine.role.RoleReveal;
import mafia.engine.rule.RuleEngine;

@Accessors(fluent = true)
public class GameEngine implements PropertyHolder {

    @NonNull @Getter
    private volatile GameState gameState = GameState.INITIALIZING;

    @NonNull @Getter @Setter
    private List<Player> players;
    
    @NonNull
    private List<Role> primaryRoles, secondaryRoles;

    @NonNull @Getter
    private GameConfiguration configuration;

    @NonNull @Getter @Setter
    private Preset preset;
    
    @Getter
    private final Properties gameProperties = new Properties("game");

    private final GameRules gameRules;

    private PhaseChannel<NightPhaseContext> nightPhaseChannel;
    private PhaseChannel<MorningPhaseContext> morningPhaseChannel;
    private PhaseChannel<VotingPhaseContext> votingPhaseChannel;
    private PhaseChannel<VotingResultPhaseContext> votingResultPhaseChannel;
    private PhaseChannel<GameResultPhaseContext> gameResultPhaseChannel;
    private PhaseChannel<RoleRevealPhaseContext> roleRevealPhaseChannel;

    private PlayerEngine playerEngine = new PlayerEngine();
    private RuleEngine ruleEngine = new RuleEngine();
    private AbilityEngine abilityEngine = new AbilityEngine();
    private DistributionEngine distributionEngine = new DistributionEngine();
    private ExpressionEngine expressionEngine = new ExpressionEngine();
    
    public GameEngine(
        @NonNull List<Player> players,
        @NonNull List<Role> primaryRoles,
        @NonNull List<Role> secondaryRoles,
        @NonNull Preset preset,
        @NonNull GameRules gameRules
    ) {
        this.players = players;
        this.primaryRoles = primaryRoles;
        this.secondaryRoles = secondaryRoles;
        this.preset = preset;
        this.gameRules = gameRules;

        gameProperties.addProperties(Map.of(
            "players", players,
            "primaryRoles", primaryRoles,
            "secondaryRoles", secondaryRoles,
            "preset", preset
        ));

        gameRules.getRules()
            .values()
            .forEach(expressionEngine::loadExpressions);
    }

    public GameEngine configure(GameConfiguration configuration) {
        this.configuration = configuration;
        gameProperties.addProperty("configuration", configuration);
        return this;
    }

    public GameEngine setChannels(
        PhaseChannel<NightPhaseContext> nightPhaseChannel,
        PhaseChannel<MorningPhaseContext> morningPhaseChannel,
        PhaseChannel<VotingPhaseContext> votingPhaseChannel,
        PhaseChannel<VotingResultPhaseContext> votingResultPhaseChannel,
        PhaseChannel<GameResultPhaseContext> gameResultPhaseChannel,
        PhaseChannel<RoleRevealPhaseContext> roleRevealPhaseChannel
    ) {
        this.nightPhaseChannel = nightPhaseChannel;
        this.morningPhaseChannel = morningPhaseChannel;
        this.votingPhaseChannel = votingPhaseChannel;
        this.votingResultPhaseChannel = votingResultPhaseChannel;
        this.gameResultPhaseChannel = gameResultPhaseChannel;
        this.roleRevealPhaseChannel = roleRevealPhaseChannel;
        return this;
    }
    
    public Object getGameProperties(String key) {
        return gameProperties.getProperty(key);
    }

    public void start() {
        var durations = getDurations();
        gameProperties.addProperty("nightCounter", 1);
        gameProperties.addProperty("nightTimeLeft", durations.get("nightTimeActionTimer"));
        gameProperties.addProperty("discussionTimeLeft", durations.get("daytimeDiscussionTimer"));
        gameProperties.addProperty("votingTimeLeft", durations.get("dayTimeVotingTimer"));

        gameState = GameState.LOADING;

        distributionEngine.distributeRoles(preset, players, primaryRoles, "primary");
        distributionEngine.distributeRoles(preset, players, secondaryRoles, "secondary");
        
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
            "nightTimeActionTimer", configuration.getDurationConfiguration(
                                        "general", 
                                        "nightTimeActionTimer"
                                    ).toSeconds(),
            "daytimeDiscussionTimer", configuration.getDurationConfiguration(
                                        "general", 
                                        "daytimeDiscussionTimer"
                                    ).toSeconds(),
            "dayTimeVotingTimer", configuration.getDurationConfiguration(
                                        "general", 
                                        "dayTimeVotingTimer"
                                    ).toSeconds()
        );
    }

    private void handleVotingPhase(long votingDuration) {
        gameProperties.addProperty("votingTimeLeft", votingDuration);
        for (var i = votingDuration; i >= 0; i--) {
            sleepInSeconds(1);
            gameProperties.addProperty("votingTimeLeft", i);
        }

        // buffer time
        sleepInSeconds(1);
        
        if (!votingPhaseChannel.hasSent()) {
            gameProperties.addProperty("votingTimeLeft", 0);
            return;
        }

        var votes = votingPhaseChannel.receive().getVotes();
        votingPhaseChannel.clear();

        var result = new VoteResult(votes, configuration);
        votingResultPhaseChannel.send(new VotingResultPhaseContext(result));

        if (result.target() != null) {
            revealPlayerprimaryRoles(List.of(result.target()), GameState.VOTING);
        }

        concludeRound();
    }

    private void handleDiscussionPhase(long discussionDuration) {
        gameProperties.addProperty("discussionTimeLeft", discussionDuration);
        for (var i = discussionDuration; i >= 0; i--) {
            sleepInSeconds(1);
            gameProperties.addProperty("discussionTimeLeft", i);
        }

        setState(GameState.VOTING);
    }

    @SuppressWarnings("unchecked")
    private void handleDayPhase() {
        var contexts = nightPhaseChannel.receive().getContexts();

        var morningPhaseContext = new MorningPhaseContext();
        if (contexts.isEmpty()) {
            nightPhaseChannel.clear();
            morningPhaseChannel.send(morningPhaseContext);
            return;
        }

        nightPhaseChannel.clear();
        playerEngine.updatePlayersState(contexts, players, configuration);

        var isAnonymousHeal = configuration.getBooleanConfiguration(
                                "general", 
                                "anonymousHeal"
                            );
        
        var alivePlayers = filterPlayer(p -> p.state() == PlayerState.ALIVE);
        var killedThisNight = filterPlayer(p -> p.state() == PlayerState.KILLED);
        var healedThisNight = isAnonymousHeal ? List.<Player>of() : filterPlayer(p -> p.state() == PlayerState.SAVED);

        killedThisNight.forEach(p -> p.state(PlayerState.DEAD));
        if (!isAnonymousHeal) {
            healedThisNight.forEach(p -> p.state(PlayerState.ALIVE));
        }

        var killedEvents = mapToList(killedThisNight, p -> new NightEvent(PlayerState.KILLED, p));
        var healedEvents = isAnonymousHeal ? List.<NightEvent>of() : 
            mapToList(healedThisNight, p -> new NightEvent(PlayerState.SAVED, p));

        morningPhaseContext.setContexts(combineLists(killedEvents, healedEvents));
        morningPhaseChannel.send(morningPhaseContext);

        revealPlayerprimaryRoles(killedThisNight, GameState.DAY);

        if (alivePlayers.size() < 3) {
            concludeRound();
        } else {
            setState(GameState.DISCUSSION);
        }
    }

    private void revealPlayerprimaryRoles(List<Player> playersToReveal, GameState state) {
        if (playersToReveal.isEmpty()) {
            return;
        }

        var secretRoles = configuration.getBooleanConfiguration(
                                "general", 
                                "secretRoles"
                            );
        
        var secretVoteOut = configuration.getBooleanConfiguration(
            "general", 
            "secretVoteOut"
        );

        var roleRevealPhaseContext = new RoleRevealPhaseContext();

        if (secretRoles && secretVoteOut) {
            roleRevealPhaseChannel.send(roleRevealPhaseContext);
            return;    
        }

        if (!secretVoteOut) {
            for (var p : playersToReveal) {
                for (var rule : gameRules.getRules("roleRevealConditions")) {
                    try {
                        if ((Boolean) expressionEngine.evalaute(rule, p.getProperties()).result()) {
                            roleRevealPhaseContext.addReveal(new RoleReveal(p, p.role()));
                            break;
                        }
                    } catch (IllegalStateException _) {
                        continue;
                    }
                }
            }
        } else if (gameState == GameState.VOTING && !secretRoles) {
            var p = playersToReveal.getFirst();
            roleRevealPhaseContext.addReveal(new RoleReveal(p, p.role()));
        }
        roleRevealPhaseChannel.send(roleRevealPhaseContext);
    }

    private void handleNightPhase(long nightDuration) {
        gameProperties.addProperty("nightTimeLeft", nightDuration);
        for (var i = nightDuration; i >= 0; i--) {
            sleepInSeconds(1);
            gameProperties.addProperty("nightTimeLeft", i);
        }
        
        setState(GameState.DAY);
    }

    private void concludeRound() {
        var evilWin = evaluateBooleanExpression("evilWinningCondition");
        var continueRound = evaluateBooleanExpression("continueRoundConditions");

        if (evilWin) {
            var gameResult = new GameResult("Evil wins");
            var gameResultContext = new GameResultPhaseContext(gameResult);
            gameResultPhaseChannel.send(gameResultContext);
            gameState = GameState.ENDED;            
        } else if (continueRound) {
            gameState = GameState.NIGHT;
            var nightCounter = (int) gameProperties.getProperty("nightCounter");
            gameProperties.addProperty("nightCounter", nightCounter + 1);
        } else {
            var gameResult = new GameResult("Good wins");
            var gameResultContext = new GameResultPhaseContext(gameResult);
            gameResultPhaseChannel.send(gameResultContext);
            gameState = GameState.ENDED;
        }
    }
    
    private boolean evaluateBooleanExpression(String category) {
        for (var rule : gameRules.getRules(category)) {
            if ((Boolean) expressionEngine.evalaute(rule, gameProperties).result()) {
                return true;
            }
        }
        return false;
    }

    private List<Player> filterPlayer(Predicate<Player> filter) {
        return filter(players, filter);
    }

    private void sleepInSeconds(int seconds) {
        sleep(seconds * 1000);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setState(GameState state) {
        gameState = state;
        sleep(100);
    }

    @Override
    public Properties getProperties() {
        return gameProperties;
    }
}
