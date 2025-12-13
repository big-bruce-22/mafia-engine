package mafia.engine.presets;

import java.util.List;
import java.util.Map;

import lombok.Getter;

public class Preset {

    @Getter
    private String name;

    @Getter
    private int minimumPlayers, maximumPlayers;

    @Getter
    private List<Map<String, Object>> primaryRoles, secondaryRoles;

    public String toString() {
        return """
            name: %s
            minimumPlayers: %d
            maximumPlayers: %d
            primaryRoles: %s
            secondaryRoles: %s
            """.formatted(
                name, minimumPlayers, maximumPlayers,
                primaryRoles.toString(), secondaryRoles.toString()
            );
    }
}
