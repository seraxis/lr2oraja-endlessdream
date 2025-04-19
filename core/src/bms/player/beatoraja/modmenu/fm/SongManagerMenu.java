package bms.player.beatoraja.modmenu.fm;

import bms.player.beatoraja.SystemSoundManager;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


public class SongManagerMenu {
    // I cannot think of a better solution than hold a ref of MusicSelector
    private static MusicSelector selector;
    /**
     * Reflect current song state, would be updated at the begin of the show function<br>
     * It's very dangerous to keep opening the `Song Manager Menu` while editing the folder definitions
     */
    private static List<ImBoolean> folderMarkList = new ArrayList<>();
    /**
     * Reflect current editing state<br>
     * Workaround since we cannot freeze the game main thread
     */
    private static List<ImBoolean> currentMarkList = new ArrayList<>();
    /**
     * Last selected song data ref<br>
     * Used to detect if player changed current selected song
     */
    private static Optional<SongData> lastSongData = Optional.empty();

    public static void show(ImBoolean showSongManager) {
        List<FolderDefinition> fdSnapshot = FolderManager.getFolderDefinitions();
        Optional<SongData> currentSongData = getCurrentSongData();
        updateMarkList(currentSongData, fdSnapshot);
        if (ImGui.begin("Song Manager", showSongManager, ImGuiWindowFlags.AlwaysAutoResize)) {
            int favorite = currentSongData.map(SongData::getFavorite).orElse(0);
            String songName = currentSongData.map(SongData::getTitle).orElse("");
            ImGui.text("current picking: " + songName);
            ImGui.text("Debug: favorite: " + favorite);
            if (songName.isEmpty()) {
                ImGui.text("Not a selectable song");
            } else {
                if (favorite == 0) {
                    ImGui.text("No related folder");
                } else {
                    for (int i = 0; i < fdSnapshot.size(); ++i) {
                        FolderDefinition fd = fdSnapshot.get(i);
                        if ((favorite & (1 << fd.getBits())) != 0) {
                            ImGui.pushID(i);
                            ImGui.bulletText(fd.getName());
                            ImGui.popID();
                        }
                    }
                }
                if (ImGui.button("Add to folders")) {
                    ImGui.openPopup("Add song to folder popup");
                }
                if (ImGui.beginPopupModal("Add song to folder popup", ImGuiWindowFlags.AlwaysAutoResize)) {
                    for (int i = 0;i < fdSnapshot.size();++i) {
                        FolderDefinition fd = fdSnapshot.get(i);
                        ImGui.checkbox(fd.getName(), currentMarkList.get(i));
                    }
                    if (ImGui.button("OK")) {
                        Logger.getGlobal().info("WE ARE ACTUALLY ARRIVED");
                        List<FolderDefinition> bindsTo = new ArrayList<>();
                        for (int i = 0;i < fdSnapshot.size();++i) {
                            if (currentMarkList.get(i).get()) {
                                bindsTo.add(fdSnapshot.get(i));
                            }
                        }
                        if (!bindsTo.isEmpty()) {
                            bind(currentSongData.get(), bindsTo, false);
                        }
                        ImGui.closeCurrentPopup();
                    }
                    if (ImGui.button("Cancel")) {
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }
            }

        }
        ImGui.end();
    }

    public static void injectMusicSelector(MusicSelector musicSelector) {
        selector = musicSelector;
    }

    /**
     * Update mark list by current song data & folder definitions
     *
     * @param currentSongData do nothing if empty
     * @param snapshot current folder definitions' snapshot
     */
    private static void updateMarkList(Optional<SongData> currentSongData, List<FolderDefinition> snapshot) {
        if (currentSongData.isEmpty()) {
            return ;
        }
        if (lastSongData.isPresent()) {
            String lastPath = lastSongData.get().getPath();
            String lastSha256 = lastSongData.get().getSha256();
            String currentPath = currentSongData.get().getPath();
            String currentSha256 = currentSongData.get().getSha256();
            if (lastPath.equals(currentPath) && lastSha256.equals(currentSha256)) {
                return ;// Okay dokey
            }
        }
        lastSongData = currentSongData;
        int favorite = currentSongData.get().getFavorite();

        folderMarkList = snapshot.stream()
                .map(fd -> (favorite & (1 << fd.getBits())) != 0)
                .map(ImBoolean::new)
                .toList();
        currentMarkList = new ArrayList<>(folderMarkList);
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

    /**
     * @param fds The *FULL SET* of the folders to bind
     * @param addAll add all difficult to selected folder if true, otherwise only add current picked one
     */
    private static void bind(SongData songData, List<FolderDefinition> fds, boolean addAll) {
        if (fds == null || fds.isEmpty()) {
            return ; // Okay dokey
        }
        int favorite = songData.getFavorite();
        for (FolderDefinition fd : fds) {
            favorite |= (1 << fd.getBits());
        }
        SongData[] songs = selector.main.getSongDatabase().getSongDatas("folder", songData.getFolder());
        for (SongData song : songs) {
            if (addAll) {
                song.setFavorite(song.getFavorite() | favorite);
            } else if (song.getSha256().equals(songData.getSha256())) {
                song.setFavorite(song.getFavorite() | favorite);
            }
        }
        selector.main.getSongDatabase().setSongDatas(songs);
        selector.getBarManager().updateBar();
        selector.play(SystemSoundManager.SoundType.OPTION_CHANGE);
    }
}
