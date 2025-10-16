package bms.player.beatoraja.arena.network;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

import java.io.IOException;

/**
 * Server -> Client score message
 */
public class ScoreMessage {
    private Score score;
    private Address player;

    public ScoreMessage() {

    }

    public ScoreMessage(Score score, Address player) {
        this.score = score;
        this.player = player;
    }

    public ScoreMessage(Value value) {
        ArrayValue arr = value.asArrayValue();
        this.score = new Score(arr.get(0));
        this.player = new Address(arr.get(1));
    }

    public byte[] pack() {
        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(2);
            packer.writePayload(score.pack());
            packer.writePayload(player.pack());
            packer.close();
            return packer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Address getPlayer() {
        return player;
    }

    public void setPlayer(Address player) {
        this.player = player;
    }
}
