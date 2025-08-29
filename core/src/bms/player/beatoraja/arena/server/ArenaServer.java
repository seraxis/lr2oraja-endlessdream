package bms.player.beatoraja.arena.server;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import imgui.type.ImBoolean;

import java.util.logging.Logger;

/**
 * A simple static wrapper of server instance of JLR2ArenaEx
 */
public class ArenaServer {
    private static io.github.catizard.jlr2arenaex.server.ArenaServer server = null;
    private static boolean serverAutoRotate = false;
    public static final ImBoolean serverStarted = new ImBoolean(false);

    public static void start() {
        stop();
        try {
            server = new io.github.catizard.jlr2arenaex.server.ArenaServer();
            server.setExceptionHandler((e) -> {
                e.printStackTrace();
                String err = String.format("Server internal error: %s", e.getMessage());
                Logger.getGlobal().severe(err);
                ImGuiNotify.error(err);
                stop();
            });
            server.start();
            serverStarted.set(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        if (server != null) {
            try {
                server.stop();
                serverStarted.set(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void setServerAutoRotate(boolean value) {
        if (server != null) {
            server.setAutoRotateHost(value);
            serverAutoRotate = value;
        }
    }

    public static boolean getServerAutoRotate() {
        return serverAutoRotate;
    }
}
