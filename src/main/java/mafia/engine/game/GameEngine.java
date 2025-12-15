package mafia.engine.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.ability.AbilityEngine;
import mafia.engine.game.event.NightEvent;
import mafia.engine.game.phase.MorningPhaseContext;
import mafia.engine.game.phase.NightPhaseContext;
import mafia.engine.game.phase.PhaseChannel;
import mafia.engine.game.phase.VotingPhaseContext;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerEngine;
import mafia.engine.player.PlayerState;
import mafia.engine.presets.Preset;
import mafia.engine.role.Role;
import mafia.engine.rule.DistributionEngine;
import mafia.engine.rule.RuleEngine;
import mafia.engine.util.StreamUtils;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class GameEngine {

    @Getter
    private GameState gameState = GameState.INITIALIZING;

    @NonNull @Getter @Setter
    private List<Player> players;
    
    @NonNull
    private List<Role> roles;

    // @NonNull
    @Getter @Setter
    private GameConfiguration configuration;

    @NonNull @Getter @Setter
    private Preset preset;
    
    private final PhaseChannel<NightPhaseContext> nightPhaseAdapter;
    private final PhaseChannel<MorningPhaseContext> morningPhaseAdapter;
    private final PhaseChannel<VotingPhaseContext> votingPhaseAdapter;

    private PlayerEngine playerEngine = new PlayerEngine();
    private RuleEngine ruleEngine = new RuleEngine();
    private AbilityEngine abilityEngine = new AbilityEngine();
    private DistributionEngine distributionEngine = new DistributionEngine();
    private Random random = new Random();

    public void start() {
        System.out.println("starting");
        gameState = GameState.LOADING;

        // distribut roles
        System.out.println("assigning roles");
        synchronized (players) {
            distributionEngine.distributeRoles(preset, roles, players);
            players.forEach(p -> p.setState(PlayerState.ALIVE));
        }

        System.out.println("--- Player Roles ---");
        players.forEach(p -> {
            System.out.println(p.getName() + " -> " + p.getRole().getRoleName());
        });

        gameState = GameState.NIGHT;

        // get on configuration
        var discussionTimeLeftInSeconds = 5;

        List<Player> killedThisNight = new ArrayList<>();

        while (gameState != GameState.ENDED) {
            switch (gameState) {
                case NIGHT -> {
                    System.out.printf("\rThe night has come...");
                    if (!nightPhaseAdapter.hasSent()) continue;

                    gameState = GameState.DAY;
                    System.out.println();
                }
                // case DAY -> {
                //     var contexts = nightPhaseAdapter.receive().getContexts();
                    
                //     playerEngine.updatePlayersState(contexts, players);

                //     var morningPhaseContext = new MorningPhaseContext();
                //     synchronized (players) {
                //         morningPhaseContext.setContexts(
                //             StreamUtils.filterThenMapToList(
                //                 players, 
                //                 player -> player.getState() == PlayerState.KILLED, 
                //                 player -> new NightEvent(PlayerState.KILLED, player)
                //             )
                //         );
                //     }
                //     morningPhaseAdapter.send(morningPhaseContext);

                //     gameState = GameState.DISCUSSION;
                // }
                case DAY -> {
                    System.out.println("The sun has risen...");
                    System.out.println("results: ");
                    var contexts = nightPhaseAdapter.receive().getContexts();
                    // nightPhaseAdapter.clear();

                    playerEngine.updatePlayersState(contexts, players);

                    synchronized (players) {
                        killedThisNight = StreamUtils.filter(
                            players,
                            p -> p.getState() == PlayerState.KILLED
                        );
                    }

                    var morningPhaseContext = new MorningPhaseContext();
                    morningPhaseContext.setContexts(
                        killedThisNight.stream()
                            .map(p -> new NightEvent(PlayerState.KILLED, p))
                            .toList()
                    );

                    morningPhaseAdapter.send(morningPhaseContext);
                    gameState = GameState.DISCUSSION;
                }
                case DISCUSSION -> {
                    synchronized (players) {
                        for (var p : killedThisNight) {
                            p.setState(PlayerState.DEAD);
                        }
                        killedThisNight = new ArrayList<>();
                    }

                    try {
                        System.out.print("\rdiscussion: time left: " + discussionTimeLeftInSeconds + "s");
                        if (discussionTimeLeftInSeconds == 0) {
                            gameState = GameState.VOTING;
                            discussionTimeLeftInSeconds = 5;
                            System.out.println();
                        } else {
                            Thread.sleep(1000);
                            discussionTimeLeftInSeconds--;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                case VOTING -> {
                    // if (votingPhaseAdapter.hasSent()) continue;
                    // todo
                    // receive voting phase]

                    synchronized (players) {
                        var aliveGoodPlayers = StreamUtils.filter(players, player -> player.getSide().equals("Good"));
                        var aliveNeutralPlayers = StreamUtils.filter(players, player -> player.getSide().equals("Neutral"));
                        var aliveEvilPlayers = StreamUtils.filter(players, player -> player.getSide().equals("Evil"));
                        if (aliveGoodPlayers.size() + aliveNeutralPlayers.size() <= 2) {
                            gameState = GameState.ENDED;
                            System.out.println("Evil win");
                        } else if (aliveGoodPlayers.size() + aliveNeutralPlayers.size() == aliveEvilPlayers.size()) {
                            gameState = GameState.ENDED;
                            System.out.println("Evil win");
                        } else if (aliveEvilPlayers.size() > 0) {
                            gameState = GameState.NIGHT;
                        } else {
                            gameState = GameState.ENDED;
                            System.out.println("Good win");
                        }
                    }

                }
                case ENDED -> {}
                
                case LOADING -> {}
                case WAITING -> {}
                default -> {}
                
            }
        }
    }
}
