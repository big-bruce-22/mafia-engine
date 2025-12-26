package mafia.engine.game.context;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.Setter;
import mafia.engine.game.event.NightEvent;

public class MorningContext implements ChannelContext<List<NightEvent>> {

    @NonNull @Setter
    private List<NightEvent> contexts = new ArrayList<>();

    public void addEvent(NightEvent event) {
        contexts.add(event);
    }

    @Override
    public List<NightEvent> getContext() {
        return contexts;
    }    
}
