package mafia.engine.game.phase;

import java.util.ArrayList;
import java.util.List;

import mafia.engine.role.RoleReveal;

public class RoleRevealPhaseContext implements PhaseContext<List<RoleReveal>> {

    private final List<RoleReveal> reveals = new ArrayList<>();

    public void addReveal(RoleReveal reveal) {
        reveals.add(reveal);
    }

    @Override
    public List<RoleReveal> getResult() {
        return reveals;
    }
}
