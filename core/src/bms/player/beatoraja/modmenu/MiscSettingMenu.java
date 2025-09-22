package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

import java.util.Random;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class MiscSettingMenu {
    private static final ImInt NOTIFICATION_POSITION = new ImInt(0);

    public static void show(ImBoolean showMiscSetting) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Misc Settings", showMiscSetting, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.combo("Notification Positions", NOTIFICATION_POSITION, ImGuiNotify.NOTIFICATION_POSITIONS)) {
                ImGuiNotify.setNotificationPosition(NOTIFICATION_POSITION.get());
            }
            // TODO: Remove me!
            if (ImGui.button("TEST NOTIFICATION")) {
                Random random = new Random();
                int x = random.nextInt(4);
                switch (x) {
                    case 0 -> ImGuiNotify.info("I've had these jeans since I was born");
                    case 1 -> ImGuiNotify.error("And now they're ripped and now they're torn");
                    case 2 -> ImGuiNotify.warning("And all my friends have skateboards");
                    case 3 -> ImGuiNotify.success("I want a knife and a gun and things");
                }
            }
        }
        ImGui.end();
    }
}
