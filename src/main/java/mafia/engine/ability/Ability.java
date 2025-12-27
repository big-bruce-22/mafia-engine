package mafia.engine.ability;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.player.PlayerAction;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;

@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ability implements PropertyHolder {

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
    private Map<String, Object> abilityProperties;

    @Getter @Setter
    private boolean isUsable = true;

    private Properties properties = new Properties("ability");

    public Ability() {
        properties.addProperty("name", name);
        properties.addProperty("description", description);
        properties.addProperty("required", required);
        properties.addProperty("abilityProperties", abilityProperties);
    }

    public void setName(String name) {
        this.name = name;
        properties.addProperty("name", name);
    }

    public void setDescription(String description) {
        this.description = description;
        properties.addProperty("description", description);
    }

    public void setRequired(boolean required) {
        this.required = required;
        properties.addProperty("required", required);
    }

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
                    abilityProperties == null ? "" : abilityProperties
                );
    }

    public PlayerAction getAction() {
        return PlayerAction.parse(name);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
