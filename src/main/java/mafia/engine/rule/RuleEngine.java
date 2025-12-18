package mafia.engine.rule;

import mafia.engine.game.event.GameEvent;
import mafia.engine.player.action.PlayerActionContext;

public class RuleEngine {
    
    public void process(GameEvent event, PlayerActionContext context) {
        switch (event) {
            case BEFORE_ABILITY -> {
                switch (context.ability().getAction()) {
                    default -> {}
                }
                // Implement rules to be processed before an ability is executed
            }
            case AFTER_ABILITY -> {
                var ability = context.ability();
                var actionResultData = context.playerActionResult().data();
                switch (ability.getAction()) {
                    case KILL -> {
                        // Example rule: If the target has a protective role, cancel the kill
                        // This is just a placeholder for actual rule logic
                    }
                    case HEAL -> {
                        // Example rule: If the target is already dead, cancel the heal
                        // This is just a placeholder for actual rule logic
                    }
                    case INVESTIGATE -> {
                        switch (String.valueOf(ability.properties().get("revealInvestigation"))) {
                            case "side" -> {
                                actionResultData
                                    .put(
                                        "investigationResult", 
                                        context.target().role().getAlignment()
                                    );
                            }
                            case "role" -> {
                                actionResultData
                                    .put(
                                        "investigationResult", 
                                        context.target().role()
                                    );
                            }
                        }    
                    }
                    default -> throw new IllegalArgumentException("Unexpected value: " + ability.getAction());
                }
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + event);
        }
    }
}
