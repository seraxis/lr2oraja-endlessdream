package bms.player.beatoraja.arena.client;

public class SelectedSong {
    private String md5;
    private String title;
    private String artist;
    private String path;

    public SelectedSong() {

    }

    public SelectedSong(String md5, String title, String artist, String path) {
        this.md5 = md5;
        this.title = title;
        this.artist = artist;
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
