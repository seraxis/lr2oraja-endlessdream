package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.ClearType;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.select.MusicSelectCommand;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;


public class SongManagerMenu {
    // I cannot think of a better solution than hold a ref of MusicSelector
    private static MusicSelector selector;
    /**
     * Current song's reverse lookup result
     */
    private static List<String> currentReverseLookupList = new ArrayList<>();
    /**
     * In-game local records cache, could be refactored into a fixed size one in the future
     */
    private static Map<String, List<ScoreData>> localHistoryCache = new HashMap<>();

    private static ImBoolean LAST_PLAYED_SORT = new ImBoolean(false);
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void show(ImBoolean showSongManager) {
        Optional<SongData> currentSongData = getCurrentSongData();
        Optional<ScoreData> currentScoreData = getCurrentScoreData();
        if (ImGui.begin("Song Manager", showSongManager, ImGuiWindowFlags.AlwaysAutoResize)) {
            String songName = currentSongData.map(SongData::getTitle).orElse("");
            String sha256 = currentSongData.map(SongData::getSha256).orElse("");
            String lastPlayRecordTime = currentScoreData.map(scoreData -> {
                Date date = new Date(scoreData.getDate() * 1000L);
                return simpleDateFormat.format(date);
            }).orElse("Not played");
            ImGui.text("current picking: " + songName);

            ImGui.text("Last played: " + lastPlayRecordTime);
            if (ImGui.checkbox("Sort by last played", LAST_PLAYED_SORT)) {
                selector.getBarManager().updateBar();
            }

            if (songName.isEmpty()) {
                ImGui.text("Not a selectable song");
            } else {
                if (ImGui.button("Show Reverse Lookup")) {
                    updateReverseLookupData(currentSongData);
                    ImGui.openPopup("Reverse Lookup");
                }
                if (ImGui.beginPopup("Reverse Lookup", ImGuiWindowFlags.AlwaysAutoResize)) {
                    for (int i = 0;i < currentReverseLookupList.size();++i) {
                        ImGui.pushID(i);
                        ImGui.bulletText(currentReverseLookupList.get(i));
                        ImGui.popID();
                    }
                    ImGui.endPopup();
                }
                // TODO: Add a sort button here
                // TODO: Add a mod filter button here
                List<ScoreData> localHistory = loadLocalHistory(sha256);
                renderLocalHistoryTable(localHistory);
            }
        }
        ImGui.end();
    }

    public static void injectMusicSelector(MusicSelector musicSelector) {
        selector = musicSelector;
    }

    /**
     * Invalid a chart's local history cache
     */
    public static void invalidCache(String sha256) {
        localHistoryCache.remove(sha256);
    }

    /**
     * Update current reverse lookup result by current song data
     *
     * @param currentSongData clear reverse lookup result if empty
     */
    private static void updateReverseLookupData(Optional<SongData> currentSongData) {
        if (currentSongData.isEmpty()) {
            currentReverseLookupList.clear();
            return ;
        }
        String currentMd5 = currentSongData.get().getMd5();
        String currentSha256 = currentSongData.get().getSha256();

        currentReverseLookupList = getReverseLookupData(currentMd5, currentSha256);
    }

    /**
     * Load one chart's local history, currently it's not an async function because querying sqlite
     * is already pretty fast. We can do the refactor later if needed
     */
    private static List<ScoreData> loadLocalHistory(String sha256) {
        return localHistoryCache.computeIfAbsent(sha256, s -> selector.main.getPlayDataAccessor().readScoreDataLog(sha256));
    }

    /**
     * Render local records as a table
     * @param localHistory local records
     */
    private static void renderLocalHistoryTable(List<ScoreData> localHistory) {
        if (ImGui.beginTable("Local History", 4, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("Clear");
            ImGui.tableSetupColumn("Freq");
            ImGui.tableSetupColumn("Judge");
            ImGui.tableSetupColumn("Time");
            ImGui.tableHeadersRow();
            for (ScoreData scoreData : localHistory) {
                ImGui.tableNextRow();
                ImGui.pushID(scoreData.getDate());

                ImGui.tableSetColumnIndex(0);
                ImGui.text(ClearType.getClearTypeByID(scoreData.getClear()).name());

                ImGui.tableSetColumnIndex(1);
                int rate = scoreData.getRate();
                String rateData = rate == 0 ? "/" : String.format("%.02fx", (rate / 100.0f));
                ImGui.text(rateData);

                ImGui.tableSetColumnIndex(2);
                int overrideJudge = scoreData.getOverridejudge();
                String overrideJudgeDate = overrideJudge == -1 ? "/" : JudgeTrainer.JUDGE_OPTIONS[overrideJudge];
                ImGui.text(overrideJudgeDate);

                ImGui.tableSetColumnIndex(3);
                ImGui.text(simpleDateFormat.format(new Date(scoreData.getDate() * 1000)));

                ImGui.popID();
            }
            ImGui.endTable();
        }
    }

    private static Optional<SongData> getCurrentSongData() {
        if (selector.getSelectedBar() instanceof SongBar) {
            final SongData sd = ((SongBar) selector.getSelectedBar()).getSongData();
            if (sd != null && sd.getPath() != null) {
                return Optional.of(sd);
            }
        }
        return Optional.empty();
    }

    private static Optional<ScoreData> getCurrentScoreData() {
        if (selector.getSelectedBar() instanceof SongBar) {
            final ScoreData sd = ((SongBar) selector.getSelectedBar()).getScore();
            return Optional.ofNullable(sd);
        }
        return Optional.empty();
    }

    private static List<String> getReverseLookupData(String md5, String sha256) {
        return selector.main.getPlayerResource().getReverseLookupData(md5, sha256);
    }

    public static boolean isLastPlayedSortEnabled() {
        return LAST_PLAYED_SORT.get();
    }

    public static void forceDisableLastPlayedSort() {
        LAST_PLAYED_SORT.set(false);
    }
}
