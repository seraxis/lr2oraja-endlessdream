package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.*;
import bms.player.beatoraja.config.KeyConfiguration;
import bms.player.beatoraja.modmenu.FreqTrainerMenu;
import bms.player.beatoraja.modmenu.JudgeTrainer;
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
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

import java.text.SimpleDateFormat;
import java.util.*;

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
	/**
	 * Sort strategy
	 */
	private static ImInt sortStrategy = new ImInt(0);
	/**
	 * Whether show scores based on current selected mods or not
	 */
	private static ImBoolean showSelectedModdedScore = new ImBoolean(false);
	/**
	 * In-game local records cache, could be refactored into a fixed size one in the future
	 */
	private static Map<String, List<ScoreData>> localHistoryCache = new HashMap<>();

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
			ImGui.bulletText("Local History");
			ImGui.combo("sort", sortStrategy, SortStrategy.items);
			ImGui.checkbox("Show Selected Mods Scores", showSelectedModdedScore);
			List<ScoreData> localHistory = loadLocalHistory(currentSongData.getSha256());
			renderLocalHistoryTable(localHistory);
		}

		options.forEach(category -> {
			Label.categoryLabel(category.getFirst()).render();
			category.getSecond().forEach(TiledOption::render);
		});
	}

	private final StringComboWidget songSortStrategy = new StringComboWidget("##Sort Strategy", sortStrategies, playerConfig::setSort);
	private final CheckboxWidget showMissingChart = new CheckboxWidget("##Show Missing Chart", config::setShowNoSongExistingBar);

	private List<Pair<String, List<TiledOption<?>>>> options = List.of(
			Pair.of("Song Select", Arrays.asList(
					new TiledOption<>("Sort Strategy", playerConfig::getSort, songSortStrategy),
					new TiledOption<>("Sort by Last Played", lastPlayedSort::get, useLastPlayedSort),
					new TiledOption<>("Key Mode", playerConfig::getMusicselectinput, selectKeyMode).addDescription(
							"Many features in music select scene is defined by key1, key2, etc. This option defines which key mode bindings you want to use in music select scene. If there's no explicit reason, keep it same as your play mode(normally 7k)"
					),
					new TiledOption<>("Show missing charts", config::isShowNoSongExistingBar, showMissingChart).addDescription(
							"This option won't work if you're using http download too"
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

	/**
	 * Invalid a chart's local history cache
	 */
	public static void invalidCache(String sha256) {
		localHistoryCache.remove(sha256);
	}

	/**
	 * Load one chart's local history, currently it's not an async function because querying sqlite
	 * is already pretty fast. We can do the refactor later if needed
	 * Returned scores would be sorted by current sort strategy and filtered by current filtering settings
	 */
	private static List<ScoreData> loadLocalHistory(String sha256) {
		List<ScoreData> snapshot = localHistoryCache.computeIfAbsent(sha256, s -> SettingMenu.mainRef.getPlayDataAccessor().readScoreDataLog(sha256));
		SortStrategy strategy = SortStrategy.valueOf(sortStrategy.get());
		snapshot.sort(strategy.getComparator());
		if (showSelectedModdedScore.get()) {
			Optional<Integer> freqValue = FreqTrainerMenu.isFreqTrainerEnabled() ? Optional.of(FreqTrainerMenu.getFreq()) : Optional.empty();
			Optional<Integer> overrideJudge = JudgeTrainer.isActive() ? Optional.of(JudgeTrainer.getJudgeRank()) : Optional.empty();
			return snapshot.stream().filter(score -> {
				if (freqValue.isPresent() && !freqValue.get().equals(score.getRate())) {
					return false;
				}
				if (overrideJudge.isPresent() && !overrideJudge.get().equals(score.getOverridejudge())) {
					return false;
				}
				return true;
			}).toList();
		} else {
			return snapshot;
		}
	}

	/**
	 * Render local records as a table
	 *
	 * @param localHistory local records
	 */
	private static void renderLocalHistoryTable(List<ScoreData> localHistory) {
		if (ImGui.beginTable("Local History", 5, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
			ImGui.tableSetupScrollFreeze(0, 1);
			ImGui.tableSetupColumn("Clear");
			ImGui.tableSetupColumn("Score");
			ImGui.tableSetupColumn("Freq");
			ImGui.tableSetupColumn("Judge");
			ImGui.tableSetupColumn("Time");
			ImGui.tableHeadersRow();
			for (ScoreData scoreData : localHistory) {
				ImGui.tableNextRow();
				ImGui.pushID(scoreData.getDate());

				ImGui.tableNextColumn();
				ImGui.text(ClearType.getClearTypeByID(scoreData.getClear()).name());

				ImGui.tableNextColumn();
				ImGui.text("" + scoreData.getExscore());

				ImGui.tableNextColumn();
				int rate = scoreData.getRate();
				String rateData = rate == 0 ? "/" : String.format("%.02fx", (rate / 100.0f));
				ImGui.text(rateData);

				ImGui.tableNextColumn();
				int overrideJudge = scoreData.getOverridejudge();
				String overrideJudgeDate = overrideJudge == -1 ? "/" : JudgeTrainer.JUDGE_OPTIONS[overrideJudge];
				ImGui.text(overrideJudgeDate);

				ImGui.tableNextColumn();
				ImGui.text(simpleDateFormat.format(new Date(scoreData.getDate() * 1000)));

				ImGui.popID();
			}
			ImGui.endTable();
		}
	}

	private enum SortStrategy {
		RECORD_TIME("Record Time", (lhs, rhs) -> (int) (rhs.getDate() - lhs.getDate())),
		EX_SCORE("EX Score", (lhs, rhs) -> rhs.getExscore() - lhs.getExscore()),
		;

		private final String name;
		private final Comparator<ScoreData> comparator;

		SortStrategy(String name, Comparator<ScoreData> comparator) {
			this.name = name;
			this.comparator = comparator;
		}

		public String getName() {
			return name;
		}

		public Comparator<ScoreData> getComparator() {
			return comparator;
		}

		public static SortStrategy valueOf(int i) {
			String name = items[i];
			for (SortStrategy value : SortStrategy.values()) {
				if (value.getName().equals(name)) {
					return value;
				}
			}
			return null;
		}

		public static String[] items = Arrays.stream(SortStrategy.values()).map(SortStrategy::getName).toArray(String[]::new);
	}
}
