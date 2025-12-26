package tui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mafia.engine.ability.Ability;
import mafia.engine.config.PresetsConfig;
import mafia.engine.config.RoleConfig;
import mafia.engine.config.loader.Loader;
import mafia.engine.core.GameConfiguration;
import mafia.engine.core.GameEngine;
import mafia.engine.core.GameRules;
import mafia.engine.core.GameState;
import mafia.engine.expression.evaluator.Evaluator;
import mafia.engine.expression.lexer.Lexer;
import mafia.engine.expression.parser.Parser;
import mafia.engine.game.channel.BlockingChannel;
import mafia.engine.game.channel.Channel;
import mafia.engine.game.channel.prompt.Prompt;
import mafia.engine.game.channel.prompt.PromptResponse;
import mafia.engine.game.context.GameResultContext;
import mafia.engine.game.context.MorningContext;
import mafia.engine.game.context.NightContext;
import mafia.engine.game.context.RoleRevealContext;
import mafia.engine.game.context.VotingContext;
import mafia.engine.game.context.VotingResultContext;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerState;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.role.DistributionEngine;
import mafia.engine.role.Role;
import mafia.engine.util.StreamUtils;
import mafia.engine.vote.PlayerVote;

public class Main {

    // public static void main(String[] args) throws Exception {
    //     Screen screen = new DefaultTerminalFactory().createScreen();
    //     screen.startScreen();

    //     String[] options = {"Option A", "Option B", "Option C"};
    //     boolean[] checked = {false, true, false};
    //     int selected = 0;

    //     while (true) {
    //         screen.clear();
    //         TextGraphics tg = screen.newTextGraphics();
    //         for (int i = 0; i < options.length; i++) {
    //             String prefix = checked[i] ? "[x] " : "[ ] ";
    //             if (i == selected) {
    //                 tg.setBackgroundColor(TextColor.ANSI.WHITE);
    //                 tg.setForegroundColor(TextColor.ANSI.BLACK);
    //             } else {
    //                 tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
    //                 tg.setForegroundColor(TextColor.ANSI.DEFAULT);
    //             }
    //             tg.putString(2, i + 2, prefix + options[i]);
    //         }
    //         screen.refresh();

    //         KeyStroke key = screen.readInput();
    //         if (key.getKeyType() == KeyType.ArrowDown) selected = (selected + 1) % options.length;
    //         else if (key.getKeyType() == KeyType.ArrowUp) selected = (selected - 1 + options.length) % options.length;
    //         else if (key.getKeyType() == KeyType.Character && key.getCharacter() == ' ') checked[selected] = !checked[selected];
    //         else if (key.getKeyType() == KeyType.Enter) break; // Done
    //     }

    //     screen.stopScreen();
    //     for (int i = 0; i < options.length; i++) {
    //         System.out.println(options[i] + ": " + checked[i]);
    //     }
    // }
        
    public static void main(String[] args) throws Exception {
        var primaryRoleConfig = Loader.load("mafia-engine/PrimaryRoles.yaml", RoleConfig.class);
        var secondaryRoleConfig = Loader.load("mafia-engine/SecondaryRoles.yaml", RoleConfig.class);
        var presetsConfig = Loader.load("mafia-engine/Presets.yaml", PresetsConfig.class);
        var gameConfig = Loader.load("mafia-engine/GameConfiguration.yaml", GameConfiguration.class);
        var gameRules  = Loader.load("mafia-engine/GameRules.yaml", GameRules.class);

        var players = generatePlayers();
        var primaryRoles = primaryRoleConfig.getRoles();
        var secondaryRoles = secondaryRoleConfig.getRoles();
        var preset = presetsConfig.getPresets().get(1);
    
        var nightPhaseChannel = new BlockingChannel<NightContext>();
        var morningPhaseChannel = new BlockingChannel<MorningContext>();
        var votingPhaseChannel = new BlockingChannel<VotingContext>();
        var votingResultPhaseChannel = new BlockingChannel<VotingResultContext>();
        var gameResultPhaseChannel = new BlockingChannel<GameResultContext>();
        var roleRevealPhaseChannel = new BlockingChannel<RoleRevealContext>();

        var promptChannel = new BlockingChannel<Prompt>();
        var promptResponseChannel = new BlockingChannel<PromptResponse>();

        // DistributionEngine distributor = new DistributionEngine();
        // distributor.distributeRoles(preset, players, primaryRoles, "primary");
        // distributor.distributeRoles(preset, players, secondaryRoles, "secondary");

        // var lexer = new Lexer();
        // var parser = new Parser();
        // var evaluator = new Evaluator();
        // var parsed = parser.parse(lexer.tokenize("""
        //     count(game.players, player.state is ALIVE)
        //     """));
        // var parsed = parser.parse(lexer.tokenize("player.soulmate.state"));
        // var parsed = parser.parse(lexer.tokenize("player.alignment is Evil"));

        var gameEngine = new GameEngine(
            players,
            primaryRoles,
            secondaryRoles,
            preset,
            gameRules
        ).setChannels(
            nightPhaseChannel,
            morningPhaseChannel,
            votingPhaseChannel,
            votingResultPhaseChannel,
            gameResultPhaseChannel,
            roleRevealPhaseChannel
        ).configure(gameConfig);

        // players.forEach(p -> p.state(PlayerState.ALIVE));
        // players.forEach(p -> System.out.println(p.name() + " assigned as " + p.role().getRoleName() + " / " + (p.secondaryRole() == null ? "" : p.secondaryRole().getRoleName())));
        // System.out.println();

        // for (var player : players) {
        //     var evaluation = evaluator.evaluate(parsed, player.properties(), "player");
        //     System.out.println("" + player.name() + " evaluation: " + evaluation.result());
        //     System.out.println(evaluation.type() + " : " + evaluation.result().getClass().getSimpleName());
        // }

        // players.forEach(p -> p.state(PlayerState.ALIVE));
        // var evaluation = evaluator.evaluate(parsed, gameEngine.gameProperties(), "game");
        // System.out.println("no of good + no of neutral < no of evils " + evaluation.result());

        // if (true) {
        //     return;
        // }
        
        new Thread(gameEngine::start).start();

        // Wait until roles are assigned
        while (gameEngine.gameState() != GameState.NIGHT) {
            Thread.sleep(10);
        }

        for (var p : players) {
            System.out.print(p.name() + " is " + p.role().getRoleName());
            if (p.secondaryRole() != null) {
                System.out.print(", " + p.secondaryRole().getRoleName());
            }
            System.out.println();
        }

        System.out.println();

        while (gameEngine.gameState() != GameState.ENDED) {
            switch (gameEngine.gameState()) {
                case NIGHT      -> handleNightPhase(nightPhaseChannel, morningPhaseChannel, gameEngine);
                case DAY        -> handleDayPhase(morningPhaseChannel, roleRevealPhaseChannel, gameEngine);
                case DISCUSSION -> handleDiscussionPhase(morningPhaseChannel, gameEngine);
                case VOTING     -> handleVotingPhase(votingPhaseChannel, votingResultPhaseChannel, roleRevealPhaseChannel, gameEngine);
                default -> Thread.sleep(10);
            }
        }

        if (gameEngine.gameState() == GameState.ENDED) {
            System.out.println("Game has ended!");
            System.out.println(gameResultPhaseChannel.receive().getContext());
            System.out.println("alive players");
            players.stream().filter(p -> p.state() == PlayerState.ALIVE).forEach(p -> System.out.println(p.name()));
        }
    }

    private static void handleVotingPhase(
        Channel<VotingContext> votingPhaseChannel,
        Channel<VotingResultContext> votingResultPhaseChannel,
        Channel<RoleRevealContext> roleRevealPhaseChannel,
        GameEngine gameEngine
    ) throws InterruptedException {
        
        System.out.println("Voting Phase...");
        var votingContext = new VotingContext();
        votingContext.setVotes(generatePlayerVotes(gameEngine.players()));
        votingPhaseChannel.send(votingContext);

        while (gameEngine.gameState() == GameState.VOTING) {
            Thread.sleep(10);
            System.out.printf("\rVoting time is ongoing... (%ss left)", gameEngine.gameProperties().getProperty("votingTimeLeft"));
        }

        System.out.println();
        System.out.println(votingResultPhaseChannel.receive().getContext());
        System.out.println();

        if (roleRevealPhaseChannel.receive() != null) {
            var roleReveals = roleRevealPhaseChannel.receive().getContext();
    
            for (var reveal : roleReveals) {
                System.out.print(reveal.player().name() + " is a " + reveal.role().getRoleName());
                if (reveal.secondaryRole() != null) {
                    System.out.print(" and " + reveal.secondaryRole().getRoleName() + "\n");
                }
            }

            roleRevealPhaseChannel.clear();
            System.out.println();
        }

        votingResultPhaseChannel.clear();
    }

    private static void handleDiscussionPhase(
        Channel<MorningContext> morningPhaseChannel,
        GameEngine gameEngine
    ) throws InterruptedException {

        System.out.println("Discussion Phase...");
        while (gameEngine.gameState() == GameState.DISCUSSION) {
            Thread.sleep(100);
            System.out.printf("\rDiscussion time is ongoing... (%ss left)", gameEngine.gameProperties().getProperty("discussionTimeLeft"));
        }

        System.out.println();
        System.out.println();
    }

    private static void handleDayPhase(
        Channel<MorningContext> morningPhaseChannel, 
        Channel<RoleRevealContext> roleRevealPhaseChannel,
        GameEngine gameEngine
    ) throws InterruptedException {

        System.out.println("Day Phase...");

        while (!morningPhaseChannel.hasSent()) {
            Thread.sleep(10);
        }
        
        var nightKills = morningPhaseChannel.receive().getContext();
        if (nightKills.isEmpty()) {
            return;
        }
        
        morningPhaseChannel.clear();

        System.out.println("Players killed last night:");
        nightKills.forEach(System.out::println);
        System.out.println();

        while (!roleRevealPhaseChannel.hasSent()) {
            Thread.sleep(10);
        }

        var roleReveals = roleRevealPhaseChannel.receive().getContext();
        roleRevealPhaseChannel.clear();

        for (var reveal : roleReveals) {
            System.out.print(reveal.player().name() + " is a " + reveal.role().getRoleName());
            if (reveal.secondaryRole() != null) {
                System.out.print(" and " + reveal.secondaryRole().getRoleName() + "\n");
            }
        }

        if (!roleReveals.isEmpty()) {
            System.out.println();
        }
    }

    private static void handleNightPhase(
        Channel<NightContext> nightPhaseChannel,
        Channel<MorningContext> morningPhaseChannel, 
        GameEngine gameEngine
    ) throws InterruptedException {

        System.out.println("--- Night " + gameEngine.gameProperties().getProperty("nightCounter") + " ---");
        System.out.println("Night Phase...");

        var nightContext = new NightContext();
        nightContext.setContexts(generateNightContexts(gameEngine.players()));
        nightPhaseChannel.send(nightContext);

        while (!morningPhaseChannel.hasSent() && gameEngine.gameState() == GameState.NIGHT) {
            Thread.sleep(100);
            System.out.printf("\rPlayer doing actions... (%ss left)", gameEngine.gameProperties().getProperty("nightTimeLeft"));
        }
        System.out.println();
        System.out.println();
    }

    private static List<PlayerVote> generatePlayerVotes(List<Player> players) {
        var alivePlayers = StreamUtils.filter(players, p -> p.state() == PlayerState.ALIVE);
        return StreamUtils.mapToList(
            alivePlayers,
            player -> new PlayerVote(player, getRandomPlayer(alivePlayers, player))
        );
    }

    private static List<PlayerActionContext> generateNightContexts(List<Player> players) {
        var contexts = new ArrayList<PlayerActionContext>();

        for (var player : players) {
            if (player.state() != PlayerState.ALIVE ||
                player.role().getAbilities().isEmpty()) {
                continue;
            }

            var alivePlayers = StreamUtils.filter(players, p -> p.state() == PlayerState.ALIVE);
            var randomTarget = getRandomPlayer(alivePlayers, player);
            // var randomTarget = player;

            var role = player.role().getRoleName().toLowerCase();
            if (role.equalsIgnoreCase("psycho")) {
                continue;
            }

            var abilityName = switch (role) {
                case "doctor" -> "heal";
                case "detective" -> "investigate";
                case "killer" -> "nightKill";
                default -> throw new IllegalStateException("Unexpected role: " + role);
            };

            var ability = getAbility(player.role(), abilityName);
            contexts.add(new PlayerActionContext(player, randomTarget, ability));
        }
        return contexts;
    }

    private static Ability getAbility(Role role, String abilityName) {
        return StreamUtils.findOrElse(
            role.getAbilities(),
            ability -> ability.name().equalsIgnoreCase(abilityName),
            null
        );
    }

    private static Player getRandomPlayer(List<Player> players, Player exclude) {
        var candidates = new ArrayList<>(players);
        candidates.remove(exclude);
        Collections.shuffle(candidates);
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private static List<Player> generatePlayers() {
        String[] names = {
            "Wayne",
            "Mark",
            "Aez",
            "Xav",
            "Pat",
            "Ricia",
            "Ruth",
            "Pj",
            "Claire",
            "Wilson",
            "Ralph"
        };

        return new ArrayList<>(Arrays.asList(names)
            .stream()
            .map(n -> new Player().name(n))
            .toList());
    }

    private static final String[] classNames = {
        "Ruth",
        "Chasten",
        "Wayne",
        "Jayjay",
        "Jahred",
        "Arnold",
        "Tristan",
        "Patrick",
        "Xavier",
        "Phoebe",
        "Mary",
        "Mark",
        "Jewel",
        "Adelaine",
        "Princess",
        "Juan",
        "Ralph",
        "Ricia",
        "Archangel",
        "Warren",
        "Michael",
        "Marie",
        "Aezekiel",
        "Jhayden",
        "John",
        "Criztian",
        "Carlos",
        "Naomi",
        "Jimwell",
        "Wilson",
        "Tristan",
        "Jeremy"
    };
}