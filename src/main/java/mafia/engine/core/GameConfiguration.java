package mafia.engine.core;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import mafia.engine.config.Configuration;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;
import mafia.engine.util.StreamUtils;

public class GameConfiguration implements PropertyHolder {
    
    @Getter @Setter
    private String version;

    @Getter @Setter
    private Map<String, List<Configuration>> configurations;

    public Configuration getConfiguration(String category, String configurationName) {
        var list = StreamUtils.filter(
            configurations.get(category), 
            c -> c.getConfigurationName().equals(configurationName)
        );
        return list.isEmpty() ? null : list.getFirst();
    }

    public boolean getBooleanConfiguration(String category, String configurationName) {
        return getConfiguration(category, configurationName).getBooleanValue();
    }

    public Duration getDurationConfiguration(String category, String configurationName) {
        return getConfiguration(category, configurationName).getDurationValue();
    }

    @Override
    public Properties getProperties() {
        return new Properties("configuration")
            .addProperty("version", version)
            .addProperty("configurations", configurations);
    }
}
