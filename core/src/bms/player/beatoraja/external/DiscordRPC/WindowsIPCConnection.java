package bms.player.beatoraja.external.DiscordRPC;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.io.IOException;
import java.nio.ByteBuffer;

class WindowsIPCConnection implements IPCConnection {
    private static final String PIPE_PATH = "\\\\.\\pipe\\discord-ipc-0";
    private WinNT.HANDLE pipeHandle;

    @Override
    public void connect() throws IOException {
        pipeHandle = Kernel32.INSTANCE.CreateFile(PIPE_PATH,
                Kernel32.GENERIC_READ | Kernel32.GENERIC_WRITE,
                0, null, Kernel32.OPEN_EXISTING, 0, null);

        if (pipeHandle == null || WinNT.INVALID_HANDLE_VALUE.equals(pipeHandle)) {
            throw new IOException("Failed to connect to Discord IPC pipe");
        }
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        IntByReference bytesWritten = new IntByReference();

        if (!Kernel32.INSTANCE.WriteFile(pipeHandle, data, data.length, bytesWritten, null)) {
            throw new IOException("Failed to write to Discord IPC pipe");
        }
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        byte[] data = new byte[size];
        IntByReference bytesRead = new IntByReference();

        if (!Kernel32.INSTANCE.ReadFile(pipeHandle, data, data.length, bytesRead, null)) {
            throw new IOException("Failed to read from Discord IPC pipe");
        }

        return ByteBuffer.wrap(data, 0, bytesRead.getValue());
    }

    @Override
    public void close() {
        if (pipeHandle != null && !WinNT.INVALID_HANDLE_VALUE.equals(pipeHandle)) {
            Kernel32.INSTANCE.CloseHandle(pipeHandle);
        }
    }
}
