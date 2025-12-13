package mafia.engine.game;

import java.util.Map;

public class GameConfiguration {

    private Map<String, Object> configurations;

    public int getIntConfig(String configurationName) {
        return Integer.parseInt(getStringConfig(configurationName));
    }

    public String getStringConfig(String configurationName) {
        return String.valueOf(configurations.get(configurationName));
    }

    public boolean getBoolConfig(String configurationName) {
        return Boolean.parseBoolean(getStringConfig(configurationName));
    }
}
