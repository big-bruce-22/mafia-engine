package mafia.engine.rule;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import mafia.engine.player.Player;
import mafia.engine.presets.Preset;
import mafia.engine.role.Role;
import mafia.engine.role.RoleDistribution;
import mafia.engine.util.StreamUtils;

public class DistributionEngine {
    
    private Random rand = new Random();

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
        Collections.shuffle(roleDistribution);
        for (var player : players) {
            String roleName = roleDistribution.remove(rand.nextInt(0, roleDistribution.size()));
            var role = StreamUtils.findOrElse(
                roles, 
                r -> r.getRoleName().equalsIgnoreCase(roleName), 
                null
            );
            // System.out.println("assigning role for : " + player.getName() + " : " + role.getRoleName());
            player.setRole(role);
            player.setSide(player.getRole().getAlignment());
        }
    }
}
