package mafia.engine.util;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer {

    private static final Writer instance = new Writer();

    public static Writer getInstance() {
        return instance;
    }

    public BlockingQueue<String> consoleQueue = new LinkedBlockingQueue<>();

    public Writer() {
        // Logger thread
        new Thread(() -> {
            while (true) {
                try {
                    System.out.print(consoleQueue.take());
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }
}