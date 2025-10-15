package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;

import java.util.function.BiConsumer;

public class FunctionBar extends SelectableBar {
        private BiConsumer<MusicSelector, FunctionBar> function;
        private String title;
        private String subtitle;
        private int displayBarType;
        private int displayTextType;
        private SongData song = null;
        private Integer level = null;
        private int[] lamps = new int[0];

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

        public void setFunction(BiConsumer<MusicSelector, FunctionBar> f) { this.function = f; }
        public void setSongData(SongData song) { this.song = song; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        public void setLevel(int level) { this.level = level; }
        public void setLamps(int[] lamps) { this.lamps = lamps; }
        public void setDisplayBarType(int displayBarType) { this.displayBarType = displayBarType; }
        public void setDisplayTextType(int displayTextType) { this.displayTextType = displayTextType; }

        public void accept(MusicSelector selector) { this.function.accept(selector, this); }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public Integer getLevel() { return level; }
        public int getLamp(boolean dontCare) { return 0; }
        public int[] getLamps() { return lamps; }

        public int getDisplayBarType() { return displayBarType; }
        public int getDisplayTextType() { return displayTextType; }
    }
