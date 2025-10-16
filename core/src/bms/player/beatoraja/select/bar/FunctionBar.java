package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;

import java.util.function.BiConsumer;

public class FunctionBar extends SelectableBar {
    // bar appearance IDs
    public static final int STYLE_SONG = 0;
    public static final int STYLE_FOLDER = 1;
    public static final int STYLE_TABLE = 2;
    public static final int STYLE_COURSE = 3;
    public static final int STYLE_MISSING = 4;
    public static final int STYLE_SPECIAL = 5;
    public static final int STYLE_SEARCH = 6;

    public static final int STYLE_TEXT_PLAIN = 0;
    public static final int STYLE_TEXT_NEW = 1;
    public static final int STYLE_TEXT_MISSING = 8;

    private BiConsumer<MusicSelector, FunctionBar> function;
    private String title;
    private String subtitle;
    private int displayBarType;
    private int displayTextType;
    private SongData song = null;
    private Integer level = null;
    private int lamp = 0;
    private int[] lamps = new int[0];

    public FunctionBar(BiConsumer<MusicSelector, FunctionBar> f, String title, int displayBarType) {
        this(f, title, displayBarType, 0);
    }

    public FunctionBar(BiConsumer<MusicSelector, FunctionBar> f, String title, int displayBarType,
                       int displayTextType) {
        this.title = title;
        this.function = f;
        this.displayBarType = displayBarType;
        this.displayTextType = displayTextType;
    }

    public void setFunction(BiConsumer<MusicSelector, FunctionBar> f) { this.function = f; }
    public void setSongData(SongData song) { this.song = song; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setLevel(int level) { this.level = level; }
    public void setLamp(int lamp) { this.lamp = lamp; }
    public void setLamps(int[] lamps) { this.lamps = lamps; }
    public void setDisplayBarType(int displayBarType) { this.displayBarType = displayBarType; }
    public void setDisplayTextType(int displayTextType) { this.displayTextType = displayTextType; }

    public void accept(MusicSelector selector) { this.function.accept(selector, this); }
	@Override
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public Integer getLevel() { return level; }
	@Override
    public int getLamp(boolean isPlayer) { return this.lamp; }
    public int[] getLamps() { return lamps; }

    public int getDisplayBarType() { return displayBarType; }
    public int getDisplayTextType() { return displayTextType; }
}
