package mafia.engine.game.channel;

@FunctionalInterface
public interface ChannelListener<T> {
    void onReceive(T message);
}
