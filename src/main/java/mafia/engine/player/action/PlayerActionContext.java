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

    public PlayerActionContext(Player actor, Player target) {
        this.actor = actor;
        this.target = target;
    }

    public PlayerActionContext(Player actor, Player target, Ability ability) {
        this.actor = actor;
        this.target = target;
        this.ability = ability;
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public String toString() {
        return "PlayerActionContext [actor=" + actor.name() + ", target=" + target.name() + ", ability=" + ability.name() + ", cancelled=" + cancelled + "]";
    }
}
