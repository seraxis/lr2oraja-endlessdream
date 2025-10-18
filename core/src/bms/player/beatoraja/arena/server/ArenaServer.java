package bms.player.beatoraja.arena.server;

/**
 * A simple static wrapper of Server
 */
public class ArenaServer {
    private static Server server = null;

    public static void start() {
        stop();
        try {
            server = new Server();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
