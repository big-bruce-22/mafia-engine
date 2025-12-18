package mafia.engine.game.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValueType {
    DURATION("duration"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    STRING("string");

    private final String type;

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonCreator
    public static ValueType fromString(String value) {
        for (ValueType vt : ValueType.values()) {
            if (vt.type.equalsIgnoreCase(value)) {
                return vt;
            }
        }
        throw new IllegalArgumentException("Unknown value type: " + value);
    }
}