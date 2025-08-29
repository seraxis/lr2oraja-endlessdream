package bms.player.beatoraja.arena.network;

public class PackUtil {
    public static byte[] concat(byte id, byte[] payload) {
        byte[] out = new byte[payload.length + 1];
        out[0] = id;
        System.arraycopy(payload, 0, out, 1, payload.length);
        return out;
    }
}
