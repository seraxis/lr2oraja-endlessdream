package bms.player.beatoraja.arena.client;

import bms.player.beatoraja.arena.enums.ClientToServer;
import bms.player.beatoraja.arena.network.PeerList;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.msgpack.value.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Client {
    public static ImString userName = new ImString("", 128);
    public static ImBoolean connected = new ImBoolean(false);
    public static ImString host = new ImString("", 128);
    public static ClientState state = new ClientState();
    private static WSClient wsClient = null;
    private static Consumer<Boolean> accepter = null;

    public static void connect(String host, String userName) {
        disconnect();
        if (connected.get()) {
            destroy();
        }

        URI uri = null;
        try {
            uri = new URI(String.format("ws://%s:2222/", host));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            ImGuiNotify.error("failed to connect the server: wrong uri pattern");
            return;
        }
        wsClient = new WSClient(uri);
        wsClient.setConnectionLostTimeout(0);
        wsClient.connect();
    }

    public static void disconnect() {
        stop();
    }

    public static void stop() {
        if (wsClient != null) {
            wsClient.close();
            wsClient = null;
        }
    }

    /**
     * Destroy the previous websocket client if exist
     */
    public static void destroy() {
        if (connected.get()) {
            connected.set(false);
            state = new ClientState();
            // hooks::pacemaker::Destroy(); // Restore original bytes for pacemaker
            ImGuiNotify.info("Disconnected from the server");
        }
    }

    /**
     * A static wrapper for easier access
     */
    public static void send(ClientToServer id, byte[] data) {
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.send(id, data);
        }
    }

    /**
     * Sync current room's peers with server
     */
    public static void updatePeerState(Value value) {
        updatePeerList(value);

        Logger.getGlobal().info("[+] Connected users:");
        Client.state.getPeers().forEach((address, peer) -> {
            Logger.getGlobal().info("- " + peer.getUserName());
        });
    }

    /**
     * Similar to updatePeerState, but triggered when someone changed ready status
     *
     * @param value server's state
     */
    public static void updateReadyState(Value value) {
        updatePeerList(value);

        String selectedMD5 = Client.state.getPeers().entrySet().stream().findAny().get().getValue().getSelectedMD5();
        boolean allReady = Client.state.getPeers()
                .entrySet()
                .stream()
                .allMatch(e -> e.getValue().isReady() && e.getValue().getSelectedMD5().equals(selectedMD5));
        if (allReady) {
            Logger.getGlobal().info("[+] All player in lobby are ready");
            if (accepter != null) {
                accepter.accept(true);
            }
        }
    }

    /**
     * Update state's peers & host
     *
     * @param value message from server, should be an instance of PeerList
     */
    private static void updatePeerList(Value value) {
        PeerList peerList = new PeerList(value);
        Client.state.setPeers(peerList.getList());
        Client.state.setHost(peerList.getHost());
    }

    public static void acceptNextAllReady(Consumer<Boolean> setter) {
        accepter = setter;
    }
}

