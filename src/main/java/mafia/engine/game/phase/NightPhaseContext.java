package mafia.engine.game.phase;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import mafia.engine.player.action.PlayerActionContext;

public class NightPhaseContext implements PhaseContext<Void> {

    @Getter @Setter
    private List<PlayerActionContext> contexts = new ArrayList<>();

    public void addContext(PlayerActionContext ctx) {
        contexts.add(ctx);
    }

    @Override
    public Void getResult() {
        return null;
    }
    
}
