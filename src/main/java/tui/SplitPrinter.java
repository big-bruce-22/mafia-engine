package tui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

public class SplitPrinter {

    private static Map<String, Queue<Pair<String, String>>> buffers = new HashMap<>();

    public static volatile boolean quickPrint = true;

    public static void println(String taskName) {
        if (quickPrint) {
            System.out.println();
        }
        buffers.putIfAbsent(taskName, new LinkedList<>());
        buffers.get(taskName).offer(Pair.of("println", ""));
    }

    public static void println(String taskName, Object obj) {
        if (quickPrint) {
            System.out.println(obj);
        }
        buffers.putIfAbsent(taskName, new LinkedList<>());
        buffers.get(taskName).offer(Pair.of("println", obj.toString()));
    }

    public static void println(String taskName, String message) {
        if (quickPrint) {
            System.out.println(message);
        }
        buffers.putIfAbsent(taskName, new LinkedList<>());
        buffers.get(taskName).offer(Pair.of("println", message));
    }

    public static void print(String taskName, String message) {
        if (quickPrint) {
            System.out.print(message);
        }
        buffers.putIfAbsent(taskName, new LinkedList<>());
        buffers.get(taskName).offer(Pair.of("print", message));
    }

    public static void printf(String taskName, String format, Object... args) {
        if (quickPrint) {
            System.out.printf(format, args);
        }
        buffers.putIfAbsent(taskName, new LinkedList<>());
        buffers.get(taskName).offer(Pair.of("print", String.format(format, args)));
    }

    public static void printAll() {
        for (var entrySet : buffers.entrySet()) {
            var buffer = entrySet.getValue();
            while (!buffer.isEmpty()) {
                var action = buffer.poll();
                switch (action.getLeft()) {
                    case "print" -> System.out.print(action.getRight());
                    case "println" -> System.out.println(action.getRight());
                }
            }
        }
    }

    public static void printFlush(String taskName) {
        buffers.putIfAbsent(taskName, new LinkedList<>());
        var buffer = buffers.get(taskName);
        while (!buffer.isEmpty()) {
            var action = buffer.poll();
            if (action.getLeft().equals("print")) {
                System.out.print(action.getRight());
            } else if (action.getLeft().equals("println")) {
                System.out.println(action.getRight());
            }
        }
    }
}
