package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.*;


public class SongManagerMenu {
    // I cannot think of a better solution than hold a ref of MusicSelector
    private static MusicSelector selector;
    /**
     * Current song's reverse lookup result
     */
    private static List<String> currentReverseLookupList = new ArrayList<>();

    private static ImBoolean LAST_PLAYED_SORT = new ImBoolean(false);

    public static void show(ImBoolean showSongManager) {
        Optional<SongData> currentSongData = getCurrentSongData();
        if (ImGui.begin("Song Manager", showSongManager, ImGuiWindowFlags.AlwaysAutoResize)) {
            String songName = currentSongData.map(SongData::getTitle).orElse("");
            ImGui.text("current picking: " + songName);
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
                    ImGui.endPopup();;
                }
            }
        }

        if (ImGui.checkbox("Sort by last played", LAST_PLAYED_SORT)) {
            selector.getBarManager().updateBar();
        }

        ImGui.end();
    }

    public static void injectMusicSelector(MusicSelector musicSelector) {
        selector = musicSelector;
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
