package bms.player.beatoraja.arena.network;

import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

/**
 * Server -> Client message
 */
public class Message {
    private String message;
    private Address player;
    private boolean systemMessage;

    public Message() {

    }

    public Message(Value value) {
        ArrayValue arr = value.asArrayValue();
        this.message = arr.get(0).asStringValue().asString();
        this.player = new Address(arr.get(1));
        this.systemMessage = arr.get(2).asBooleanValue().getBoolean();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Address getPlayer() {
        return player;
    }

    public void setPlayer(Address player) {
        this.player = player;
    }

    public boolean isSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(boolean systemMessage) {
        this.systemMessage = systemMessage;
    }
}
