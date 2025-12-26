package mafia.engine.game.context;

import java.util.ArrayList;
import java.util.List;

import mafia.engine.role.RoleReveal;

public class RoleRevealContext implements ChannelContext<List<RoleReveal>> {

    private final List<RoleReveal> reveals = new ArrayList<>();

    public void addReveal(RoleReveal reveal) {
        reveals.add(reveal);
    }

    @Override
    public List<RoleReveal> getContext() {
        return reveals;
    }
}
