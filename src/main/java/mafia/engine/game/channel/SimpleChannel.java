package mafia.engine.game.channel;

import java.util.ArrayList;
import java.util.List;

public class SimpleChannel<T> {

    private final List<ChannelListener<T>> listeners = new ArrayList<>();

    public void publish(T message) {
        for (ChannelListener<T> listener : listeners) {
            listener.onReceive(message);
        }
    }

    public void subscribe(ChannelListener<T> listener) {
        listeners.add(listener);
    }
}
