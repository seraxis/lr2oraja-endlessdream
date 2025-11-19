package bms.player.beatoraja.external.DiscordRPC;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnixIPCConnection implements IPCConnection {
    private static final Logger logger = LoggerFactory.getLogger(UnixIPCConnection.class);
    private SocketChannel socket;

    @Override
    public void connect() throws IOException {
        String[] envVars = {"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};
        String basePath = null;

        for (String envVar : envVars) {
            basePath = System.getenv(envVar);
            if (basePath != null) break;
        }

        String ipcPath = (basePath != null ? basePath : "/tmp") + "/discord-ipc-0";
        socket = SocketChannel.open(StandardProtocolFamily.UNIX);
        socket.connect(UnixDomainSocketAddress.of(Path.of(ipcPath)));
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        while (buffer.hasRemaining()) {
            socket.read(buffer);
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
				logger.warn("Failed to close Unix socket: {}", e.getMessage());
            }
        }
    }
}
