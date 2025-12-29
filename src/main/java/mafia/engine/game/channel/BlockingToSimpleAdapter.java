package mafia.engine.game.channel;

public class BlockingToSimpleAdapter<T> {

    private final BlockingChannel<T> blockingChannel;
    private final SimpleChannel<T> simpleChannel;

    private volatile boolean running = true;

    public BlockingToSimpleAdapter(
        BlockingChannel<T> blockingChannel,
        SimpleChannel<T> simpleChannel
    ) {
        this.blockingChannel = blockingChannel;
        this.simpleChannel = simpleChannel;
        startPump();
    }

    private void startPump() {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    // blocks until engine sends something
                    T value = blockingChannel.receive();

                    // synchronous publish (listeners handled here)
                    simpleChannel.publish(value);

                    // once publish() returns → automatically move on
                } catch (Exception e) {
                    break;
                }
            }
        }, "BlockingToSimpleAdapter");

        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
    }
}
