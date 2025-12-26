package mafia.engine.role;

import java.util.Collections;
import java.util.List;

import lombok.NonNull;
import mafia.engine.player.Player;
import mafia.engine.presets.Preset;
import mafia.engine.util.StreamUtils;

public class DistributionEngine {

    public void distributeRoles(
        Preset preset, 
        List<Player> players, 
        List<Role> roles, 
        String roleType
    ) {
        List<String> roleDistribution = RoleDistribution.getDistribution(
            preset,
            roles,
            players.size(),
            roleType
        );
        assignRoles(roles, roleDistribution, players, roleType);
    }

    private void assignRoles(
        List<Role> roles, 
        List<String> roleDistribution, 
        List<Player> players, 
        @NonNull String roleType
    ) {
        Collections.shuffle(players);
        for (var player : players) {
            if (roleDistribution.isEmpty()) {
                break;
            }

            Collections.shuffle(roleDistribution);
            String roleName = roleDistribution.removeFirst();
            var role = StreamUtils.findOrElse(
                roles, 
                r -> r.getRoleName().equalsIgnoreCase(roleName), 
                null
            );
            switch (roleType.toLowerCase()) {
                case "primary" -> {
                    player.role(role);
                    player.alignment(player.role().getAlignment());
                }
                case "secondary" -> player.secondaryRole(role);
                default -> throw new IllegalStateException("Unexpected role type: " + roleType);
            }
        }

        var isSoulmateRole = roles.getFirst().getRoleName().equals("Soulmate");
        if (isSoulmateRole) {
            var soulMates = StreamUtils.filter(players, p -> p.secondaryRole() != null);
            
            if (soulMates.size() % 2 == 1) {
                throw new IllegalStateException("Odd number of soulmates");
            }

            for (var it = soulMates.iterator(); it.hasNext(); ) {
                var first = it.next();
                var second = it.next();

                first.getProperties().addProperty("soulmate", second);
                second.getProperties().addProperty("soulmate", first);
            }
        }
    }
}
