package mafia.engine.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mafia.engine.presets.Preset;

public class RoleDistribution {

    public static List<String> getDistribution(Preset preset, List<Role> availableRoles, int numberOfPlayers, String roleType) {
        Map<String, Integer> roleDistribution = new HashMap<>();

        var list = switch (roleType.toLowerCase()) {
            case "primary" -> preset.getPrimaryRoles();
            case "secondary" -> preset.getSecondaryRoles();
            default -> throw new IllegalStateException("Unexpected role type: " + roleType);
        };
        
        for (var roles : list) {
            var count = Integer.parseInt(String.valueOf(roles.get("players")));
            var roleName = String.valueOf(roles.get("role"));
            roleDistribution.put(roleName, count);    
        }

        for (var distribution : roleDistribution.values()) {
            if (distribution == -1) {
                var unassignedLeft = roleDistribution.values().stream().reduce(0, Integer::sum);
                roleDistribution.replaceAll((key, value) -> value == -1 ? numberOfPlayers - unassignedLeft - 1: value);
            }
        }

        List<String> roles = new ArrayList<>();
        roleDistribution.keySet().forEach(key -> addToList(roles, roleDistribution, key));
        return roles;
    }

    private static <K, V> void addToList(List<K> list, Map<K, Integer> map, K key) {
        for (int i = 0; i < map.get(key); i++) {
            list.add(key);
        }
    }
}
