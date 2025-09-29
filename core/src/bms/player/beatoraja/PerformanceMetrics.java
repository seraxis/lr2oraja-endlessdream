package bms.player.beatoraja;

import java.util.Vector;
import java.util.Stack;

public class PerformanceMetrics {
    private static PerformanceMetrics self = new PerformanceMetrics();

    public static PerformanceMetrics get() { return self; }

    public EventBlock Event(String eventName) { return new EventBlock(eventName); }

    public record EventResult(String name, int id, int parent, long startTime, long duration) {}
    public Vector<EventResult> eventResults = new Vector<EventResult>();

    public Stack<Integer> activeBlocks = new Stack<Integer>();

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
}
