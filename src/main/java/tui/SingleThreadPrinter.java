package tui;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class SingleThreadPrinter {

    private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private static final Thread printerThread;

    private static final Runnable POISON_PILL = () -> {};

    static {
        printerThread = new Thread(() -> {
            try {
                while (true) {
                    Runnable task = queue.take();
                    if (task == POISON_PILL) break;
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "single-thread-printer");

        printerThread.setDaemon(true);
        printerThread.start();
    }

    private SingleThreadPrinter() {}

    /* ---------------- BASIC ---------------- */

    public static void println() {
        queue.offer(() -> System.out.println());
    }

    public static void println(Object obj) {
        queue.offer(() -> System.out.println(obj));
    }

    public static void println(String message) {
        queue.offer(() -> System.out.println(message));
    }

    public static void print(String message) {
        queue.offer(() -> System.out.print(message));
    }

    /* ---------------- FORMATTED ---------------- */

    public static void printf(String format, Object... args) {
        queue.offer(() -> System.out.printf(format, args));
    }

    /* ---------------- ATOMIC BLOCK ---------------- */

    /**
     * Ensures multiple print calls execute together without interleaving
     */
    public static void atomic(Runnable runnable) {
        queue.offer(runnable);
    }

    /* ---------------- SHUTDOWN ---------------- */

    public static void shutdown() {
        queue.offer(POISON_PILL);
    }
}
