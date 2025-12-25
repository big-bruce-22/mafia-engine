package mafia.engine.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class GameRules {

    @Getter @Setter
    private String version;

    @Getter @Setter
    private Map<String, List<String>> rules = new HashMap<>();

    public GameRules addRule(String category, String rule) {
        rules.putIfAbsent(category, new ArrayList<>());
        rules.get(category).add(rule);
        return this;
    }

    public GameRules addRules(String category, String... rules) {
        for (var r : rules) {
            addRule(category, r);
        }
        return this;
    }

    public GameRules addRules(String category, List<String> rules) {
        for (var r : rules) {
            addRule(category, r);
        }
        return this;
    }

    public List<String> getRules(String category) {
        return rules.get(category);
    }

    public boolean removeRule(String category, String rule) {
        return rules.get(category).remove(rule);
    }   
}
