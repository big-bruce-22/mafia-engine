package mafia.engine.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
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
    @JsonProperty("required")
    private boolean required;

    @Getter
    @JsonProperty("immediateResult")
    private boolean immediateResult;
    
    @Getter
    @JsonProperty("optional")
    private boolean optional;

    @Getter
    @JsonProperty("trigger")
    private String trigger;

    @Getter
    @JsonProperty("abilityProperties")
    private Map<String, Object> abilityProperties;

    @Getter
    @JsonProperty("conditions")
    private List<String> conditions = new ArrayList<>();

    @Getter
    @JsonProperty("abilityTime")
    private String abilityTime;

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

    public void setImmediateResult(boolean immediateResult) {
        this.immediateResult = immediateResult;
        properties.addProperty("immediateResult", immediateResult);
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
        properties.addProperty("optional", optional);
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
        properties.addProperty("trigger", trigger);
    }

    public void setAbilityProperties(Map<String, Object> abilityProperties) {
        this.abilityProperties = abilityProperties;
        properties.addProperty("abilityProperties", abilityProperties);
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
        properties.addProperty("conditions", conditions);
    }

    public void setAbilityTime(String abilityTime) {
        this.abilityTime = abilityTime;
        properties.addProperty("abilityTime", abilityTime);
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
