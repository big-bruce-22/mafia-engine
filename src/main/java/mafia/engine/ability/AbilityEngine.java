package mafia.engine.ability;

import mafia.engine.player.action.PlayerActionContext;

public class AbilityEngine {
    
    public void registerAction(PlayerActionContext context) {
        if (context.cancelled()) {
            return;
        }

        var target = context.target();
        var action = context.ability();

        target.incrementAttemptedAction(action.getAction());
    }
}
