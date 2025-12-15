package mafia.engine.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mafia.engine.player.action.PlayerActionResult;
import mafia.engine.role.Role;

public class Player {

    @NonNull @Getter @Setter
    private String name, side;
    
    @NonNull @Getter @Setter
    private Role role;

    @NonNull @Getter @Setter
    private PlayerState state;

    // Records of actions taken by the player
    @Getter
    private List<PlayerActionResult> playerActionResults = new ArrayList<>();

    // Count of receieved attempted actions by type
    @Getter
    private Map<PlayerAction, Integer> attemptedActions = new HashMap<>();

    public void incrementAttemptedAction(PlayerAction action) {
        if (!attemptedActions.containsKey(action)) {
            attemptedActions.put(action, 0);
        }
        attemptedActions.put(action, attemptedActions.get(action) + 1);
    }

    @Override
    public String toString() {
        return """
            name: %s
            side: %s
            state: %s
            role: %s
            attemptedActions: %s
            """.formatted(
                name, side, state, role.getRoleName(),
                attemptedActions.toString()
            );
    }
}
