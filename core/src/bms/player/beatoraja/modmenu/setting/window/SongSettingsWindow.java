package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.widget.StringComboWidget;
import bms.player.beatoraja.modmenu.setting.widget.TiledOption;
import bms.player.beatoraja.select.BarSorter;
import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.List;

public class SongSettingsWindow extends TiledOptionBasedWindow {
	public SongSettingsWindow(Config config, PlayerConfig playerConfig) {
		super("Song", config, playerConfig);
	}

	private static String[] sortStrategies = new String[BarSorter.values().length];

	static {
		for (int i = 0; i < BarSorter.values().length; i++) {
			sortStrategies[i] = BarSorter.values()[i].name();
		}
		// TODO: How do we hide the truth that "last played" is not a real sort option?
	}

	private final StringComboWidget songSortStrategy = new StringComboWidget("##Sort Strategy", sortStrategies, playerConfig::setSort);

	private List<Pair<String, List<TiledOption<?>>>> options = List.of(
			Pair.of("Song Select", Arrays.asList(
					new TiledOption<>("Sort Strategy", playerConfig::getSort, songSortStrategy)
			))
	);

	@Override
	public List<Pair<String, List<TiledOption<?>>>> getOptions() {
		return options;
	}
}
