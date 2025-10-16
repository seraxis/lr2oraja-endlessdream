package bms.player.beatoraja.arena.network;

import bms.model.BMSModel;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

public class SelectedBMSMessage {
    private int randomSeed;
    private String md5;
    private String title;
    private String artist;
    private int option;
    private int gauge;
    private boolean itemModeEnabled;

    public SelectedBMSMessage() {

    }

    public SelectedBMSMessage(int randomSeed, String md5, String title, String artist, int option, int gauge, boolean itemModeEnabled) {
        this.randomSeed = randomSeed;
        this.md5 = md5;
        this.title = title;
        this.artist = artist;
        this.option = option;
        this.gauge = gauge;
        this.itemModeEnabled = itemModeEnabled;
    }

    public SelectedBMSMessage(BMSModel model, long randomSeed, int option) {
        // TODO: random seed, items are not supported. Although we passed the random seed here, it's not a LR2 seed but
        // a raja seed, it needs to be transformed
        // NOTE: Gauge isn't synced everytime, considering 99% raja users are using auto-shift, there's no reason
        // to sync an initial gauge value. Also LR2 has a different gauge system definition, it's tedious to handle
        // the assist clear & ex-hard etc
        this((int) randomSeed, model.getMD5(), model.getTitle(), model.getArtist(), option, 0, false);
    }

    public SelectedBMSMessage(Value value) {
        ArrayValue arr = value.asArrayValue();
        this.randomSeed = arr.get(0).asIntegerValue().asInt();
        this.md5 = arr.get(1).asStringValue().asString();
        this.title = arr.get(2).asStringValue().asString();
        this.artist = arr.get(3).asStringValue().asString();
        this.option = arr.get(4).asIntegerValue().asInt();
        this.gauge = arr.get(5).asIntegerValue().toInt();
        this.itemModeEnabled = arr.get(6).asBooleanValue().getBoolean();
    }

    public byte[] pack() {
        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(7);
            packer.packInt(this.randomSeed);
            packer.packString(md5);
            packer.packString(title);
            packer.packString(artist);
            packer.packInt(option);
            packer.packInt(gauge);
            packer.packBoolean(itemModeEnabled);
            packer.close();
            return packer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
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

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public int getGauge() {
        return gauge;
    }

    public void setGauge(int gauge) {
        this.gauge = gauge;
    }

    public boolean isItemModeEnabled() {
        return itemModeEnabled;
    }

    public void setItemModeEnabled(boolean itemModeEnabled) {
        this.itemModeEnabled = itemModeEnabled;
    }
}
