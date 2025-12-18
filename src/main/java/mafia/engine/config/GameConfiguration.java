package mafia.engine.config;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import mafia.engine.game.configuration.Configuration;
import mafia.engine.util.StreamUtils;

public class GameConfiguration {
    
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
}
