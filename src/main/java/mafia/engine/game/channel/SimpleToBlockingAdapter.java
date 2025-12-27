package mafia.engine.game.channel;

public class SimpleToBlockingAdapter<T> {

    private final BlockingChannel<T> blockingChannel;

    public SimpleToBlockingAdapter(
            SimpleChannel<T> simpleChannel,
            BlockingChannel<T> blockingChannel
    ) {
        this.blockingChannel = blockingChannel;

        simpleChannel.subscribe(this::onReceive);
    }

    private void onReceive(T value) {
        if (value == null) {
            return;
        }
        blockingChannel.send(value);
    }
}
