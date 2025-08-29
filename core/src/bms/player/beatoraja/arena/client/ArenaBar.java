package bms.player.beatoraja.arena.client;

import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.Bar;
import bms.player.beatoraja.select.bar.DirectoryBar;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;

public class ArenaBar extends DirectoryBar {
    private SongData songData;

    public ArenaBar(MusicSelector selector, SongData songData) {
        super(selector );
        this.songData = songData;
    }

    @Override
    public void updateFolderStatus() {
        updateFolderStatus(new SongData[]{songData});
    }

    @Override
    public Bar[] getChildren() {
        return SongBar.toSongBarArray(new SongData[]{songData});
    }

    @Override
    public String getTitle() {
        return "Arena";
    }
}
