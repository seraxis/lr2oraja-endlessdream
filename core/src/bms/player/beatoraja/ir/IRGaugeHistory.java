package bms.player.beatoraja.ir;

import com.badlogic.gdx.utils.FloatArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pojo class for sending gauge data to IR service, gauge data are an array of numbers like [100.0, 100.0, 89.0, ...]
 * TODO: For gauges that cannot recover from death (e.g. hard, ex-hard), we can delete the elements
 * after death to reduce the packet size sent to server
 */
public record IRGaugeHistory(List<Float> easy, List<Float> groove, List<Float> hard, List<Float> exhard) {
	/**
	 * Convert gaugelog from BmsPlayer
	 *
	 * @implNote gaugelog is a fixed length 2d-array, see GaugeProperty for first dimension's arrangement
	 * TODO: We're relying on the fact that currently every GaugeProperty has same gauge types definition
	 * @see bms.player.beatoraja.play.BMSPlayer
	 */
	public static IRGaugeHistory fromGaugeLog(FloatArray[] gaugeLog) {
		if (gaugeLog == null) {
			return new IRGaugeHistory(emptyGaugeData(), emptyGaugeData(), emptyGaugeData(), emptyGaugeData());
		}
		return new IRGaugeHistory(
				gaugeLog.length > 1 ? convert(gaugeLog[1]) : emptyGaugeData(),
				gaugeLog.length > 2 ? convert(gaugeLog[2]) : emptyGaugeData(),
				gaugeLog.length > 3 ? convert(gaugeLog[3]) : emptyGaugeData(),
				gaugeLog.length > 4 ? convert(gaugeLog[4]) : emptyGaugeData()
		);
	}

	private static List<Float> convert(FloatArray floatArray) {
		List<Float> ret = new ArrayList<>();
		if (floatArray == null || floatArray.items == null) {
			return ret;
		}
		for (float item : floatArray.items) {
			ret.add(item);
		}
		return ret;
	}

	private static List<Float> emptyGaugeData() {
		return Collections.singletonList(0f);
	}
}
