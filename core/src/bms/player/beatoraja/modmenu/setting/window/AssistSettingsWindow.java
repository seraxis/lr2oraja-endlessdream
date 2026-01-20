package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.widget.CheckboxWidget;
import bms.player.beatoraja.modmenu.setting.widget.DragIntegerWidget;
import bms.player.beatoraja.modmenu.setting.widget.Label;
import bms.player.beatoraja.modmenu.setting.widget.TiledOption;
import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.List;

public class AssistSettingsWindow extends TiledOptionBasedWindow {

	private final CheckboxWidget enableConstant = new CheckboxWidget("##Enable Constant", getPlayConfig()::setEnableConstant);
	private final DragIntegerWidget constantFadeInTime = new DragIntegerWidget("##Constant fade-in time", getPlayConfig()::setConstantFadeinTime);
	private final CheckboxWidget bpmGuide = new CheckboxWidget("##BPM Guide", playerConfig::setBpmguide);

	private final CheckboxWidget enableExpandJudge = new CheckboxWidget("##Enable Expand Judge", playerConfig::setCustomJudge);
	private final DragIntegerWidget expandJudgeKeyPG = new DragIntegerWidget("##Expand Judge Key PG", playerConfig::setKeyJudgeWindowRatePerfectGreat);
	private final DragIntegerWidget expandJudgeKeyGR = new DragIntegerWidget("##Expand Judge Key GR", playerConfig::setKeyJudgeWindowRateGreat);
	private final DragIntegerWidget expandJudgeKeyGD = new DragIntegerWidget("##Expand Judge Key GD", playerConfig::setKeyJudgeWindowRateGood);
	private final DragIntegerWidget expandJudgeScratchPG = new DragIntegerWidget("##Expand Judge Scratch PG", playerConfig::setScratchJudgeWindowRatePerfectGreat);
	private final DragIntegerWidget expandJudgeScratchGR = new DragIntegerWidget("##Expand Judge Scratch GR", playerConfig::setScratchJudgeWindowRateGreat);
	private final DragIntegerWidget expandJudgeScratchGD = new DragIntegerWidget("##Expand Judge Scratch GD", playerConfig::setScratchJudgeWindowRateGood);

	private final List<Pair<String, List<TiledOption<?>>>> options = Arrays.asList(
			Pair.of("Assist Options", Arrays.asList(
					new TiledOption<>("Enable Constant", getPlayConfig()::isEnableConstant, enableConstant).addIcon(
							Label.defaultAssistIconLabel()
					).addDescription(
							"The notes fade-in time would be a constant value instead of being affected by bpm"
					),
					new TiledOption<>("Constant fade-in time", getPlayConfig()::getConstantFadeinTime, constantFadeInTime).addIcon(
							Label.defaultAssistIconLabel()
					),
					new TiledOption<>("BPM Guide", playerConfig::isBpmguide, bpmGuide).addIcon(
							Label.defaultAssistIconLabel()
					)
			)),
			Pair.of("Expand Judge", Arrays.asList(
					new TiledOption<>("Enable Expand Judge", playerConfig::isCustomJudge, enableExpandJudge).addIcon(
							Label.assistIconLabel("Your play would be restricted to assist clear if this option is flagged and any timing window below is larger than 100%", 240F)
					),
					new TiledOption<>("Key Perfect Great", playerConfig::getKeyJudgeWindowRatePerfectGreat, expandJudgeKeyPG),
					new TiledOption<>("Key Great", playerConfig::getKeyJudgeWindowRateGreat, expandJudgeKeyGR),
					new TiledOption<>("Key Good", playerConfig::getKeyJudgeWindowRateGood, expandJudgeKeyGD),
					new TiledOption<>("Scratch Perfect Great", playerConfig::getScratchJudgeWindowRatePerfectGreat, expandJudgeScratchPG),
					new TiledOption<>("Scratch Great", playerConfig::getScratchJudgeWindowRateGreat, expandJudgeScratchGR),
					new TiledOption<>("Scratch Good", playerConfig::getScratchJudgeWindowRateGood, expandJudgeScratchGD)
			))
	);

	public AssistSettingsWindow(Config config, PlayerConfig playerConfig) {
		super("Assist", config, playerConfig);
	}

	public List<Pair<String, List<TiledOption<?>>>> getOptions() {
		return options;
	}
}
