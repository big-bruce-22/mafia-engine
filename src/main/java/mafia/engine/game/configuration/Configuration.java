package mafia.engine.game.configuration;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    @Getter @Setter
    private String configurationName, displayName;

    @Getter @Setter
    private ValueType valueType;

    private Object value;

    @Getter @Setter
    private List<Object> selectionValues;

    @Getter @Setter
    private List<String> configurationDescription;

    public void setValue(Object value) {
        if (!isValidValue(value)) {
            var message = "Invalid value for type of %s : %s in configuration %s"
                .formatted(valueType, value, configurationName);
            throw new IllegalArgumentException(message);
        }
        this.value = value;
    }

    public boolean getBooleanValue() {
        validateConvertion(valueType, ValueType.BOOLEAN);
        return Boolean.valueOf(getStringValue());
    }

    public int getIntegerValue() {
        validateConvertion(valueType, ValueType.INTEGER);
        return Integer.parseInt(getStringValue());
    }

    public Duration getDurationValue() {
        validateConvertion(valueType, ValueType.DURATION);
        var durationString = getStringValue();
        var i = Integer.parseInt(getStringValue().replaceAll("[a-zA-Z]", ""));
        if (durationString.endsWith("s")) {
            return Duration.ofMillis(i * 1000);
        }
        if (durationString.endsWith("m")) {
            return Duration.ofMinutes(i);
        }
        if (durationString.endsWith("h")) {
            return Duration.ofHours(i);
        }
        throw new IllegalStateException("Unexpected duration: " + getStringValue());
    }

    public String getStringValue() {
        return String.valueOf(value);
    }

    private void validateConvertion(ValueType original, ValueType to) {
        if (original != to) {
            throw new IllegalStateException("Cannot convert " + original + " to " + to);
        }
    }

    private boolean isValidValue(Object value) {
        return switch (valueType) {
            case DURATION -> isValidDuration(value);
            case INTEGER -> value instanceof Integer;
            case BOOLEAN -> value instanceof Boolean;
            case STRING -> value instanceof String;
            default -> false;
        };
    }

    private boolean isValidDuration(Object value) {
        return value instanceof String strValue && strValue.matches("\\d+[smh]");
    }

    @Override
    public String toString() {
        return """
            configurationName: %s
            displayName: %s
            valueType: %s
            value: %s
            configurationDescription: [
                %s
            ]
            """.formatted(
                configurationName, 
                displayName,
                valueType,
                value,
                configurationDescription.stream().collect(Collectors.joining("\n"))
            );
    }
}
