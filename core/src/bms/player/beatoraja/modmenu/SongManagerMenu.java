package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public class SongManagerMenu {
    // I cannot think of a better solution than hold a ref of MusicSelector
    private static MusicSelector selector;
    /**
     * Current song's reverse lookup result
     */
    private static List<String> currentReverseLookupList = new ArrayList<>();
    /**
     * Last selected song data ref<br>
     * Used to detect if player changed current selected song
     */
    private static Optional<SongData> lastSongData = Optional.empty();
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void show(ImBoolean showSongManager) {
        Optional<SongData> currentSongData = getCurrentSongData();
        Optional<ScoreData> currentScoreData = getCurrentScoreData();
        updateMarkList(currentSongData);
        if (ImGui.begin("Song Manager", showSongManager, ImGuiWindowFlags.AlwaysAutoResize)) {
            String songName = currentSongData.map(SongData::getTitle).orElse("");
            String bestPlayRecordTime = currentScoreData.map(scoreData -> {
                Date date = new Date(scoreData.getDate() * 1000L);
                return simpleDateFormat.format(date);
            }).orElse("No Data");
            ImGui.text("current picking: " + songName);
            ImGui.text("Best Record: " + bestPlayRecordTime);
            if (songName.isEmpty()) {
                ImGui.text("Not a selectable song");
            } else {
                if (ImGui.button("Show Reverse Lookup")) {
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
        ImGui.end();
    }

    public static void injectMusicSelector(MusicSelector musicSelector) {
        selector = musicSelector;
    }

    /**
     * Update current reverse lookup result by current song data
     *
     * @param currentSongData do nothing if empty
     */
    private static void updateMarkList(Optional<SongData> currentSongData) {
        if (currentSongData.isEmpty()) {
            return ;
        }
        String currentMd5 = currentSongData.get().getMd5();
        String currentSha256 = currentSongData.get().getSha256();
        if (lastSongData.isPresent()) {
            String lastPath = lastSongData.get().getPath();
            String lastSha256 = lastSongData.get().getSha256();
            String currentPath = currentSongData.get().getPath();
            if (lastPath.equals(currentPath) && lastSha256.equals(currentSha256)) {
                return ;
            }
        }
        lastSongData = currentSongData;

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
}
