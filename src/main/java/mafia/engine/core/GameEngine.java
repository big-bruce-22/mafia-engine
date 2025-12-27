package mafia.engine.core;

import static mafia.engine.util.StreamUtils.combineLists;
import static mafia.engine.util.StreamUtils.filter;
import static mafia.engine.util.StreamUtils.mapToList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import mafia.engine.ability.Ability;
import mafia.engine.ability.AbilityEngine;
import mafia.engine.expression.ExpressionEngine;
import mafia.engine.game.channel.BlockingChannel;
import mafia.engine.game.channel.prompt.AbilityPrompt;
import mafia.engine.game.channel.prompt.AbilityPromptResponse;
import mafia.engine.game.channel.prompt.Prompt;
import mafia.engine.game.channel.prompt.PromptOption;
import mafia.engine.game.channel.prompt.PromptResponse;
import mafia.engine.game.channel.prompt.VotePrompt;
import mafia.engine.game.channel.prompt.VotePromptOption;
import mafia.engine.game.channel.prompt.VotePromptResponse;
import mafia.engine.game.event.NightActionResolutionUpdate;
import mafia.engine.game.event.GameEnded;
import mafia.engine.game.event.GameUpdate;
import mafia.engine.game.event.NightEvent;
import mafia.engine.game.event.PhasedChangedUpdate;
import mafia.engine.game.event.RoleRevealUpdate;
import mafia.engine.game.event.TimeRemainingUpdate;
import mafia.engine.game.event.VotingResultUpdate;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerEngine;
import mafia.engine.player.PlayerState;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.presets.Preset;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;
import mafia.engine.role.DistributionEngine;
import mafia.engine.role.Role;
import mafia.engine.role.RoleReveal;
import mafia.engine.rule.RuleEngine;
import mafia.engine.vote.PlayerVote;
import mafia.engine.vote.VoteResult;

@Accessors(fluent = true)
public class GameEngine implements PropertyHolder {

    @NonNull @Getter
    private volatile GameState gameState = GameState.INITIALIZING;
    private volatile GamePhase gamePhase = GamePhase.NIGHT;

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

    @Getter 
    private final BlockingChannel<Prompt> promptChannel = new BlockingChannel<>();
    
    @Getter 
    private final BlockingChannel<PromptResponse> promptResponseChannel= new BlockingChannel<>();
    
    @Getter 
    private final BlockingChannel<GameUpdate> gameUpdateChannel = new BlockingChannel<>();

    private final GameRules gameRules;

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
        
        gameState = GameState.STARTING;

        players.forEach(p -> {
            System.out.print(p.name() + " is " + p.role().getRoleName());
            if (p.secondaryRole() != null) {
                System.out.print(" with secondary role " + p.secondaryRole().getRoleName());
            }
            System.out.println();
        });
        System.out.println();

        gameUpdateChannel.send(new PhasedChangedUpdate(null, GamePhase.NIGHT));
        while (gameState != GameState.ENDED) {
            switch (gamePhase) {
                case NIGHT      -> handleNightPhase(durations.get("nightTimeActionTimer"));
                case DAY        -> handleDayPhase();
                case DISCUSSION -> handleDiscussionPhase(durations.get("daytimeDiscussionTimer"));
                case VOTING     -> handleVotingPhase(durations.get("dayTimeVotingTimer"));
                default -> {}
            }
        }
    }

    private void handleNightPhase(long nightDuration) {
        var playersWithRequiredAbiliies = players.stream()
            .filter(
                p -> p.role()
                    .getAbilities()
                    .stream()
                    .filter(Ability::required)
                    .count() > 0
            ).toList();
        
        // create prompt for each player
        var alivePlayers = filterPlayer(p -> p.state() == PlayerState.ALIVE);
        for (var player : playersWithRequiredAbiliies) {
            var abilities = filter(player.role().getAbilities(), Ability::required);
            var prompt = new AbilityPrompt(
                player,
                mapToList(abilities, a -> new PromptOption(a.name())),
                alivePlayers
            );
            promptChannel.send(prompt);
        }

        gameProperties.addProperty("nightTimeLeft", nightDuration);
        for (var i = nightDuration; i >= 0; i--) {
            sleepInSeconds(1);
            gameProperties.addProperty("nightTimeLeft", i);
            gameUpdateChannel.send(new TimeRemainingUpdate(GamePhase.NIGHT, i));
        }
        
        gameUpdateChannel.send(new PhasedChangedUpdate(GamePhase.NIGHT, GamePhase.DAY));
        setPhase(GamePhase.DAY);
    }

    @SuppressWarnings("unchecked")
    private void handleDayPhase() {
        var responses = new ArrayList<PromptResponse>();
        while (promptResponseChannel.hasSent()) {
            responses.add(promptResponseChannel.receive());
        }

        var contexts = new ArrayList<PlayerActionContext>();
        for (var response : responses) {
            if (!(response instanceof AbilityPromptResponse res)) {
                throw new IllegalStateException("Expected AbilityPromptResponse but got " + response.getClass());
            }

            var source = res.source();
            var option = res.option();
            var target = res.target();

            var ability = source.role().getAbilities().stream()
                .filter(a -> a.name().equalsIgnoreCase(option.option()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "Ability " + option + " not found for player " + source.name()
                ));
            contexts.add(new PlayerActionContext(source, target, ability));
        }

        playerEngine.updatePlayersState(contexts, players, configuration);

        var isAnonymousHeal = configuration.getBooleanConfiguration(
                                "general", 
                                "anonymousHeal"
                            );
        
        var killedThisNight = filterPlayer(p -> p.state() == PlayerState.KILLED);
        var healedThisNight = isAnonymousHeal ? List.<Player>of() : filterPlayer(p -> p.state() == PlayerState.SAVED);

        for (var it = killedThisNight.listIterator(); it.hasNext(); ) {
            var p = it.next();
            if (p.secondaryRole() != null) {
                if (p.secondaryRole().getRoleName().equalsIgnoreCase("Soulmate")) {
                    var soulmate = (Player) p.getProperties().getProperty("soulmate");
                    soulmate.state(PlayerState.KILLED);
                    it.add(soulmate);
                }
            }
        }

        killedThisNight = killedThisNight.stream().distinct().toList();

        killedThisNight.forEach(p -> p.state(PlayerState.DEAD));
        if (!isAnonymousHeal) {
            healedThisNight.forEach(p -> p.state(PlayerState.ALIVE));
        }

        var killedEvents = mapToList(killedThisNight, p -> new NightEvent(PlayerState.KILLED, p));
        var healedEvents = isAnonymousHeal ? List.<NightEvent>of() : 
            mapToList(healedThisNight, p -> new NightEvent(PlayerState.SAVED, p));
        
        var update = new NightActionResolutionUpdate(combineLists(killedEvents, healedEvents));
        gameUpdateChannel.send(update);
        
        revealPlayerPrimaryRoles(killedThisNight, GamePhase.DAY);

        var alivePlayers = filterPlayer(p -> p.state() == PlayerState.ALIVE);
        var aliveEvilPlayers = filterPlayer(p -> p.state() == PlayerState.ALIVE && p.alignment().equals("Evil"));
        
        if (alivePlayers.size() < 3 || aliveEvilPlayers.size() == 0) {
            concludeRound();
        } else {
            gameUpdateChannel.send(new PhasedChangedUpdate(GamePhase.DAY, GamePhase.DISCUSSION));
            setPhase(GamePhase.DISCUSSION);
        }
    }

    private void handleDiscussionPhase(long discussionDuration) {
        gameProperties.addProperty("discussionTimeLeft", discussionDuration);
        for (var i = discussionDuration; i >= 0; i--) {
            sleepInSeconds(1);
            gameProperties.addProperty("discussionTimeLeft", i);
            gameUpdateChannel.send(new TimeRemainingUpdate(GamePhase.NIGHT, i));
        }

        gameUpdateChannel.send(new PhasedChangedUpdate(GamePhase.DISCUSSION, GamePhase.VOTING));
        setPhase(GamePhase.VOTING);
    }

    private void handleVotingPhase(long votingDuration) {
        var alivePlayers = filterPlayer(p -> p.state() == PlayerState.ALIVE);

        var choices = mapToList(alivePlayers, VotePromptOption::new);
        for (var player : alivePlayers) {
            var prompt = new VotePrompt(
                player, 
                choices
            );
            promptChannel.send(prompt);
        }

        gameProperties.addProperty("votingTimeLeft", votingDuration);
        for (var i = votingDuration; i >= 0; i--) {
            sleepInSeconds(1);
            gameProperties.addProperty("votingTimeLeft", i);
            gameUpdateChannel.send(new TimeRemainingUpdate(GamePhase.NIGHT, i));
        }

        var votes = new ArrayList<PlayerVote>();
        while (promptResponseChannel.hasSent()) {
            var response = promptResponseChannel.receive();
            if (!(response instanceof VotePromptResponse res)) {
                throw new IllegalStateException("Expected VotePromptResponse but got " + response.getClass());
            }
            
            votes.add(new PlayerVote(res.source(), res.voteOption().player()));
        }
        
        var result = new VoteResult(votes, configuration);
        gameUpdateChannel.send(new VotingResultUpdate(result));
    
        if (result.target() != null) {
            var playersToReveal = new ArrayList<Player>();
            playersToReveal.add(result.target());
            playersToReveal.addAll(result.affectedByTarget());
            revealPlayerPrimaryRoles(playersToReveal, GamePhase.VOTING);
        }

        concludeRound();
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

    private void revealPlayerPrimaryRoles(List<Player> playersToReveal, GamePhase phase) {
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

        if (secretRoles && secretVoteOut) {
            gameUpdateChannel.send(new RoleRevealUpdate(List.of()));
            return;    
        }

        List<RoleReveal> reveals = new ArrayList<>();
        if (!secretVoteOut) {
            for (var p : playersToReveal) {
                for (var rule : gameRules.getRules("roleRevealConditions")) {
                    try {
                        if ((Boolean) expressionEngine.evalaute(rule, p.getProperties()).result()) {
                            reveals.add(new RoleReveal(p, p.role(), p.secondaryRole()));
                            break;
                        }
                    } catch (IllegalStateException _) {
                        continue;
                    }
                }
            }
        } else if (phase == GamePhase.VOTING && !secretRoles) {
            var p = playersToReveal.getFirst();
            reveals.add(new RoleReveal(p, p.role(), p.secondaryRole()));
        }
        gameUpdateChannel.send(new RoleRevealUpdate(reveals));
    }

    private void concludeRound() {
        var evilWin = evaluateBooleanExpression("evilWinningCondition");
        var continueRound = evaluateBooleanExpression("continueRoundConditions");

        if (evilWin) {
            gameUpdateChannel.send(new GameEnded("Evil wins"));
            setState(GameState.ENDED);
        } else if (continueRound) {
            gameUpdateChannel.send(new PhasedChangedUpdate(GamePhase.VOTING, GamePhase.NIGHT));
            gamePhase = GamePhase.NIGHT;
            var nightCounter = (int) gameProperties.getProperty("nightCounter");
            gameProperties.addProperty("nightCounter", nightCounter + 1);
        } else {
            gameUpdateChannel.send(new GameEnded("Good wins"));
            setState(GameState.ENDED);
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

    private void setPhase(GamePhase phase) {
        gamePhase = phase;
        sleep(100);
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
