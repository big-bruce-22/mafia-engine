package mafia.engine.game.channel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingChannel<T> implements Channel<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    
    @Override
    public void send(T value) {
        // System.out.println("BlockingChannel: sending value " + value);
        if (value == null) {
            throw new IllegalArgumentException("Channel cannot send null");
        }
        queue.offer(value);
    }

    @Override
    public T receive() {
        try {
            // System.out.println("BlockingChannel: receive value " + queue.peek());
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Channel receive interrupted", e);
        }
    }

    @Override
    public boolean hasSent() {
        return !queue.isEmpty();
    }

    @Override
    public void clear() {
        queue.clear();
    }
}
