package cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mafia.engine.ability.Ability;
import mafia.engine.config.GameConfiguration;
import mafia.engine.config.PresetsConfig;
import mafia.engine.config.RoleConfig;
import mafia.engine.game.GameEngine;
import mafia.engine.game.GameState;
import mafia.engine.game.phase.GameResultPhaseContext;
import mafia.engine.game.phase.MorningPhaseContext;
import mafia.engine.game.phase.NightPhaseContext;
import mafia.engine.game.phase.PhaseChannel;
import mafia.engine.game.phase.VotingPhaseContext;
import mafia.engine.game.phase.VotingResultPhaseContext;
import mafia.engine.game.vote.PlayerVote;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerState;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.role.Role;
import mafia.engine.util.StreamUtils;

public class Main {
    
    public static void main(String[] args) throws Exception {
        RoleConfig roleConfig = Loader.load("PrimaryRoles.yaml", RoleConfig.class);
        PresetsConfig presetsConfig = Loader.load("Presets.yaml", PresetsConfig.class);
        GameConfiguration gameConfig = Loader.load("GameConfiguration.yaml", GameConfiguration.class);

        var players = Collections.synchronizedList(generatePlayers());
        var roles = roleConfig.getRoles();
        var preset = presetsConfig.getPresets().getFirst();

        var nightPhaseChannel = new PhaseChannel<NightPhaseContext>();
        var morningPhaseChannel = new PhaseChannel<MorningPhaseContext>();
        var votingPhaseChannel = new PhaseChannel<VotingPhaseContext>();
        var votingResultPhaseChannel = new PhaseChannel<VotingResultPhaseContext>();
        var gameResultPhaseChannel = new PhaseChannel<GameResultPhaseContext>();

        var gameEngine = new GameEngine(
            players,
            roles,
            preset
        ).setChannels(
            nightPhaseChannel,
            morningPhaseChannel,
            votingPhaseChannel,
            votingResultPhaseChannel,
            gameResultPhaseChannel
        ).configure(gameConfig);
        
        new Thread(gameEngine::start).start();

        // Wait until roles are assigned
        while (gameEngine.gameState() != GameState.NIGHT) {
            Thread.sleep(10);
        }

        for (var p : players) {
            System.out.println(p.name() + " is " + p.role().getRoleName());
        }

        System.out.println();

        while (gameEngine.gameState() != GameState.ENDED) {
            switch (gameEngine.gameState()) {
                case NIGHT      -> handleNightPhase(nightPhaseChannel, morningPhaseChannel, gameEngine);
                case DAY        -> handleDayPhase(morningPhaseChannel, gameEngine);
                case DISCUSSION -> handleDiscussionPhase(morningPhaseChannel, gameEngine);
                case VOTING     -> handleVotingPhase(votingPhaseChannel, votingResultPhaseChannel, gameEngine);
                default -> Thread.sleep(10);
            }
        }

        if (gameEngine.gameState() == GameState.ENDED) {
            System.out.println("Game has ended!");
            System.out.println(gameResultPhaseChannel.receive().getResult());
        }
    }

    private static void handleVotingPhase(PhaseChannel<VotingPhaseContext> votingPhaseChannel,
            PhaseChannel<VotingResultPhaseContext> votingResultPhaseChannel, GameEngine gameEngine)
            throws InterruptedException {
        
        System.out.println("Voting Phase...");
        var votingContext = new VotingPhaseContext();
        votingContext.setVotes(generatePlayerVotes(gameEngine.players()));
        votingPhaseChannel.send(votingContext);

        while (gameEngine.gameState() == GameState.VOTING) {
            Thread.sleep(10);
            System.out.printf("\rVoting time is ongoing... (%ss left)", gameEngine.getGameStatus("votingTimeLeft"));
        }

        System.out.println();
        System.out.println(votingResultPhaseChannel.receive().getResult());
        System.out.println();

        votingResultPhaseChannel.clear();
    }

    private static void handleDiscussionPhase(PhaseChannel<MorningPhaseContext> morningPhaseChannel, GameEngine gameEngine)
            throws InterruptedException {

        System.out.println("Discussion Phase...");
        while (gameEngine.gameState() == GameState.DISCUSSION) {
            Thread.sleep(100);
            System.out.printf("\rDiscussion time is ongoing... (%ss left)", gameEngine.getGameStatus("discussionTimeLeft"));
        }

        System.out.println();
        System.out.println();
    }

    private static void handleDayPhase(PhaseChannel<MorningPhaseContext> morningPhaseChannel, GameEngine gameEngine)
            throws InterruptedException {

        System.out.println("Day Phase...");

        while (!morningPhaseChannel.hasSent() && gameEngine.gameState() == GameState.DAY) {
            Thread.sleep(10);
        }
        
        var nightKills = morningPhaseChannel.receive().getResult();
        if (nightKills.isEmpty()) {
            return;
        }
        
        morningPhaseChannel.clear();

        System.out.println("Players killed last night:");
        nightKills.forEach(System.out::println);
        System.out.println();
    }

    private static void handleNightPhase(PhaseChannel<NightPhaseContext> nightPhaseChannel,
            PhaseChannel<MorningPhaseContext> morningPhaseChannel, GameEngine gameEngine) throws InterruptedException {

        System.out.println("--- Night " + gameEngine.getGameStatus("nightCounter") + " ---");
        System.out.println("Night Phase...");

        var nightContext = new NightPhaseContext();
        nightContext.setContexts(generateNightContexts(gameEngine.players()));
        nightPhaseChannel.send(nightContext);

        while (!morningPhaseChannel.hasSent() && gameEngine.gameState() == GameState.NIGHT) {
            Thread.sleep(100);
            System.out.printf("\rPlayer doing actions... (%ss left)", gameEngine.getGameStatus("nightTimeLeft"));
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
                        
            var abilityName = switch (player.role().getRoleName().toLowerCase()) {
                case "doctor" -> "heal";
                case "detective" -> "investigate";
                case "killer" -> "nightKill";
                default -> "";
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
            "Ruth",
            "Ricia",
            "Pj",
            "Claire",
            "Wilson"
        };

        return new ArrayList<>(Arrays.asList(names)
            .stream()
            .map(n -> new Player().name(n))
            .toList());
    }
}