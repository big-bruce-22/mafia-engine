package mafia.engine.game.phase;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.Setter;
import mafia.engine.game.event.NightEvent;

public class MorningPhaseContext implements PhaseContext<List<NightEvent>> {

    @NonNull @Setter
    private List<NightEvent> contexts = new ArrayList<>();

    public void addEvent(NightEvent event) {
        contexts.add(event);
    }

    @Override
    public List<NightEvent> getResult() {
        return contexts;
    }    
}
