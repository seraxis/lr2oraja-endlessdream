package bms.player.beatoraja.arena.lobby;


import bms.player.beatoraja.arena.client.Client;
import bms.player.beatoraja.arena.enums.ClientToServer;
import bms.player.beatoraja.arena.network.Address;
import bms.player.beatoraja.modmenu.ArenaMenu;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class Lobby {
    private static final float userListWidth = 150;
    private static final float userListHeight = 0;
    private static final float mainWindowWidth = 300;
    private static final float mainWindowHeight = 400;
    private static boolean scrollToBottom = false;
    private static final List<LogMessage> lines = new CopyOnWriteArrayList<>();
    private static final ImString inputBuf = new ImString(128);

    public static void render() {
        ImGui.text("Lobby");
        ImGui.separator();
        ImGui.beginChild("Users", userListWidth, userListHeight, true, ImGuiWindowFlags.AlwaysAutoResize);
        Client.state.getPeers().forEach((address, peer) -> {
            String userName = peer.getUserName();
            if (address.equals(Client.state.getHost())) {
                userName = "L " + userName;
            }
            if (peer.isReady() && Objects.equals(Client.state.getPeers().get(Client.state.getHost()).getSelectedMD5(), peer.getSelectedMD5())) {
                userName = "R " + userName;
            }
            ImGui.selectable(userName);
            if (Client.state.getHost().equals(Client.state.getRemoteId()) && ImGui.beginPopupContextItem()) {
                ImGui.textDisabled(String.format("Selected user: %s", peer.getUserName()));
                if (ImGui.menuItem("Give host")) {
                    Client.send(ClientToServer.CTS_SET_HOST, address.pack());
                }
                if (ImGui.menuItem("Kick")) {
                    Client.send(ClientToServer.CTS_KICK_USER, address.pack());
                }
                ImGui.endPopup();
            }
        });
        ImGui.endChild();
        ImGui.sameLine();
        ImGui.beginGroup();
        ImGui.beginChild("Main view", mainWindowWidth, mainWindowHeight);
        if (ImGui.button("@")) {
            ImGui.setClipboardText("http://www.dream-pro.info/~lavalse/LR2IR/search.cgi?mode=ranking&bmsmd5=" + Client.state.getSelectedSongRemote().getMd5());
            ImGuiNotify.info("Copied LR2IR link to clipboard!");
            // TODO: gui::widgets::Tooltip("Copy LR2IR link to clipboard");
        }

        float buttonWidth = ImGui.calcTextSize("@").x + ImGui.getStyle().getFramePaddingX() * 2;
        float fontSize = ImGui.getFontSize();
        float gapSize = fontSize / 2.0F;

        ImGui.sameLine(0.0F, gapSize);
        ImGui.pushItemWidth(mainWindowWidth - (fontSize * 3) - buttonWidth - gapSize);
        ImGui.inputText(
                "Title",
                new ImString(Client.state.getSelectedSongRemote().getTitle()),
                ImGuiInputTextFlags.ReadOnly
        );
        ImGui.popItemWidth();

        ImGui.pushItemWidth(mainWindowWidth - (fontSize * 3));
        ImGui.inputText("Artist", new ImString(Client.state.getSelectedSongRemote().getArtist()), ImGuiInputTextFlags.ReadOnly);
        // NOTE: When trying to jump to the arena bar automatically, current scene should be musicselector
        // otherwise the jump won't work
        if (ImGui.button("Jump")) {
            ArenaMenu.selectCurrentLobbySong();
        }
        buttonWidth = ImGui.calcTextSizeX("Jump") + ImGui.getStyle().getFramePaddingX() * 2;

        ImGui.sameLine();
        ImGui.pushItemWidth(mainWindowWidth - (fontSize * 3) - buttonWidth - gapSize);
        ImGui.inputText("Path", new ImString(Client.state.getSelectedSongRemote().getPath()), ImGuiInputTextFlags.ReadOnly);
        ImGui.popItemWidth();

        ImGui.separator();
        if (ImGui.beginTabBar("##Tabs")) {
            if (ImGui.beginTabItem("Chat")) {
                float footer_height_to_reserve = ImGui.getStyle().getItemSpacingY() + ImGui.getFrameHeightWithSpacing();
                if (ImGui.beginChild("ScrollingRegion", 0, -footer_height_to_reserve, false, ImGuiWindowFlags.HorizontalScrollbar)) {
                    ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4, 1);
                    ImGui.pushTextWrapPos(ImGui.getCursorPosX() + mainWindowWidth - 20);
                    lines.forEach(line -> {
                        if (line.msg().isEmpty()) {
                            return;
                        }
                        Optional<ImVec4> color = Optional.empty();
                        if (line.msg().startsWith("[#] ")) {
                            color = Optional.of(new ImVec4(1.0f, 0.8f, 0.6f, 1.0f));
                        } else if (line.msg().startsWith("[!] ")) {
                            color = Optional.of(new ImVec4(1.0f, 0.4f, 0.4f, 1.0f));
                        }
                        color.ifPresent(v4 -> ImGui.pushStyleColor(ImGuiCol.Text, v4.x, v4.y, v4.z, v4.w));
                        ImGui.textUnformatted(line.msg());
                        color.ifPresent(v4 -> ImGui.popStyleColor());
                    });
                    ImGui.popTextWrapPos();

                    if (scrollToBottom || (ImGui.getScrollY() >= ImGui.getScrollMaxY())) {
                        ImGui.setScrollHereY(1.0f);
                    }
                    scrollToBottom = false;

                    ImGui.popStyleVar();
                }
                ImGui.endChild();
                ImGui.separator();

                boolean reclaimFocus = false;
                ImGui.pushItemWidth(mainWindowWidth);
                if (ImGui.inputText("##Input", inputBuf, ImGuiInputTextFlags.EnterReturnsTrue)) {
                    processInput();
                    reclaimFocus = true;
                }
                ImGui.popItemWidth();

                ImGui.setItemDefaultFocus();
                if (reclaimFocus) {
                    ImGui.setKeyboardFocusHere(-1); // Auto focus previous widget
                }

                ImGui.sameLine();
                // TODO: We cannot have the send button here since we cannot grab the text
//                if (ImGui.button("Send")) {
//                    // Prevent double send if pressing Enter and clicking the button on the same frame (unlikely)
//                    if (reclaimFocus) {
//                        processInput();
//                    }
//                }

                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Settings##Lobby")) {
                ImGui.text("Player settings");
                ImGui.separator();
                // ImGui.checkBox("Enable random flip", & hooks::random::random_flip);
                ImGui.endTabItem();

                ImGui.text("Lobby settings");
                ImGui.separator();
                ImGui.beginDisabled(!Client.state.getHost().equals(Client.state.getRemoteId()));
                // ImGui.checkBox("Enable item / ojama mode", & hooks::maniac::itemModeEnabled);
                ImGui.sameLine();
                // TODO: widgets::HelpMarker ("Throw modifiers at your opponents while playing to add some spice!");
                if (ImGui.button("Item settings..."))
                    ImGui.openPopup("Item settings");
                // TODO: gui::item_settings::Render();
                ImGui.endDisabled();
            }
            ImGui.endTabBar();
        }
        ImGui.endChild();
        ImGui.endGroup();
    }

    public static void addToLogWithUser(String s, Address id) {
        String userName = Client.state.getPeers().get(id).getUserName();
        LogMessage msg = new LogMessage(String.format("%s: %s\n", userName, s), false);
        int f = lines.size() - 1;
        if (lines.isEmpty() || lines.get(f).isSystemMsg()) {
            lines.add(msg);
        } else {
            // WTF?!
            lines.set(f, new LogMessage(lines.get(f).msg().concat(msg.msg()), lines.get(f).isSystemMsg()));
        }

        // Show notification if main menu is not shown
        if (!ArenaMenu.isShow) {
            String notifyText = msg.msg().substring(4);
            if (msg.msg().startsWith("[#] ")) {
                ImGuiNotify.info(notifyText);
            }
            if (msg.msg().startsWith("[!] ")) {
                ImGuiNotify.error(notifyText);
            }
        }
    }

    /**
     * AddToLog is basically only called for system messages
     */
    public static void addToLog(String s) {
        LogMessage msg = new LogMessage(s, true);
        lines.add(msg);

        // Show notification if main menu is not shown
        if (!ArenaMenu.isShow) {
            String notifyText = msg.msg().substring(4);
            if (msg.msg().startsWith("[#] ")) {
                ImGuiNotify.info(notifyText);
            }
            if (msg.msg().startsWith("[!] ")) {
                ImGuiNotify.error(notifyText);
            }
        }
    }

    private static void processInput() {
        String r = inputBuf.get().trim();
        if (!r.isEmpty()) {
            addToLogWithUser(r, Client.state.getRemoteId());
            Client.send(ClientToServer.CTS_MESSAGE, r.getBytes());
        }
        inputBuf.clear();
        scrollToBottom = true;
    }
}
