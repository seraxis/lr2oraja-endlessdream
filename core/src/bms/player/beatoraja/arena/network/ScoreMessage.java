package bms.player.beatoraja.arena.network;

import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

/**
 * Server -> Client score message
 */
public class ScoreMessage {
    private Score score;
    private Address player;

    public ScoreMessage() {

    }

    public ScoreMessage(Value value) {
        ArrayValue arr = value.asArrayValue();
        this.score = new Score(arr.get(0));
        this.player = new Address(arr.get(1));
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
