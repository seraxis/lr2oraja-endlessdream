package bms.player.beatoraja;

import java.util.Vector;
import java.util.Stack;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Set;
import javafx.util.Pair;

public class PerformanceMetrics {
    private static PerformanceMetrics self = new PerformanceMetrics();

    public static PerformanceMetrics get() { return self; }

    // should make this thread safe
    public EventBlock Event(String eventName) { return new EventBlock(eventName); }

    // thread safe
    // relies on name identity, so only pass string literals as the name argument
    public WatchBlock Watch(String eventName) { return new WatchBlock(eventName); }

    public record EventResult(String name, int id, int parent, long startTime, long duration) {}

    public Vector<EventResult> eventResults = new Vector<EventResult>();

    public Stack<Integer> activeBlocks = new Stack<Integer>();

    private IdentityHashMap<String, ArrayDeque<Pair<Long, Long>>> watchRecords =
        new IdentityHashMap<String, ArrayDeque<Pair<Long, Long>>>();

    public synchronized void submitWatchResult(String name, long time, long duration) {
        if (!watchRecords.containsKey(name)) {
            watchRecords.put(name, new ArrayDeque<Pair<Long, Long>>());
        }
        watchRecords.get(name).add(new Pair<Long, Long>(time, duration));
    }

    public synchronized void commit() {
        long now = System.nanoTime();
        long keep = now - 3000000000L;
        // drop all measurements older than 3s (we could make this configurable per watch name)
        watchRecords.forEach((k, v) -> {
            // might want to split these into two arrays to simply the monitor
            while (!v.isEmpty() && v.peek().getKey() < keep) {
                v.pop();
            }
        });
    }

	// TODO: this isn't a great interface for plotting the performance
    public synchronized Set<String> getWatchNames() { return watchRecords.keySet(); }
    public synchronized ArrayDeque<Pair<Long, Long>> getWatchRecords(String name) {
        return watchRecords.get(name);
    }

    public class EventBlock implements AutoCloseable {
        private final String name;
        private final int id;
        private final int parent;
        private final long startTime;

        private static int nextId = 1;

        public EventBlock(String name) {
            this.name = name;
            this.id = nextId;
            nextId++;
            parent = get().activeBlocks.empty() ? 0 : get().activeBlocks.peek();
            get().activeBlocks.push(this.id);
            startTime = System.nanoTime();
        }

        @Override
        public void close() {
            var endTime = System.nanoTime();
            get().activeBlocks.pop();
            eventResults.add(new EventResult(name, id, parent, startTime, (endTime - startTime)));
        }
    }

    public class WatchBlock implements AutoCloseable {
        private final String name;
        private final long startTime;
        public WatchBlock(String name) {
            this.name = name;
            startTime = System.nanoTime();
        }

        @Override
        public void close() {
            var endTime = System.nanoTime();
            get().submitWatchResult(name, startTime, endTime - startTime);
        }
    }
}
