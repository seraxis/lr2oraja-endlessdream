package bms.player.beatoraja.modmenu;

import bms.model.Mode;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.PlayConfig;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;

import java.util.Arrays;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class MiscSettingMenu {
    private static MainController main;

    // Some of the settings are based on play mode
    // WARN: PLAY_MODE_VALUE has an initial value, 1 -> BEAT_7K
    private static final ImInt PLAY_MODE_VALUE = new ImInt(1);
    private static final String[] PLAY_MODE_OPTIONS = Arrays.stream(Mode.values())
            .map(mode -> mode.hint)
            .toArray(String[]::new);
    // This is the true play mode value, PLAY_MODE_VALUE is for rendering
    private static Mode CURRENT_PLAY_MODE = null;

    private static final ImInt NOTIFICATION_POSITION = new ImInt(0);
    private static final ImBoolean ENABLE_LIFT = new ImBoolean(false);
    private static final ImInt LIFT_VALUE = new ImInt(0);
    private static final ImBoolean ENABLE_HIDDEN = new ImBoolean(false);
    private static final ImInt HIDDEN_VALUE = new ImInt(0);
    private static final ImBoolean ENABLE_LANECOVER = new ImBoolean(false);
    private static final ImFloat LANE_COVER_MARGIN_LOW = new ImFloat(0);
    private static final ImFloat LANE_COVER_MARGIN_HIGH = new ImFloat(0);
    private static final ImInt LANE_COVER_SWITCH_DURATION = new ImInt(0);
    private static final ImBoolean ENABLE_CONSTANT = new ImBoolean(false);
    private static final ImInt CONSTANT_VALUE = new ImInt(0);

    public static void show(ImBoolean showMiscSetting) {
        // TODO: We can setup preferred game mode here in future
        if (CURRENT_PLAY_MODE == null) {
            changePlayMode(Mode.BEAT_7K);
        }

        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Misc Settings", showMiscSetting, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.combo("Notification Positions", NOTIFICATION_POSITION, ImGuiNotify.NOTIFICATION_POSITIONS)) {
                ImGuiNotify.setNotificationPosition(NOTIFICATION_POSITION.get());
            }

            // Below settings are depending on different play mode
            if (ImGui.combo("Play Mode", PLAY_MODE_VALUE, PLAY_MODE_OPTIONS)) {
                changePlayMode(Mode.getMode(PLAY_MODE_OPTIONS[PLAY_MODE_VALUE.get()]));
            }

            if (ImGui.checkbox("Enable Lift", ENABLE_LIFT)) {
                getPlayConfig().setEnablelift(ENABLE_LIFT.get());
            }
            ImGui.sameLine();
            if (ImGui.inputInt("Lift Value", LIFT_VALUE)) {
                getPlayConfig().setLift(LIFT_VALUE.get() / 1000f);
            }

            if (ImGui.checkbox("Enable Hidden", ENABLE_HIDDEN)) {
                getPlayConfig().setEnablehidden(ENABLE_HIDDEN.get());
            }
            ImGui.sameLine();
			if (ImGui.inputInt("Hidden Value", HIDDEN_VALUE)) {
				getPlayConfig().setHidden(HIDDEN_VALUE.get() / 1000f);
			}

			if (ImGui.checkbox("Enable LaneCover", ENABLE_LANECOVER)) {
				getPlayConfig().setEnablelanecover(ENABLE_LANECOVER.get());
			}
            if (ImGui.inputFloat("Lane Cover Margin(low)", LANE_COVER_MARGIN_LOW)) {
                getPlayConfig().setLanecovermarginlow(LANE_COVER_MARGIN_LOW.get());
            }
            if (ImGui.inputFloat("Lane Cover Margin(high)", LANE_COVER_MARGIN_HIGH)) {
                getPlayConfig().setLanecovermarginhigh(LANE_COVER_MARGIN_HIGH.get());
            }
            if (ImGui.inputInt("Lane Cover Switch Duration", LANE_COVER_SWITCH_DURATION)) {
                getPlayConfig().setLanecoverswitchduration(LANE_COVER_SWITCH_DURATION.get());
            }

            if (ImGui.checkbox("Enable Constant", ENABLE_CONSTANT)) {
                getPlayConfig().setEnableConstant(ENABLE_CONSTANT.get());
            }
            ImGui.sameLine();
            if (ImGui.inputInt("Constant Fade-in", CONSTANT_VALUE)) {
                getPlayConfig().setConstantFadeinTime(CONSTANT_VALUE.get());
            }
        }
        ImGui.end();
    }

    public static void setMain(MainController main) {
        MiscSettingMenu.main = main;
    }

    /**
     * Get current play mode(5k, 7k...) config, a simple wrapper around MainController
     */
    private static PlayConfig getPlayConfig() {
        return main.getPlayerConfig().getPlayConfig(CURRENT_PLAY_MODE).getPlayconfig();
    }

    /**
     * Change current play mode, resetting related options
     */
    private static void changePlayMode(Mode mode) {
        CURRENT_PLAY_MODE = mode;
        PlayConfig conf = getPlayConfig();

        ENABLE_LIFT.set(conf.isEnablelift());
        LIFT_VALUE.set((int) (conf.getLift() * 1000));

        ENABLE_HIDDEN.set(conf.isEnablehidden());
        HIDDEN_VALUE.set((int) (conf.getHidden() * 1000));

        ENABLE_LANECOVER.set(conf.isEnablelanecover());
        LANE_COVER_MARGIN_LOW.set(conf.getLanecovermarginlow());
        LANE_COVER_MARGIN_HIGH.set(conf.getLanecovermarginhigh());
        LANE_COVER_SWITCH_DURATION.set(conf.getLanecoverswitchduration());

        ENABLE_CONSTANT.set(conf.isEnableConstant());
        CONSTANT_VALUE.set(conf.getConstantFadeinTime());
    }
}
