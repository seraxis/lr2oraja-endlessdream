package bms.player.beatoraja.arena.network;

import bms.player.beatoraja.ScoreData;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

public class Score {
    private int poor;
    private int bad;
    private int good;
    private int great;
    private int pGreat;
    private int maxCombo;
    private int score;
    private int currentNotes;

    public Score() {

    }

    public Score(ScoreData scoreData) {
        this.poor = scoreData.getEpr() + scoreData.getLpr();
        this.bad = scoreData.getEbd() + scoreData.getLbd();
        this.good = scoreData.getEgd() + scoreData.getLgd();
        this.great = scoreData.getEgr() + scoreData.getLgr();
        this.pGreat = scoreData.getEpg() + scoreData.getLpg();
        this.maxCombo = scoreData.getCombo();
        this.score = scoreData.getExscore();
        this.currentNotes = this.poor + this.bad + this.good + this.great + this.pGreat;
    }

    public Score(Value value) {
        ArrayValue arr = value.asArrayValue();
        this.poor = arr.get(0).asIntegerValue().toInt();
        this.bad = arr.get(1).asIntegerValue().toInt();
        this.good = arr.get(2).asIntegerValue().toInt();
        this.great = arr.get(3).asIntegerValue().toInt();
        this.pGreat = arr.get(4).asIntegerValue().toInt();
        this.maxCombo = arr.get(5).asIntegerValue().toInt();
        this.score = arr.get(6).asIntegerValue().toInt();
        this.currentNotes = arr.get(7).asIntegerValue().toInt();
    }

    public byte[] pack() {
        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(8);
            packer.packInt(this.poor);
            packer.packInt(this.bad);
            packer.packInt(this.good);
            packer.packInt(this.great);
            packer.packInt(this.pGreat);
            packer.packInt(this.maxCombo);
            packer.packInt(this.score);
            packer.packInt(this.currentNotes);
            packer.close();
            return packer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int getPoor() {
        return poor;
    }

    public void setPoor(int poor) {
        this.poor = poor;
    }

    public int getBad() {
        return bad;
    }

    public void setBad(int bad) {
        this.bad = bad;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getGreat() {
        return great;
    }

    public void setGreat(int great) {
        this.great = great;
    }

    public int getpGreat() {
        return pGreat;
    }

    public void setpGreat(int pGreat) {
        this.pGreat = pGreat;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public void setMaxCombo(int maxCombo) {
        this.maxCombo = maxCombo;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCurrentNotes() {
        return currentNotes;
    }

    public void setCurrentNotes(int currentNotes) {
        this.currentNotes = currentNotes;
    }
}
