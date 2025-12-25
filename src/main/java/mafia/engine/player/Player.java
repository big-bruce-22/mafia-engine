package mafia.engine.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import mafia.engine.player.action.PlayerActionResult;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;
import mafia.engine.role.Role;

@Accessors(fluent = true, chain = true)
public class Player implements PropertyHolder {

    @NonNull @Getter
    private String name, alignment;
    
    @NonNull @Getter
    private Role role;

    @NonNull @Getter
    private volatile PlayerState state = PlayerState.ALIVE;

    // Records of actions taken by the player
    @Getter
    private List<PlayerActionResult> playerActionResults = new ArrayList<>();

    // Count of receieved attempted actions by type
    @Getter
    private Map<PlayerAction, Integer> attemptedActions = new HashMap<>();

    public Player() {
        // properties.addProperty("soulmate", null);
    }

    @Getter
    private Properties properties = new Properties("player");

    public void incrementAttemptedAction(PlayerAction action) {
        if (!attemptedActions.containsKey(action)) {
            attemptedActions.put(action, 0);
        }
        attemptedActions.put(action, attemptedActions.get(action) + 1);
    }

    public Player name(String name) {
        this.name = name;
        properties.addProperty("name", name);
        return this;
    }

    public Player alignment(String alignment) {
        this.alignment = alignment;
        properties.addProperty("alignment", alignment);
        return this;
    }

    public Player role(Role role) {
        this.role = role;
        properties.addProperty("role", role);
        return this;
    }

    public Player state(PlayerState state) {
        this.state = state;
        properties.addProperty("state", state);
        return this;
    }

    @Override
    public String toString() {
        return """
            name: %s
            alignment: %s
            state: %s
            role: %s
            attemptedActions: %s
            """.formatted(
                name, alignment, state, role.getRoleName(),
                attemptedActions.toString()
            );
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
