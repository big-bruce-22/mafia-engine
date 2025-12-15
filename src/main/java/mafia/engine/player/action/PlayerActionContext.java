package mafia.engine.player.action;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import mafia.engine.ability.Ability;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class PlayerActionContext {

    @NonNull @Getter @Setter
    private Player actor, target;

    @NonNull @Getter @Setter
    private Ability ability;

    @Getter
    private boolean cancelled;

    @NonNull @Getter @Setter
    private PlayerActionResult playerActionResult;

    public void cancel() {
        cancelled = true;
    }
}
