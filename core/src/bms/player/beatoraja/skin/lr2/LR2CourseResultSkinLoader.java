package bms.player.beatoraja.skin.lr2;

import java.io.IOException;
import java.util.function.BiConsumer;

import bms.player.beatoraja.*;
import bms.player.beatoraja.result.CourseResultSkin;
import bms.player.beatoraja.skin.lr2.commands.DestinationGaugeChart;
import bms.player.beatoraja.skin.lr2.commands.DestinationNoteChart;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntIntMap;

import bms.player.beatoraja.result.SkinGaugeGraphObject;
import bms.player.beatoraja.skin.SkinHeader;
import bms.player.beatoraja.skin.SkinNoteDistributionGraph;

/**
 * LR2コースリザルトスキン読み込み用クラス
 *
 * @author exch
 */
public class LR2CourseResultSkinLoader extends LR2SkinCSVLoader<CourseResultSkin> {

	Rectangle gauge = new Rectangle();
	SkinGaugeGraphObject gaugeobj;
	SkinNoteDistributionGraph noteobj;

	public LR2CourseResultSkinLoader(final Resolution src, final Config c) {
		super(src, c);
		addCommandWord(CourseCommand.values());
	}

	public CourseResultSkin loadSkin(MainState state, SkinHeader header, IntIntMap option) throws IOException {
		return this.loadSkin(new CourseResultSkin(header), state, option);
	}

}

enum CourseCommand implements LR2SkinLoader.Command<LR2CourseResultSkinLoader> {
	
	STARTINPUT((loader, str) -> {
		loader.skin.setInput(Integer.parseInt(str[1]));
		loader.skin.setRankTime(Integer.parseInt(str[2]));
	}),
	SRC_GAUGECHART_1P((loader, str) -> {
		int[] values = loader.parseInt(str);
		loader.gaugeobj = new SkinGaugeGraphObject();
		loader.gaugeobj.setLineWidth(values[6]);
		loader.gaugeobj.setDelay(values[14] - values[13]);
		loader.gauge = new Rectangle(0, 0, values[11], values[12]);
		loader.skin.add(loader.gaugeobj);
	}),
	DST_GAUGECHART_1P((loader, str) -> {
		DestinationGaugeChart dst = LR2CommandParser.getInstance().parse(str);
		loader.gauge.x = dst.x();
		loader.skin.setDestination(loader.gaugeobj, dst.time, loader.gauge.x, loader.gauge.y, loader.gauge.width, loader.gauge.height, dst.acc,
				dst.a(), dst.r(), dst.g(), dst.b(), dst.blend, dst.filter, dst.angle,
				dst.center, dst.loop, dst.timer, dst.op1(), dst.op2(), dst.op3(), new int[]{dst.op4()});
	}),
	SRC_NOTECHART_1P((loader, str) -> {
		// #SRC_NOTECHART_1P,(index),(gr),(x),(y),(w),(h),(div_x),(div_y),(cycle),(timer),field_w,field_h,(start),(end),delay,backTexOff,orderReverse,noGap
		int[] values = loader.parseInt(str);
		loader.noteobj = new SkinNoteDistributionGraph(values[1], values[15], values[16], values[17], values[18], values[19]);
		loader.gauge = new Rectangle(0, 0, values[11], values[12]);
		loader.skin.add(loader.noteobj);
	}),
	DST_NOTECHART_1P((loader, str) -> {
		DestinationNoteChart dst = LR2CommandParser.getInstance().parse(str);
		loader.gauge.x = dst.x();
		loader.gauge.y = loader.src.height - dst.y();
		loader.skin.setDestination(loader.noteobj, dst.time, loader.gauge.x, loader.gauge.y, loader.gauge.width, loader.gauge.height, dst.acc,
				dst.a(), dst.r(), dst.g(), dst.b(), dst.blend, dst.filter, dst.angle,
				dst.center, dst.loop, dst.timer, dst.op1(), dst.op2(), dst.op3(), new int[]{dst.op4()});
	});

	public final BiConsumer<LR2CourseResultSkinLoader, String[]> function;

	private CourseCommand(BiConsumer<LR2CourseResultSkinLoader, String[]> function) {
		this.function = function;
	}

	public void execute(LR2CourseResultSkinLoader loader, String[] str) {
		function.accept(loader, str);
	}

}
