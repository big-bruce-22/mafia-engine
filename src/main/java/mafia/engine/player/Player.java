package mafia.engine.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private Role role, secondaryRole;

    @NonNull @Getter
    private volatile PlayerState state = PlayerState.ALIVE;

    // Records of actions taken by the player
    @Getter
    private List<PlayerActionResult> playerActionResults = new ArrayList<>();

    // Count of receieved attempted actions by type
    @Getter
    private Map<PlayerAction, Integer> attemptedActions = new HashMap<>();

    @Getter
    private Properties properties = new Properties("player");

    @Getter
    private final UUID PLAYER_ID = UUID.randomUUID();

    public Player() {
        properties.addProperty("votedOut", false);
        properties.addProperty("killed", false);
        properties.addProperty("killer", new ArrayList<Player>());
    }

    public void incrementAttemptedAction(PlayerAction action) {
        attemptedActions.putIfAbsent(action, 0);
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
    
    public Player secondaryRole(Role secondaryRole) {
        this.secondaryRole = secondaryRole;
        properties.addProperty("secondaryRole", secondaryRole);
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
            secondary role: %s
            attemptedActions: %s
            """.formatted(
                name, alignment, 
                state, role.getRoleName(), 
                secondaryRole == null ? "" : secondaryRole.getRoleName(),
                attemptedActions.toString()
            );
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
