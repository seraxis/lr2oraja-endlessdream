package bms.player.beatoraja.external.DiscordRPC;

import java.io.IOException;
import java.nio.ByteBuffer;

interface IPCConnection {
    void connect() throws IOException;
    void write(ByteBuffer buffer) throws IOException;
    ByteBuffer read(int size) throws IOException;
    void close();
}
