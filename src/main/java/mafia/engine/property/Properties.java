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

}
