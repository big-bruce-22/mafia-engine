package mafia.engine.config;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration implements PropertyHolder {

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
        var pattern = Pattern.compile("[h|m|s|ms]", Pattern.CASE_INSENSITIVE);
        var i = Integer.parseInt(pattern.matcher(getStringValue()).replaceAll(""));
        return switch (durationString.charAt(durationString.length() - 1)) {
            case 's', 'S' -> Duration.ofSeconds(i);
            case 'm', 'M' -> Duration.ofMinutes(i);
            case 'h', 'H' -> Duration.ofHours(i);
            default -> throw new IllegalStateException("Unexpected duration: " + getStringValue());
        };
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

    @Override
    public Properties getProperties() {
        return new Properties("configuration")
            .addProperty("configurationName", configurationName)
            .addProperty("displayName", displayName)
            .addProperty("valueType", valueType)
            .addProperty("value", value)
            .addProperty("selectionValues", selectionValues)
            .addProperty("configurationDescription", configurationDescription);
    }
}
