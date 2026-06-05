package bms.player.beatoraja.modmenu;

import bms.model.Mode;
import bms.player.beatoraja.play.JudgeResult;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Track the current judge count
 */
public class JudgeCountTracker {
	// column count
	private final int columnCount;
	private final int[] scratchKeys;
	// (column, judge) -> count
	private final Map<Pair<Integer, JudgeResult>, AtomicInteger> counts = new ConcurrentHashMap<>();
	// column -> (fast, slow)
	private final Map<Integer, Pair<AtomicInteger, AtomicInteger>> fsCounts = new ConcurrentHashMap<>();

	public JudgeCountTracker(Mode playMode) {
		this.columnCount = playMode.key;
		this.scratchKeys = playMode.scratchKey;
		for (int i = 0; i < columnCount; i++) {
			for (JudgeResult judge : JudgeResult.values()) {
				counts.put(Pair.of(i, judge), new AtomicInteger());
			}
			fsCounts.put(i, Pair.of(new AtomicInteger(), new AtomicInteger()));
		}
	}

	public void track(int lane, int judge, boolean fast, int count) {
		JudgeResult judgeResult = JudgeResult.valueOf(judge, fast);
		counts.get(Pair.of(lane, judgeResult)).addAndGet(count);
		// TODO: Allow user define their own interval?
		if (judgeResult != JudgeResult.EARLY_PGREAT && judgeResult != JudgeResult.LATE_PGREAT) {
			Pair<AtomicInteger, AtomicInteger> fs = fsCounts.get(lane);
			if (fast) {
				fs.getLeft().incrementAndGet();
			} else {
				fs.getRight().incrementAndGet();
			}
		}
	}

	public int getCount(int lane, JudgeResult judge) {
		return counts.get(Pair.of(lane, judge)).get();
	}

	public int getFastCount(int lane) {
		return fsCounts.get(lane).getLeft().get();
	}

	public int getSlowCount(int lane) {
		return fsCounts.get(lane).getRight().get();
	}

	public int getColumnCount() {
		return columnCount;
	}

	public int[] getScratchKeys() {
		return scratchKeys;
	}
}
