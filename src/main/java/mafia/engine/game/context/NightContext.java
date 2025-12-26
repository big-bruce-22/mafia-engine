package mafia.engine.game.context;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import mafia.engine.player.action.PlayerActionContext;

public class NightContext implements ChannelContext<List<PlayerActionContext>> {

    @Setter
    private List<PlayerActionContext> contexts = new ArrayList<>();

    public void addContext(PlayerActionContext ctx) {
        contexts.add(ctx);
    }

    @Override
    public List<PlayerActionContext> getContext() {
        return contexts;
    }
}
