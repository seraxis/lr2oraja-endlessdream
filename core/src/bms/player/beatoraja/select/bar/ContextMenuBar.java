package bms.player.beatoraja.select.bar;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;

import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.BMSPlayerMode;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.skin.property.EventFactory.EventType;
import bms.player.beatoraja.ScoreDatabaseAccessor.ScoreDataCollector;
import bms.model.Mode;

import static bms.player.beatoraja.select.bar.FunctionBar.*;
import static bms.player.beatoraja.SystemSoundManager.SoundType.FOLDER_OPEN;
import static bms.player.beatoraja.SystemSoundManager.SoundType.OPTION_CHANGE;

import java.net.URI;
import java.awt.Desktop;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;

public class ContextMenuBar extends DirectoryBar {
    private SongData song = null;
    private TableBar table = null;
    private boolean showMeta = false;
    private String title;

    public static boolean browserOpen(String url) {
        Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
        clipboard.setContents(url);
        try {
            URI uri = new URI(url);
            Desktop.getDesktop().browse(uri);
            ImGuiNotify.info("Copied URL to clipboard.");
            return true;
        }
        catch (Throwable e) {
            e.printStackTrace();
            ImGuiNotify.info("Opening the browser failed. Copied URL to clipboard.");
            return false;
        }
    }

    public ContextMenuBar(MusicSelector selector, SongData song) {
        // sets showInvisibleChart = true
        super(selector, true);
        this.setSortable(false);
        this.song = song;
        this.title = song.getTitle();
    }

    public ContextMenuBar(MusicSelector selector, TableBar table) {
        // sets showInvisibleChart = true
        super(selector, true);
        this.setSortable(false);
        this.table = table;
        this.title = table.getTableData().getName();
    }

    public String getTitle() { return title; }
    public int getLamp(boolean dontCare) { return 0; }
    public Bar getPrevious() { return table; }

    public Bar[] getChildren() {
        if (song != null && song.getPath() != null) { return songContext(); }
        else if (song != null && song.getPath() == null) { return missingSongContext(); }
        else if (table != null) { return tableContext(); }
        else { return new Bar[0]; }
    }

    private Bar[] missingSongContext() {
        ArrayList<Bar> options = new ArrayList<>();

        var play = new SongBar(song);
        options.add(play);

        addLeaderboardEntries(options);

        showMeta = true;
        addMetaEntries(options);

        addTagDisplayEntries(options);
        return options.toArray(new Bar[0]);
    }

    private Bar[] songContext() {
        ArrayList<Bar> options = new ArrayList<>();

        var play = new SongBar(song);
        options.add(play);

        var autoplay = new FunctionBar((selector, self) -> {
            selector.selectSong(BMSPlayerMode.AUTOPLAY);
            selector.readChart(song, play);
        }, "Autoplay", STYLE_TABLE);
        options.add(autoplay);

        var practice = new FunctionBar((selector, self) -> {
            selector.selectSong(BMSPlayerMode.PRACTICE);
            selector.readChart(song, play);
        }, "Practice", STYLE_TABLE);
        options.add(practice);

        addLeaderboardEntries(options);

        var related = new FunctionBar((selector, self) -> {
            var same = new SameFolderBar(selector, song.getFullTitle(), song.getFolder());
            selector.getBarManager().updateBar(same);
            selector.play(FOLDER_OPEN);
        }, "Related", STYLE_TABLE);
        SongData[] songs = selector.getSongDatabase().getSongDatas("folder", song.getFolder());
        related.setLamps(calculateLamps(selector, songs));
        options.add(related);

        var folder = new FunctionBar(null, "Open Song Folder", STYLE_FOLDER);
        folder.setFunction((selector, self) -> {
            selector.getBarManager().setSelected(play);
            selector.executeEvent(EventType.open_with_explorer);
            selector.getBarManager().setSelected(folder);
            selector.play(FOLDER_OPEN);
        });
        options.add(folder);

        var url = new FunctionBar((selector, self) -> {
            boolean success = ContextMenuBar.browserOpen(song.getUrl());
            selector.play(success ? FOLDER_OPEN : OPTION_CHANGE);
        }, "Open URL", STYLE_FOLDER);
        if (song.getUrl() != null && song.getUrl().length() > 0) options.add(url);

        var appendUrl = new FunctionBar((selector, self) -> {
            boolean success = ContextMenuBar.browserOpen(song.getAppendurl());
            selector.play(success ? FOLDER_OPEN : OPTION_CHANGE);
        }, "Open Append URL", STYLE_FOLDER);
        if (song.getAppendurl() != null && song.getAppendurl().length() > 0 &&
            !song.getAppendurl().equals(song.getUrl())) {
            options.add(appendUrl);
        }

        addMetaEntries(options);

        boolean isFavChart = ((song.getFavorite() & SongData.FAVORITE_CHART) != 0);
        var favChart = new FunctionBar(
            (selector, self)
                -> {
                song.setFavorite(song.getFavorite() ^ SongData.FAVORITE_CHART);
                selector.main.getSongDatabase().setSongDatas(new SongData[] {song});
                selector.play(OPTION_CHANGE);
                boolean isFav = 0 != (song.getFavorite() & SongData.FAVORITE_CHART);
                self.setDisplayBarType(isFav ? STYLE_COURSE : STYLE_MISSING);
                self.setDisplayTextType(isFav ? STYLE_TEXT_PLAIN : STYLE_TEXT_MISSING);
            },
            "Favorite Chart", isFavChart ? STYLE_COURSE : STYLE_MISSING,
            isFavChart ? STYLE_TEXT_PLAIN : STYLE_TEXT_MISSING);
        favChart.setSongData(song);
        options.add(favChart);

        boolean isFavSong = ((song.getFavorite() & SongData.FAVORITE_SONG) != 0);
        var favSong = new FunctionBar(
            (selector, self)
                -> {
                song.setFavorite(song.getFavorite() ^ SongData.FAVORITE_SONG);
                selector.main.getSongDatabase().setSongDatas(new SongData[] {song});
                selector.play(OPTION_CHANGE);
                boolean isFav = 0 != (song.getFavorite() & SongData.FAVORITE_SONG);
                self.setDisplayBarType(isFav ? STYLE_COURSE : STYLE_MISSING);
                self.setDisplayTextType(isFav ? STYLE_TEXT_PLAIN : STYLE_TEXT_MISSING);
            },
            "Favorite Song", isFavSong ? STYLE_COURSE : STYLE_MISSING,
            isFavSong ? STYLE_TEXT_PLAIN : STYLE_TEXT_MISSING);
        favSong.setSongData(song);
        options.add(favSong);

        addTagDisplayEntries(options);

        for (int i = 0; i < MusicSelector.REPLAY; ++i) {
            boolean replayExists = selector.main.getPlayDataAccessor().existsReplayData(
                song.getSha256(), song.hasUndefinedLongNote(),
                selector.main.getPlayerConfig().getLnmode(), i);
            if (!replayExists) { continue; }
            final int replayIndex = i;
            var replay = new FunctionBar((selector, self) -> {
                selector.selectSong(BMSPlayerMode.getReplayMode(replayIndex));
                selector.readChart(song, play);
            }, "Replay", STYLE_COURSE);
            replay.setLevel(i + 1);
            options.add(replay);
        }

        return options.toArray(new Bar[0]);
    }

    private void addLeaderboardEntries(ArrayList<Bar> options) {
        var leaderboard = new FunctionBar((selector, self) -> {
            selector.getBarManager().updateBar(new LeaderBoardBar(selector, song, false));
            selector.play(FOLDER_OPEN);
        }, "Leaderboard", STYLE_SPECIAL);
        if (0 < selector.main.getIRStatus().length) { options.add(leaderboard); }

        var lr2ir = new FunctionBar((selector, self) -> {
            selector.getBarManager().updateBar(new LeaderBoardBar(selector, song, true));
            selector.play(FOLDER_OPEN);
        }, "LR2IR Leaderboard", STYLE_SPECIAL);
        options.add(lr2ir);
    }

    private void addMetaEntries(ArrayList<Bar> options) {
        var lr2irPage = new FunctionBar((selector, self) -> {
            String urlBase =
                "http://www.dream-pro.info/~lavalse/LR2IR/search.cgi?mode=ranking&bmsmd5=";
            boolean success = ContextMenuBar.browserOpen(urlBase + song.getMd5());
            selector.play(success ? FOLDER_OPEN : OPTION_CHANGE);
        }, "Open LR2IR page", STYLE_FOLDER);
        if (song.getMd5() != null) options.add(lr2irPage);

        var chartViewer = new FunctionBar((selector, self) -> {
            String urlBase = "https://bms-score-viewer.pages.dev/view?md5=";
            boolean success = ContextMenuBar.browserOpen(urlBase + song.getMd5());
            selector.play(success ? FOLDER_OPEN : OPTION_CHANGE);
        }, "Open Chart Viewer", STYLE_FOLDER);
        if (song.getMd5() != null) options.add(chartViewer);

        var meta = new FunctionBar((selector, self) -> {
            if (!showMeta) {
                showMeta = true;
                selector.getBarManager().updateBar();
                selector.play(OPTION_CHANGE);
            }
        }, "Metadata", showMeta ? STYLE_TABLE : STYLE_SEARCH);
        options.add(meta);
        if (showMeta) {
            // var explain =
            //     new FunctionBar((selector, self) -> {}, "Press play to copy:", STYLE_SEARCH);
            var title = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getTitle());
                self.setDisplayTextType(STYLE_TEXT_PLAIN);
                ImGuiNotify.info("Copied song title to clipboard.");
                selector.play(OPTION_CHANGE);
            }, "Copy Title", STYLE_SEARCH, STYLE_TEXT_NEW);
            var md5 = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getMd5());
                self.setDisplayTextType(STYLE_TEXT_PLAIN);
                ImGuiNotify.info("Copied MD5 to clipboard.");
                selector.play(OPTION_CHANGE);
            }, "Copy MD5", STYLE_SEARCH, STYLE_TEXT_NEW);
            var sha256 = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getSha256());
                self.setDisplayTextType(STYLE_TEXT_PLAIN);
                ImGuiNotify.info("Copied SHA256 to clipboard.");
                selector.play(OPTION_CHANGE);
            }, "Copy SHA256", STYLE_SEARCH, STYLE_TEXT_NEW);
            var path = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getPath());
                self.setDisplayTextType(STYLE_TEXT_PLAIN);
                ImGuiNotify.info("Copied song path to clipboard.");
                selector.play(OPTION_CHANGE);
            }, "Copy Path", STYLE_SEARCH, STYLE_TEXT_NEW);
            var urltext = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getUrl());
                self.setDisplayTextType(STYLE_TEXT_PLAIN);
                ImGuiNotify.info("Copied URL to clipboard.");
                selector.play(OPTION_CHANGE);
            }, "Copy URL", STYLE_SEARCH, STYLE_TEXT_NEW);
            var appendUrltext = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getAppendurl());
                self.setDisplayTextType(STYLE_TEXT_PLAIN);
                ImGuiNotify.info("Copied append URL to clipboard.");
                selector.play(OPTION_CHANGE);
            }, "Copy Append URL", STYLE_SEARCH, STYLE_TEXT_NEW);

            title.setSubtitle(song.getTitle());
            md5.setSubtitle(song.getMd5());
            sha256.setSubtitle(song.getSha256());
            urltext.setSubtitle(song.getUrl());
            appendUrltext.setSubtitle(song.getAppendurl());
            // options.add(explain);
            if (song.getTitle() != null) { options.add(title); }
            if (song.getMd5() != null) { options.add(md5); }
            if (song.getSha256() != null) { options.add(sha256); }
            if (song.getPath() != null) { options.add(path); }
            if (song.getUrl() != null) { options.add(urltext); }
            if (song.getAppendurl() != null && !song.getAppendurl().equals(song.getUrl())) {
                options.add(appendUrltext);
            }
        }
    }

    private void addTagDisplayEntries(ArrayList<Bar> options) {
        String md5 = (song.getMd5() == null) ? "" : song.getMd5();
        String sha256 = (song.getSha256() == null) ? "" : song.getSha256();
        List<String> reverseLookup = new ArrayList<>();
        TableBar[] tables = selector.getBarManager().getTables();
        for (TableBar table : tables) {
            HashBar[] levels = table.getLevels();
            for (HashBar level : levels) {
                SongData[] songs = level.getElements();
                for (SongData tableSong : songs) {
                    boolean md5Match =
                        !tableSong.getMd5().isEmpty() && tableSong.getMd5().equals(md5);
                    boolean sha256Match =
                        !tableSong.getSha256().isEmpty() && tableSong.getSha256().equals(sha256);
                    if (md5Match || sha256Match) {
                        addTableEntry(options, table, level);
                        break;
                    }
                }
            }
        }
    }

    private void addTableEntry(ArrayList<Bar> options, TableBar table, HashBar level) {
        var entry = level.getTitle() + " " + table.getTitle();
        var showTables = new FunctionBar((selector, self) -> {
            var barManager = selector.getBarManager();
            barManager.updateBar(null);
            barManager.setSelected(table);
            barManager.updateBar(table);
            barManager.setSelected(level);
            barManager.updateBar(level);
            barManager.setSelected(new SongBar(song));
            selector.play(FOLDER_OPEN);
        }, entry, STYLE_SEARCH);
        SongData[] songs = level.getElements();
        showTables.setLamps(calculateLamps(selector, songs));
        options.add(showTables);
    }

    private static int[] calculateLamps(MusicSelector selector, SongData[] songs) {
        String[] songHashes = Stream.of(songs)
                                  .map(e -> e.getSha256().length() > 0 ? e.getSha256() : e.getMd5())
                                  .toArray(String[] ::new);
        songs = selector.getSongDatabase().getSongDatas(songHashes);
        final Mode mode = selector.main.getPlayerConfig().getMode();
        int[] lamps = new int[11];
        final ScoreDataCollector collector = (song, score) -> {
            if (song.getPath() == null ||
                (mode != null && song.getMode() != 0 && song.getMode() != mode.id)) {
                return;
            }

            int lampIndex = (score != null) ? score.getClear() : 0;
            lamps[lampIndex]++;
        };

        selector.getScoreDataCache().readScoreDatas(collector, songs,
                                                    selector.main.getPlayerConfig().getLnmode());
        return lamps;
    }

    private Bar[] tableContext() {
        ArrayList<Bar> options = new ArrayList<>();
        options.add(table);

        var openUrl = new FunctionBar((selector, self) -> {
            boolean success = ContextMenuBar.browserOpen(table.getTableData().getUrl());
            selector.play(success ? FOLDER_OPEN : OPTION_CHANGE);
        }, "Open URL", STYLE_FOLDER);
        if (table.getTableData().getUrl() != null) options.add(openUrl);

        var name = new FunctionBar((selector, self) -> {
            Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
            clipboard.setContents(table.getTableData().getName());
            self.setDisplayTextType(STYLE_TEXT_PLAIN);
            ImGuiNotify.info("Copied table name to clipboard.");
            selector.play(OPTION_CHANGE);
        }, "Copy Table Name", STYLE_SEARCH, STYLE_TEXT_NEW);
        if (table.getTableData().getName() != null) options.add(name);

        var copyUrl = new FunctionBar((selector, self) -> {
            Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
            clipboard.setContents(table.getTableData().getUrl());
            self.setDisplayTextType(STYLE_TEXT_PLAIN);
            ImGuiNotify.info("Copied table URL to clipboard.");
            selector.play(OPTION_CHANGE);
        }, "Copy URL", STYLE_SEARCH, STYLE_TEXT_NEW);
        if (table.getTableData().getUrl() != null) options.add(copyUrl);

        return options.toArray(new Bar[0]);
    }
}


// entries we can add in the future once more features are supported:
// song menu:
//  ir rival scores
//  readme (imgui popup)
//  play history
//	tag editing
//  replay score, lamp, random lanes

// folder menu:
//  select a table to filter by
//  autoplay all

// on a replay
//  delete replay
//  all replay playing options (can someone please figure out what they are)
