package mafia.engine.property;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
public class Properties {

    @Getter
    private final String propertyName;

    @Getter
    private Map<String, Object> properties = new HashMap<>();

    public Properties addProperty(String propertyName, Object property) {
        properties.put(propertyName, property);
        return this;
    }
    
    public Properties addProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public Object removeProperty(String propertyName) {
        return properties.remove(propertyName);
    }
        
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    public boolean containsProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }


    @Override
    public String toString() {
        return """
            Properties{
                name = %s,
                properties = %s    
            }
            """
            .formatted(propertyName, formatObject(properties, 1));
    }

    private String formatObject(Object obj, int indentLevel) {
        String indent = "    ".repeat(indentLevel);

        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{\n");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append(indent).append(entry.getKey()).append(" = ")
                .append(formatObject(entry.getValue(), indentLevel + 1))
                .append(",\n");
            }
            sb.append("    ".repeat(indentLevel - 1)).append("}");
            return sb.toString();
        } else if (obj instanceof Iterable<?> iterable) {
            StringBuilder sb = new StringBuilder("[\n");
            for (Object item : iterable) {
                sb.append(indent).append(formatObject(item, indentLevel + 1)).append(",\n");
            }
            sb.append("    ".repeat(indentLevel - 1)).append("]");
            return sb.toString();
        } else if (obj instanceof Properties nestedProp) {
            return nestedProp.toString().replaceAll("(?m)^", indent); // indent nested Properties
        } else {
            return obj.toString();
        }
    }

}
