    import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;

import mafia.engine.ability.Ability;
import mafia.engine.config.PresetsConfig;
import mafia.engine.config.RoleConfig;
import mafia.engine.game.GameEngine;
import mafia.engine.game.GameState;
import mafia.engine.game.phase.MorningPhaseContext;
import mafia.engine.game.phase.NightPhaseContext;
import mafia.engine.game.phase.PhaseChannel;
import mafia.engine.game.phase.VotingPhaseContext;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerState;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.role.Role;
import mafia.engine.util.StreamUtils;

class Loader {

    public static <T> T load(String path, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Path.of(path).toFile(), clazz);
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        RoleConfig roleConfig = Loader.load("PrimaryRoles.json", RoleConfig.class);
        PresetsConfig presetsConfig = Loader.load("Presets.json", PresetsConfig.class);
        
        var players = generatePlayers();
        var roles = roleConfig.getRoles();
        var preset = presetsConfig.getPresets().getFirst();

        PhaseChannel<NightPhaseContext> nightPhaseAdapter = new PhaseChannel<>();
        PhaseChannel<MorningPhaseContext> morningPhaseAdapter = new PhaseChannel<>();
        PhaseChannel<VotingPhaseContext> votingPhaseAdapter = new PhaseChannel<>();

        GameEngine gameEngine = new GameEngine(players, roles, preset, nightPhaseAdapter, morningPhaseAdapter, votingPhaseAdapter);

        new Thread(gameEngine::start).start();

        // Wait until roles are assigned
        while (gameEngine.gameState() != GameState.NIGHT) {
            Thread.sleep(10);
        }

        while (gameEngine.gameState() != GameState.ENDED) {
            var nightContext = new NightPhaseContext();
            nightContext.setContexts(generateNightContexts(gameEngine.players()));
            nightPhaseAdapter.send(nightContext);
            
            while (!morningPhaseAdapter.hasSent() && gameEngine.gameState() == GameState.DAY) {
                Thread.sleep(1000);
            }

            

            if (gameEngine.gameState() == GameState.VOTING) {
                var nightKills = morningPhaseAdapter.receive().getResult();
                nightKills.forEach(System.out::println);    
            }
            
        }
    }

    private static List<PlayerActionContext> generateNightContexts(List<Player> players) {
        synchronized (players) {
            var contexts = new ArrayList<PlayerActionContext>();
            Random rand = new Random();
            for (var player : players) {
                if (player.getRole().getAbilities().isEmpty()) {
                    continue;
                }
    
                var alivePlayers = StreamUtils.filter(players, p -> p.getState() == PlayerState.ALIVE);
                var randomTarget = alivePlayers.get(rand.nextInt(0, alivePlayers.size()));
    
                if (randomTarget == player) {
                    randomTarget = alivePlayers.get((alivePlayers.indexOf(player) + 1) % alivePlayers.size());
                }
                
                var ctx = new PlayerActionContext();
                ctx.actor(player);
                ctx.target(randomTarget);
                contexts.add(ctx);
    
                var abilityName = switch (player.getRole().getRoleName()) {
                    case "Doctor" -> "heal";
                    case "Detective" -> "investigate";
                    case "Killer" -> "nightKill";
                    default -> "";
                };
                ctx.ability(getAbility(player.getRole(), abilityName));
            }
            return contexts;
        }
    }

    private static Ability getAbility(Role role, String abilityName) {
        for (var ability : role.getAbilities()) {
            if (ability.name().equalsIgnoreCase(abilityName)) {
                return ability;
            }
        }
        return null;
    }

    private static void assignRoles(List<Role> roles, List<String> roleDistribution, List<Player> players) {
        Random rand = new Random();
        Collections.shuffle(players);
        Collections.shuffle(roleDistribution);
        for (var player : players) {
            String roleName = roleDistribution.remove(rand.nextInt(0, roleDistribution.size()));
            player.setRole(getRoleByName(roles, roleName));
            player.setSide(player.getRole().getAlignment());
        }
    }

    private static Role getRoleByName(List<Role> roles, String roleName) {
        for (var role : roles) {
            if (role.getRoleName().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return null;
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

        var players = new ArrayList<Player>();

        for (var name : names) {
            var player = new Player();
            player.setName(name);
            players.add(player);
        }

        return players;
    }
}