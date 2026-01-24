package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.config.KeyConfiguration;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.widget.CheckboxWidget;
import bms.player.beatoraja.modmenu.setting.widget.Label;
import bms.player.beatoraja.modmenu.setting.widget.StringComboWidget;
import bms.player.beatoraja.modmenu.setting.widget.TiledOption;
import bms.player.beatoraja.select.BarSorter;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;
import bms.tool.util.Pair;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Show the current song data & some misc selector settings
 */
public class SongSettingsWindow extends BaseSettingWindow {
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Internal states
	private SongData currentSongData;
	private ScoreData currentScoreData;
	private final List<String> reverseLookupData = new ArrayList<>();
	// NOTE: Ideally, this should be shown as an option in sort strategies, but it's really hard to hide the fact
	//  that it's actually not, so current it's directly exposed to both users and internal modules.
	private static final ImBoolean lastPlayedSort = new ImBoolean(false);

	public SongSettingsWindow(Config config, PlayerConfig playerConfig) {
		super(config, playerConfig);
	}

	@Override
	public String getName() {
		return "Song";
	}

	private static final String[] sortStrategies = new String[BarSorter.values().length];
	private final CheckboxWidget useLastPlayedSort = new CheckboxWidget("##Last Played Sort", lastPlayedSort::set);
	private final StringComboWidget selectKeyMode = new StringComboWidget("##Select Key Mode", KeyConfiguration.SELECTKEY, playerConfig::setMusicselectinput);

	static {
		for (int i = 0; i < BarSorter.values().length; i++) {
			sortStrategies[i] = BarSorter.values()[i].name();
		}
		// TODO: How do we hide the truth that "last played" is not a real sort option?
	}


	@Override
	public void render() {
		if (currentSongData != null) {
			Label.categoryLabel("Current Song").render();
			if (ImGui.beginTable("##SongSettingsWindow##SongData", 2)) {
				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(0);
				ImGui.text("Song Name");
				ImGui.tableSetColumnIndex(1);
				ImGui.text(currentSongData.getFullTitle());

				ImGui.tableNextRow();

				ImGui.tableSetColumnIndex(0);
				ImGui.text("Artist");
				ImGui.tableSetColumnIndex(1);
				ImGui.text(currentSongData.getArtist());

				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(0);
				ImGui.text("Last Played");
				ImGui.tableSetColumnIndex(1);
				if (currentScoreData != null) {
					ImGui.text(simpleDateFormat.format(new Date(currentScoreData.getDate() * 1000L)));
				} else {
					ImGui.text("Not played");
				}
				ImGui.endTable();
			}

			Label.categoryLabel("Reverse lookup").render();
			for (int i = 0;i < reverseLookupData.size();++i) {
				ImGui.pushID(i);
				ImGui.bulletText(reverseLookupData.get(i));
				ImGui.popID();
			}
		}

		options.forEach(category -> {
			Label.categoryLabel(category.getFirst()).render();
			category.getSecond().forEach(TiledOption::render);
		});
	}

	private final StringComboWidget songSortStrategy = new StringComboWidget("##Sort Strategy", sortStrategies, playerConfig::setSort);

	private List<Pair<String, List<TiledOption<?>>>> options = List.of(
			Pair.of("Song Select", Arrays.asList(
					new TiledOption<>("Sort Strategy", playerConfig::getSort, songSortStrategy),
					new TiledOption<>("Sort by Last Played", lastPlayedSort::get, useLastPlayedSort),
					new TiledOption<>("Key Mode", playerConfig::getMusicselectinput, selectKeyMode).addDescription(
							"Many features in music select scene is defined by key1, key2, etc. This option defines which key mode bindings you want to use in music select scene. If there's no explicit reason, keep it same as your play mode(normally 7k)"
					)
			))
	);

	@Override
	public void refresh() {
		currentSongData = null;
		currentScoreData = null;
		reverseLookupData.clear();
		// Update the current song if needed
		MainState current = SettingMenu.mainRef.getCurrentState();
		if (current instanceof MusicSelector selector) {
			if (selector.getSelectedBar() instanceof SongBar songBar) {
				SongData sd = songBar.getSongData();
				if (sd != null && sd.getPath() != null) {
					currentSongData = sd;
				}
				currentScoreData = songBar.getScore();
			}
			if (currentSongData != null) {
				reverseLookupData.addAll(SettingMenu.mainRef.getPlayerResource().getReverseLookupData());
			}
		}
		// Update the options
		options.forEach(p -> p.getSecond().forEach(TiledOption::refresh));
	}

	public static boolean isLastPlayedSortEnabled() {
		return lastPlayedSort.get();
	}

	public static void forceDisableLastPlayedSort() {
		lastPlayedSort.set(false);
	}
}
