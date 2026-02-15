package bms.player.beatoraja.skin.lr2;

import java.io.*;
import java.util.function.BiConsumer;

import bms.player.beatoraja.skin.lr2.commands.DestinationBpmChart;
import bms.player.beatoraja.skin.lr2.commands.DestinationGaugeChart;
import bms.player.beatoraja.skin.lr2.commands.DestinationNoteChart;
import bms.player.beatoraja.skin.lr2.commands.DestinationTimingChart;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.*;

import bms.player.beatoraja.*;
import bms.player.beatoraja.result.*;
import bms.player.beatoraja.skin.*;

/**
 * LR2リザルトスキン読み込み用クラス
 *
 * @author exch
 */
public class LR2ResultSkinLoader extends LR2SkinCSVLoader<MusicResultSkin> {

	Rectangle gauge = new Rectangle();
	SkinGaugeGraphObject gaugeobj;
	SkinNoteDistributionGraph noteobj;
	SkinBPMGraph bpmgraphobj;
	SkinTimingDistributionGraph timinggraphobj;

	public LR2ResultSkinLoader(final Resolution src, final Config c) {
		super(src, c);
		addCommandWord(ResultCommand.values());
	}

	public MusicResultSkin loadSkin(MainState state, SkinHeader header, IntIntMap option) throws IOException {
		return this.loadSkin(new MusicResultSkin(header), state, option);
	}

}

enum ResultCommand implements LR2SkinLoader.Command<LR2ResultSkinLoader> {
	
	STARTINPUT ((loader, str) -> {
		loader.skin.setInput(Integer.parseInt(str[1]));
		loader.skin.setRankTime(Integer.parseInt(str[2]));
	}),
	SRC_GAUGECHART_1P ((loader, str) -> {
		int[] values = loader.parseInt(str);
		loader.gaugeobj = new SkinGaugeGraphObject();
		loader.gaugeobj.setLineWidth(values[6]);
		loader.gaugeobj.setDelay(values[14] - values[13]);
		loader.gauge = new Rectangle(0, 0, values[11], values[12]);
		loader.skin.add(loader.gaugeobj);
	}),
	DST_GAUGECHART_1P ((loader, str) -> {
		DestinationGaugeChart dst = LR2CommandParser.getInstance().parse(str);
		loader.gauge.x = dst.x();
		loader.skin.setDestination(loader.gaugeobj, dst.time, loader.gauge.x, loader.gauge.y, loader.gauge.width, loader.gauge.height, dst.acc,
				dst.a(), dst.r(), dst.g(), dst.b(), dst.blend, dst.filter, dst.angle,
				dst.center, dst.loop, dst.timer, dst.op1(), dst.op2(), dst.op3(), new int[]{dst.op4()});
	}),
	SRC_NOTECHART_1P ((loader, str) -> {
		//#SRC_NOTECHART_1P,(index),(gr),(x),(y),(w),(h),(div_x),(div_y),(cycle),(timer),field_w,field_h,(start),(end),delay,backTexOff,orderReverse,noGap
		int[] values = loader.parseInt(str);
		loader.noteobj = new SkinNoteDistributionGraph(values[1], values[15], values[16], values[17], values[18], values[19]);
		loader.gauge = new Rectangle(0, 0, values[11], values[12]);
		loader.skin.add(loader.noteobj);
	}),
	DST_NOTECHART_1P ((loader, str) -> {
		DestinationNoteChart dst = LR2CommandParser.getInstance().parse(str);
		loader.gauge.x = dst.x();
		loader.gauge.y = loader.src.height - dst.y();
		loader.skin.setDestination(loader.noteobj, dst.time, loader.gauge.x, loader.gauge.y, loader.gauge.width, loader.gauge.height, dst.acc,
				dst.a(), dst.r(), dst.g(), dst.b(), dst.blend, dst.filter, dst.angle,
				dst.center, dst.loop, dst.timer, dst.op1(), dst.op2(), dst.op3(), new int[]{dst.op4()});
	}),
	SRC_BPMCHART ((loader, str) -> {
		//#SRC_BPMCHART, field_w, field_h, delay, lineWidth, mainBPMColor, minBPMColor, maxBPMColor, otherBPMColor, stopLineColor, transitionLineColor
		int[] values = loader.parseInt(str);
		loader.bpmgraphobj = new SkinBPMGraph(values[3], values[4], str[5], str[6], str[7], str[8], str[9], str[10]);
		loader.gauge = new Rectangle(0, 0, values[1], values[2]);
		loader.skin.add(loader.bpmgraphobj);
	}),
	DST_BPMCHART ((loader, str) -> {
		DestinationBpmChart dst = LR2CommandParser.getInstance().parse(str);
		loader.gauge.x = dst.x();
		loader.gauge.y = loader.src.height - dst.y();
		loader.skin.setDestination(loader.bpmgraphobj, dst.time, loader.gauge.x, loader.gauge.y, loader.gauge.width, loader.gauge.height, dst.acc,
				dst.a(), dst.r(), dst.g(), dst.b(), dst.blend, dst.filter, dst.angle,
				dst.center, dst.loop, dst.timer, dst.op1(), dst.op2(), dst.op3(), new int[]{dst.op4()});
	}),
	SRC_TIMINGCHART_1P ((loader, str) -> {
		//#SRC_TIMINGCHART_1P,(index),(gr),(x),width,height,lineWidth,graphColor,averageColor,devColor,PGColor,GRColor,GDColor,BDColor,PRColor,drawAverage,drawDev
		int[] values = loader.parseInt(str);
		loader.timinggraphobj = new SkinTimingDistributionGraph(values[4], values[6], str[7], str[8], str[9], str[10], str[11], str[12], str[13], str[14], values[15], values[16]);
		loader.gauge = new Rectangle(0, 0, values[4], values[5]);
		loader.skin.add(loader.timinggraphobj);
	}),
	DST_TIMINGCHART_1P ((loader, str) -> {
		DestinationTimingChart dst = LR2CommandParser.getInstance().parse(str);
		loader.gauge.x = dst.x();
		loader.gauge.y = loader.src.height - dst.y();
		loader.skin.setDestination(loader.timinggraphobj, dst.time, loader.gauge.x, loader.gauge.y, loader.gauge.width, loader.gauge.height, dst.acc,
				dst.a(), dst.r(), dst.g(), dst.b(), dst.blend, dst.filter, dst.angle,
				dst.center, dst.loop, dst.timer, dst.op1(), dst.op2(), dst.op3(), new int[]{dst.op4()});
	});

	public final BiConsumer<LR2ResultSkinLoader, String[]> function;
	
	private ResultCommand(BiConsumer<LR2ResultSkinLoader, String[]> function) {
		this.function = function;
	}
	
	public void execute(LR2ResultSkinLoader loader, String[] str) {
		function.accept(loader, str);
	}
}
