import com.fasterxml.jackson.databind.ObjectMapper;

import mafia.engine.ability.Ability;
import mafia.engine.config.PresetsConfig;
import mafia.engine.config.RoleConfig;
import mafia.engine.context.Context;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerEngine;
import mafia.engine.role.Role;
import mafia.engine.role.RoleDistribution;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        PlayerEngine playerEngine = new PlayerEngine();

        var players = generatePlayers();
        var roles = roleConfig.getRoles();
        List<String> roleDistribution = RoleDistribution.getDistribution(
                presetsConfig.getPresets().getFirst(),
                roles,
                players.size()
            );

        assignRoles(roles, roleDistribution, players);
        
        System.out.println("--- Player Roles ---");
        players.forEach(p -> {
            System.out.println(p.getName() + " -> " + p.getRole().getRoleName());
        });
        
        var contexts = new ArrayList<Context>();
        Random rand = new Random();
        for (var player : players) {
            if (player.getRole().getAbilities().isEmpty()) {
                continue;
            }

            var ctx = new Context();
            ctx.actor(player);
            var randomTarget = players.get(rand.nextInt(0, players.size()));
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

        playerEngine.updatePlayersState(contexts, players);

        System.out.println("\n--- Action Results ---");
        for (var player : players) {
            for (var result : player.getActionResults()) {
                System.out.println(result.toString());
            }
        }

        System.out.println("\n--- Player States ---");
        for (var player : players) {
            System.out.println(player.getName() + " is " + player.getState());
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