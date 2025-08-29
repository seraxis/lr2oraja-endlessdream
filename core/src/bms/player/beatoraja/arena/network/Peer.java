package bms.player.beatoraja.arena.network;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

import java.io.IOException;

public class Peer {
    private String userName;
    private String selectedMD5 = "";
    private boolean ready;
    private Score score = new Score();
    private int option;
    private int gauge;

    public Peer() {

    }

    public Peer(Value value) {
        ArrayValue arr = value.asArrayValue();
        this.userName = arr.get(0).asStringValue().asString();
        this.selectedMD5 = arr.get(1).asStringValue().asString();
        this.ready = arr.get(2).asBooleanValue().getBoolean();
        this.score = new Score(arr.get(3));
        this.option = arr.get(4).asIntegerValue().toInt();
        this.gauge = arr.get(5).asIntegerValue().toInt();
    }

    public byte[] pack() {
        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(6);
            packer.packString(userName);
            packer.packString(selectedMD5);
            packer.packBoolean(ready);
            packer.writePayload(score.pack());
            packer.packInt(option);
            packer.packInt(gauge);
            packer.close();
            return packer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        this.selectedMD5 = "";
        this.score = new Score();
        this.ready = false;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSelectedMD5() {
        return selectedMD5;
    }

    public void setSelectedMD5(String selectedMD5) {
        this.selectedMD5 = selectedMD5;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
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

    public int getExScore() {
        return this.score.getpGreat() * 2 + this.score.getGreat();
    }

    public int getBP() {
        return this.score.getBad() + this.score.getPoor();
    }

    public int getMaxCombo() {
        return this.score.getMaxCombo();
    }

    public float getRate() {
        return this.getScore().getCurrentNotes() == 0
                ? 0f
                : this.getExScore() * 50f / this.getScore().getCurrentNotes();
    }
}
