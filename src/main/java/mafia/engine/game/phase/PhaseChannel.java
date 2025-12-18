package mafia.engine.game.phase;

import lombok.NonNull;

public class PhaseChannel<T> {

    @NonNull
    private volatile T context;

    public void send(T context) {
        this.context = context;
    }

    public boolean hasSent() {
        return context != null;
    }

    public T receive() {
        return context;
    }

    public void clear() {
        context = null;        
    }
}
