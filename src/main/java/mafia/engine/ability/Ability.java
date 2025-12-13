package mafia.engine.ability;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.player.PlayerAction;

@NoArgsConstructor
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ability {

    @Getter
    @JsonProperty("ability")
    private String name;

    @Getter
    @JsonProperty("abilityDescription")
    private String description;

    @Getter
    private boolean required;

    @Getter
    @JsonProperty("abilityProperties")
    private Map<String, Object> properties;

    @Getter @Setter
    private boolean isUsable = true;

    @Override
    public String toString() {
        return """
                ability: %s
                description: %s
                required: %b
                abilityProperties: [
                    %s
                ]
                """.formatted(
                    name, 
                    description, 
                    required, 
                    properties == null ? "" : properties
                );
    }

    public PlayerAction getAction() {
        return PlayerAction.parse(name);
    }
}
