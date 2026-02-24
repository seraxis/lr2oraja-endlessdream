package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.widget.*;
import bms.player.beatoraja.play.PracticeConfiguration;
import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.List;

public class PlaySettingsWindow extends TiledOptionBasedWindow {

	private final DragFloatWidget hiSpeed = new DragFloatWidget("##HI-SPEED", getPlayConfig()::setHispeed);
	private final DragIntegerWidget greenNumber = new DragIntegerWidget("##Green Number", getPlayConfig()::setDuration);
	private final StringComboWidget hiSpeedFix = new StringComboWidget("##HI-SPEED Fix", new String[] { "OFF", "START BPM", "MAX BPM", "MAIN BPM", "MIN BPM" }, getPlayConfig()::setFixhispeed);
	private final DragFloatWidget hiSpeedMargin = new DragFloatWidget("##HI-SPEED Margin", getPlayConfig()::setHispeedMargin);
	private final CheckboxWidget hiSpeedFixAutoAdjust = new CheckboxWidget("##HI-SPEED Fix Auto Adjust", getPlayConfig()::setHispeedAutoAdjust);
	private final DragIntegerWidget noteDisplayTiming = new DragIntegerWidget("##Note Display Timing", playerConfig::setJudgetiming);
	private final CheckboxWidget noteDisplayTimingAutoAdjust = new CheckboxWidget("##Note Display Timing Auto Adjust", playerConfig::setNotesDisplayTimingAutoAdjust);

	private final CheckboxWidget enableLaneCover = new CheckboxWidget("##Enable Lane Cover", getPlayConfig()::setEnablelanecover);
	private final DragFloatWidget laneCover = new DragFloatWidget("##Lane Cover", getPlayConfig()::setLanecover);
	private final DragFloatWidget laneCoverMarginLow = new DragFloatWidget("##Lane Cover Margin(low)", getPlayConfig()::setLanecovermarginlow);
	private final DragFloatWidget laneCoverMarginHigh = new DragFloatWidget("##Lane Cover Margin(high)", getPlayConfig()::setLanecovermarginhigh);
	private final DragIntegerWidget laneCoverSwitchDuration = new DragIntegerWidget("##Lane Cover Switch Duration", getPlayConfig()::setLanecoverswitchduration);

	private final CheckboxWidget enableLift = new CheckboxWidget("##Enable Lift", getPlayConfig()::setEnablelift);
	private final DragFloatWidget lift = new DragFloatWidget("##Lift", getPlayConfig()::setLift);

	private final CheckboxWidget enableHidden = new  CheckboxWidget("##EnableHidden", getPlayConfig()::setEnablehidden);
	private final DragFloatWidget hidden = new DragFloatWidget("##Hidden", getPlayConfig()::setHidden);

	private final StringComboWidget noteModifier = new StringComboWidget("##Note Modifier", PracticeConfiguration.RANDOM,playerConfig::setRandom);
	private final StringComboWidget longNoteType = new StringComboWidget("##Long Note Type", new String[] { "LONG NOTE", "CHARGE NOTE", "HELL CHARGE NOTE" }, playerConfig::setLongnoteMode);
	private final StringComboWidget gauge = new StringComboWidget("##Gauge", PracticeConfiguration.GAUGE, playerConfig::setGauge);
	private final StringComboWidget gaugeAutoShift = new StringComboWidget("##Gauge Auto shift", new String[] { "NONE", "CONTINUE", "SURVIVAL TO GROOVE","BEST CLEAR","SELECT TO UNDER" }, playerConfig::setGaugeAutoShift);
	private final StringComboWidget bottomShiftableGauge = new StringComboWidget("##Bottom Shiftable gauge", PracticeConfiguration.GAUGE, playerConfig::setBottomShiftableGauge);
	private final ComboWidget<String> targetScore = new ComboWidget<>("##Target Score", playerConfig.getTargetlist(), playerConfig::setTargetid);
	private final CheckboxWidget forceCNEndings = new CheckboxWidget("##CN Endings on LNs", playerConfig::setForcedCNEndings);
	private final CheckboxWidget showJudgeArea = new CheckboxWidget("##Show Judge Area", playerConfig::setShowjudgearea);
	private final CheckboxWidget showPassNotes = new CheckboxWidget("##Show Passed Notes", playerConfig::setShowpastnote);
	private final CheckboxWidget showHiddenNotes = new CheckboxWidget("##Show Hidden Notes", playerConfig::setShowhiddennote);
	private final CheckboxWidget markProcessedNote = new CheckboxWidget("##Mark processed note", playerConfig::setMarkprocessednote);
	private final CheckboxWidget guideSE = new CheckboxWidget("##Guide SE", playerConfig::setGuideSE);
	private final CheckboxWidget windowHold = new CheckboxWidget("##Window Hold", playerConfig::setWindowHold);
	private final CheckboxWidget chartPreview = new CheckboxWidget("##Chart Preview", playerConfig::setChartPreview);
	private final DragIntegerWidget hRandomThreshold = new DragIntegerWidget("##H-Random Threshold", playerConfig::setHranThresholdBPM);
	private final DragIntegerWidget exitPressDuration = new DragIntegerWidget("##Exit Press Duration", playerConfig::setExitPressDuration);

	private final StringComboWidget doubleOption = new StringComboWidget("##Double Option", new String[] { "OFF", "FLIP", "BATTLE", "BATTLE AS" }, playerConfig::setDoubleoption);
	private final StringComboWidget noteModifier2P = new StringComboWidget("##Note Modifier 2P", PracticeConfiguration.RANDOM, playerConfig::setRandom2);

	private final List<Pair<String, List<TiledOption<?>>>> options = List.of(
			Pair.of("Scroll", Arrays.asList(
					new TiledOption<>("HI-SPEED", getPlayConfig()::getHispeed, hiSpeed),
					new TiledOption<>("Green Number", getPlayConfig()::getDuration, greenNumber),
					new TiledOption<>("HI-SPEED Fix", getPlayConfig()::getFixhispeed, hiSpeedFix),
					new TiledOption<>("HI-SPEED Margin", getPlayConfig()::getHispeedMargin, hiSpeedMargin),
					new TiledOption<>("HI-SPEED Fix Auto Adjust", getPlayConfig()::isEnableHispeedAutoAdjust, hiSpeedFixAutoAdjust),
					new TiledOption<>("Note Display Timing", playerConfig::getJudgetiming, noteDisplayTiming),
					new TiledOption<>("Note Display Timing Auto Adjust", playerConfig::isNotesDisplayTimingAutoAdjust, noteDisplayTimingAutoAdjust)
			)),
			Pair.of("Lane Cover", Arrays.asList(
					new TiledOption<>("Enable Lane Cover", getPlayConfig()::isEnablelanecover, enableLaneCover),
					new TiledOption<>("Lane Cover", getPlayConfig()::getLanecover, laneCover),
					new TiledOption<>("Lane Cover Margin(low)", getPlayConfig()::getLanecovermarginlow, laneCoverMarginLow),
					new TiledOption<>("Lane Cover Margin(high)", getPlayConfig()::getLanecovermarginhigh, laneCoverMarginHigh),
					new TiledOption<>("Lane Cover Switch Duration", getPlayConfig()::getLanecoverswitchduration, laneCoverSwitchDuration)
			)),
			Pair.of("Lift", Arrays.asList(
					new TiledOption<>("Enable Lift", getPlayConfig()::isEnablelift, enableLift),
					new TiledOption<>("Lift", getPlayConfig()::getLift, lift)
			)),
			Pair.of("Hidden", Arrays.asList(
					new TiledOption<>("Enable Hidden", getPlayConfig()::isEnablehidden, enableHidden),
					new TiledOption<>("Hidden", getPlayConfig()::getHidden, hidden)
			)),
			Pair.of("Options", Arrays.asList(
					new TiledOption<>("Note Modifier", playerConfig::getRandom, noteModifier),
					new TiledOption<>("Long Note Type", playerConfig::getLongnoteMode, longNoteType),
					new TiledOption<>("Gauge", playerConfig::getGauge, gauge),
					new TiledOption<>("Gauge Auto Shift", playerConfig::getGaugeAutoShift, gaugeAutoShift),
					new TiledOption<>("Bottom Shiftable Gauge", playerConfig::getBottomShiftableGauge, bottomShiftableGauge),
					new TiledOption<>("Target Score", playerConfig::getTargetid, targetScore)
			)),
			Pair.of("DP Options", Arrays.asList(
					new TiledOption<>("DP Option", playerConfig::getDoubleoption, doubleOption),
					new TiledOption<>("Note Modifier(2P)", playerConfig::getRandom2, noteModifier2P)
			)),
			Pair.of("Misc Options", Arrays.asList(
					new TiledOption<>("CN Endings on LNs", playerConfig::isForcedCNEndings, forceCNEndings),
					new TiledOption<>("Show Judge Area", playerConfig::isShowjudgearea, showJudgeArea),
					new TiledOption<>("Show Pass Notes", playerConfig::isMarkprocessednote, showPassNotes),
					new TiledOption<>("Show Hidden Notes", playerConfig::isShowhiddennote, showHiddenNotes),
					new TiledOption<>("Mark Processed Notes", playerConfig::isMarkprocessednote, markProcessedNote),
					new TiledOption<>("Guide SE", playerConfig::isGuideSE, guideSE),
					new TiledOption<>("Window Hold", playerConfig::isWindowHold, windowHold),
					new TiledOption<>("Chart Preview", playerConfig::isChartPreview, chartPreview),
					new TiledOption<>("H-Random Threshold", playerConfig::getHranThresholdBPM, hRandomThreshold),
					new TiledOption<>("Start+Select Exit Delay", playerConfig::getExitPressDuration, exitPressDuration)
			))
	);

	public PlaySettingsWindow(Config config, PlayerConfig playerConfig) {
		super("Play", config, playerConfig);
	}

	@Override
	public List<Pair<String, List<TiledOption<?>>>> getOptions() {
		return options;
	}
}
