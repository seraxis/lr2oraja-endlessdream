package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.widget.StringComboWidget;
import bms.player.beatoraja.modmenu.setting.widget.TiledOption;
import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.List;

public class ReplaySettingsWindow extends TiledOptionBasedWindow {
	public ReplaySettingsWindow(Config config, PlayerConfig playerConfig) {
		super("Replay", config, playerConfig);
	}

	@Override
	public List<Pair<String, List<TiledOption<?>>>> getOptions() {
		return options;
	}

	private final StringComboWidget replayAutoSaveSlot1 = createReplayAutoSave(1);
	private final StringComboWidget replayAutoSaveSlot2 = createReplayAutoSave(2);
	private final StringComboWidget replayAutoSaveSlot3 = createReplayAutoSave(3);
	private final StringComboWidget replayAutoSaveSlot4 = createReplayAutoSave(4);

	private final List<Pair<String, List<TiledOption<?>>>> options = Arrays.asList(
			Pair.of("Auto Save", Arrays.asList(
					new TiledOption<>("Slot 1", () -> playerConfig.getAutoSaveReplay()[0], replayAutoSaveSlot1),
					new TiledOption<>("Slot 2", () -> playerConfig.getAutoSaveReplay()[1], replayAutoSaveSlot2),
					new TiledOption<>("Slot 3", () -> playerConfig.getAutoSaveReplay()[2], replayAutoSaveSlot3),
					new TiledOption<>("Slot 4", () -> playerConfig.getAutoSaveReplay()[3], replayAutoSaveSlot4)
			))
	);

	private StringComboWidget createReplayAutoSave(int slot) {
		return new StringComboWidget(String.format("##Slot %d", slot),
				new String[]{"NONE", "BETTER_SCORE", "BETTER_OR_SAME_SCORE", "BETTER_MISSCOUNT", "BETTER_OR_SAME_MISSCOUNT",
						"BETTER_COMBO", "BETTER_OR_SAME_COMBO", "BETTER_LAMP", "BETTER_OR_SAME_LAMP", "BETTER_ALL", "ALWAYS"},
				StringComboWidget.PredefinedWidth.Short, newValue -> playerConfig.getAutoSaveReplay()[slot] = newValue);
	}
}
