package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.arena.client.ArenaBar;
import bms.player.beatoraja.arena.client.Client;
import bms.player.beatoraja.arena.lobby.Lobby;
import bms.player.beatoraja.arena.server.ArenaServer;
import bms.player.beatoraja.arena.server.Server;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;
import imgui.ImGui;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

public class ArenaMenu {
    public static boolean isFocused = false;
    public static boolean isShow = false;
    private static MusicSelector selector;

    public static void setMusicSelector(MusicSelector selector) {
        ArenaMenu.selector = selector;
    }

    public static void show(ImBoolean showArenaMenu) {
        isShow = showArenaMenu.get();
        if (!isShow) {
            isFocused = false;
        }
        // This tweak must be called in game's main thread, otherwise the game crashes immediately
        // because we cannot dispose a texture outside of glfw context
        if (Client.state.getAutoSelectFlag()) {
            selectCurrentLobbySong();
            // There's a risk of race condition
            Client.state.setAutoSelectFlag(false);
        }
        ImGui.begin("EndlessDream ArenaEX", showArenaMenu, ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoBringToFrontOnFocus);
        {
            isFocused = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows);
            if (ImGui.beginTabBar("TabBar")) {
                if (ImGui.beginTabItem("Client")) {
                    if (ImGui.collapsingHeader("Connect", ImGuiTreeNodeFlags.DefaultOpen)) {
                        ImGui.inputText("Username", Client.userName);
                        ImGui.inputText("Server", Client.host);

                        ImGui.beginDisabled(Client.host.isEmpty() || Client.userName.isEmpty());
                        if (ImGui.button("Connect##Button")) {
                            Client.connect(Client.host.get(), Client.userName.get());
                        }
                        ImGui.endDisabled();

                        ImGui.sameLine();

                        ImGui.beginDisabled(!Client.connected.get());
                        if (ImGui.button("Disconnect")) {
                            Client.disconnect();
                        }
                        ImGui.endDisabled();
                    }
                    ImGui.endTabItem();
                    if (Client.connected.get()) {
                        ImGui.separator();
                        Lobby.render();
                    }
                }

                if (ImGui.beginTabItem("Server")) {
                    ImGui.text("Server");
                    ImGui.separator();

                    ImGui.beginDisabled(Server.started.get());
                    if (ImGui.button("Start")) {
                        ArenaServer.start();
                    }
                    ImGui.endDisabled();

                    ImGui.beginDisabled(!Server.started.get());
                    if (ImGui.button("Stop")) {
                        ArenaServer.stop();
                    }
                    ImGui.endDisabled();
                    ImGui.checkbox("Auto-rotate host after each song", Server.autoRotateHost);
                    ImGui.endTabItem();
                }
                ImGui.endTabBar();
            }
            ImGui.end();
        }
    }

    public static void selectCurrentLobbySong() {
        SongData songData = Client.state.getCurrentSongData();
        if (songData != null) {
            ArenaBar bar = new ArenaBar(selector, songData);
            selector.getBarManager().replaceArenaSelection(bar);
            selector.getBarManager().updateBar();
            selector.getBarManager().setSelected(bar);
            // This line might break something if we're not currently at music select scene
            selector.getBarManager().updateBar(bar);
        }
    }
}
