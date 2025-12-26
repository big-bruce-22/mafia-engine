package mafia.engine.game.channel;

public interface Channel<T> {

    void send(T value);

    T receive();

    boolean hasSent();

    void clear();
}