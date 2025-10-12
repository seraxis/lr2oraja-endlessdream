package bms.player.beatoraja.select.bar;

import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.BMSPlayerMode;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.MusicSelectCommand;
import bms.player.beatoraja.skin.property.EventFactory.EventType;

import static bms.player.beatoraja.SystemSoundManager.SoundType.FOLDER_OPEN;
import static bms.player.beatoraja.SystemSoundManager.SoundType.OPTION_CHANGE;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;

public class ContextMenuBar extends DirectoryBar {
    private SongData song = null;
    private boolean showMeta = false;

    public ContextMenuBar(MusicSelector selector, SongData song) {
        // sets showInvisibleChart = true
        super(selector, true);
        this.setSortable(false);
        this.song = song;
    }

    public String getTitle() { return song.getTitle(); }
    public int getLamp(boolean dontCare) { return 0; }

    public Bar[] getChildren() {
        if (song != null && song.getPath() != null) { return songContext(); }
        else if (song != null && song.getPath() == null) { return missingSongContext(); }
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
            selector.getBarManager().setSelected(play);
            selector.selectSong(BMSPlayerMode.AUTOPLAY);
        }, "Autoplay", STYLE_TABLE);
        options.add(autoplay);

        var practice = new FunctionBar((selector, self) -> {
            selector.getBarManager().setSelected(play);
            selector.selectSong(BMSPlayerMode.PRACTICE);
        }, "Practice", STYLE_TABLE);
        options.add(practice);

		addLeaderboardEntries(options);

        var related = new FunctionBar((selector, self) -> {
            selector.getBarManager().setSelected(play);
            selector.execute(MusicSelectCommand.SHOW_SONGS_ON_SAME_FOLDER);
        }, "Related", STYLE_TABLE);
        options.add(related);

        var folder = new FunctionBar((selector, self) -> {
            selector.getBarManager().setSelected(play);
            selector.executeEvent(EventType.open_with_explorer);
        }, "Open Song Folder", STYLE_FOLDER);
        options.add(folder);

        var url = new FunctionBar((selector, self) -> {
            selector.getBarManager().setSelected(play);
            selector.executeEvent(EventType.open_download_site);
        }, "Open URL", STYLE_FOLDER);
        if (song.getUrl() != null) options.add(url);

        addMetaEntries(options);

        boolean isFavChart = ((song.getFavorite() & SongData.FAVORITE_CHART) != 0);
        var favChart = new FunctionBar(
            (selector, self)
                -> {
                song.setFavorite(song.getFavorite() ^ SongData.FAVORITE_CHART);
                selector.main.getSongDatabase().setSongDatas(new SongData[] {song});
                selector.play(OPTION_CHANGE);
                boolean isFav = 0 != (song.getFavorite() & SongData.FAVORITE_CHART);
                self.displayBarType = isFav ? STYLE_COURSE : STYLE_MISSING;
                self.displayTextType = isFav ? STYLE_TEXT_PLAIN : STYLE_TEXT_MISSING;
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
                self.displayBarType = isFav ? STYLE_COURSE : STYLE_MISSING;
                self.displayTextType = isFav ? STYLE_TEXT_PLAIN : STYLE_TEXT_MISSING;
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
                selector.getBarManager().setSelected(play);
                selector.selectSong(BMSPlayerMode.getReplayMode(replayIndex));
            }, "Replay", STYLE_COURSE);
            replay.setLevel(i + 1);
            options.add(replay);
        }

        return options.toArray(new Bar[0]);
    }

    void addLeaderboardEntries(ArrayList<Bar> options) {
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

    void addMetaEntries(ArrayList<Bar> options) {
        var meta = new FunctionBar((selector, self) -> {
            showMeta = true;
            selector.getBarManager().updateBar();
            selector.resource.setSongdata(song);
        }, showMeta ? "Press play to copy:" : "Metadata", STYLE_SEARCH);
        options.add(meta);
        if (showMeta) {
            var title = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getTitle());
                self.displayTextType = STYLE_TEXT_PLAIN;
                ImGuiNotify.info("Copied to clipboard.");
            }, "Title", STYLE_SEARCH, STYLE_TEXT_NEW);
            var md5 = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getMd5());
                self.displayTextType = STYLE_TEXT_PLAIN;
                ImGuiNotify.info("Copied to clipboard.");
            }, "MD5", STYLE_SEARCH, STYLE_TEXT_NEW);
            var sha256 = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getSha256());
                self.displayTextType = STYLE_TEXT_PLAIN;
                ImGuiNotify.info("Copied to clipboard.");
            }, "SHA256", STYLE_SEARCH, STYLE_TEXT_NEW);
            var path = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getPath());
                self.displayTextType = STYLE_TEXT_PLAIN;
                ImGuiNotify.info("Copied to clipboard.");
            }, "PATH", STYLE_SEARCH, STYLE_TEXT_NEW);
            var urltext = new FunctionBar((selector, self) -> {
                Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
                clipboard.setContents(song.getUrl());
                self.displayTextType = STYLE_TEXT_PLAIN;
                ImGuiNotify.info("Copied to clipboard.");
            }, "URL", STYLE_SEARCH, STYLE_TEXT_NEW);
            title.setSubtitle(song.getTitle());
            md5.setSubtitle(song.getMd5());
            sha256.setSubtitle(song.getSha256());
            urltext.setSubtitle(song.getUrl());
            if (song.getTitle() != null) options.add(title);
            if (song.getMd5() != null) options.add(md5);
            if (song.getSha256() != null) options.add(sha256);
            if (song.getPath() != null) options.add(path);
            if (song.getUrl() != null) options.add(urltext);
        }
    }

    void addTagDisplayEntries(ArrayList<Bar> options) {
        selector.resource.setSongdata(song);
        List<String> tables =
            selector.resource.getReverseLookupData(song.getMd5(), song.getSha256());
        var showTables =
            new FunctionBar((selector, self) -> {}, String.join(", ", tables), STYLE_SEARCH);
        if (!tables.isEmpty()) { options.add(showTables); }
    }

    public class FunctionBar extends SelectableBar {
        private BiConsumer<MusicSelector, FunctionBar> function;
        private String title;
        private String subtitle;
        private int displayBarType;
        private int displayTextType;
        private SongData song = null;
        private Integer level = null;

        public FunctionBar(BiConsumer<MusicSelector, FunctionBar> f, String title,
                           int displayBarType) {
            this(f, title, displayBarType, 0);
        }

        public FunctionBar(BiConsumer<MusicSelector, FunctionBar> f, String title,
                           int displayBarType, int displayTextType) {
            this.title = title;
            this.function = f;
            this.displayBarType = displayBarType;
            this.displayTextType = displayTextType;
        }

        private void setSongData(SongData song) { this.song = song; }
        private void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        private void setLevel(int level) { this.level = level; }

        public void accept(MusicSelector selector) { this.function.accept(selector, this); }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public Integer getLevel() { return level; }
        public int getLamp(boolean dontCare) { return 0; }
        public int getDisplayBarType() { return displayBarType; }
        public int getDisplayTextType() { return displayTextType; }
    }

    // bar appearance IDs
    private static final int STYLE_SONG = 0;
    private static final int STYLE_FOLDER = 1;
    private static final int STYLE_TABLE = 2;
    private static final int STYLE_COURSE = 3;
    private static final int STYLE_MISSING = 4;
    private static final int STYLE_SPECIAL = 5;
    private static final int STYLE_SEARCH = 6;

    private static final int STYLE_TEXT_PLAIN = 0;
    private static final int STYLE_TEXT_NEW = 1;
    private static final int STYLE_TEXT_MISSING = 8;
}

// how to better display the tables a song is in?
//  maybe press on the entry to expand it in case there are too many to read

// entries remaining todo and ones we can add in the future once more features are supported:
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
