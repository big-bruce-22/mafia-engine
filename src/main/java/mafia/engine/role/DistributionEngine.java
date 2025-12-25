package mafia.engine.role;

import java.util.Collections;
import java.util.List;

import mafia.engine.player.Player;
import mafia.engine.presets.Preset;
import mafia.engine.util.StreamUtils;

public class DistributionEngine {

    public void distributeRoles(Preset preset, List<Role> roles, List<Player> players) {
        List<String> roleDistribution = RoleDistribution.getDistribution(
                preset,
                roles,
                players.size()
            );
        assignRoles(roles, roleDistribution, players);
    }

    private void assignRoles(List<Role> roles, List<String> roleDistribution, List<Player> players) {
        Collections.shuffle(players);
        for (var player : players) {
            Collections.shuffle(roleDistribution);
            String roleName = roleDistribution.removeFirst();
            var role = StreamUtils.findOrElse(
                roles, 
                r -> r.getRoleName().equalsIgnoreCase(roleName), 
                null
            );
            player.role(role);
            player.alignment(player.role().getAlignment());
        }
    }
}
