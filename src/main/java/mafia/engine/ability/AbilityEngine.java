package mafia.engine.ability;

import mafia.engine.context.Context;

public class AbilityEngine {
    
    public void registerAction(Context context) {
        if (context.cancelled()) {
            return;
        }

        var target = context.target();
        var action = context.ability();

        target.incrementAttemptedAction(action.getAction());
    }
}
