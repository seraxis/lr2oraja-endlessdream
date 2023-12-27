package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.Arrays;
import java.util.List;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

public class FreqTrainerMenu {

    public static ImBoolean FREQ_TRAINER_ENABLED = new ImBoolean(false);

    private static int[] freq = new int[] {100};

    private static List<Integer> buttonVals = Arrays.asList(-10, -5, -1, 100, 1, 5, 10);

    public static void show(ImBoolean showFreqTrainer) {
        float relativeX = windowWidth * 0.47f;
        float relativeY = windowHeight * 0.06f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if(ImGui.begin("Rate Modifier", showFreqTrainer, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Modifies the chart playback rate to be faster or");
            ImGui.text("slower by a given percent.");

            buttonVals.forEach(value -> {
                if (value == 100) {
                    if(ImGui.button("Reset")) {
                        freq[0] = 100;
                    }
                } else {
                    if(ImGui.button((value > 0 ? "+" : "") + value + "%")) {
                        freq[0] = clamp(freq[0] + value);
                    }
                }
                ImGui.sameLine();
            });
            ImGui.newLine();
            ImGui.sliderInt("%",
                    freq,
                    50,
                    200);

            ImGui.text("Controls");
            ImGui.indent();
            ImGui.checkbox("Rate Enabled", FREQ_TRAINER_ENABLED);
            ImGui.sameLine();
            helpMarker("When enabled positive rate scores will save locally, however scores will not submit to IR and result lamp will always be NO PLAY.");

            freq[0] = clamp(freq[0]);
        }
        ImGui.end();
    }

    private static int clamp(int result) {
        return Math.max(50, Math.min(200, result));
    }

    public static boolean isFreqTrainerEnabled() {
        return FREQ_TRAINER_ENABLED.get();
    }

    public static int getFreq() {
        return freq[0];
    }

    public static boolean isFreqNegative() {
        return freq[0] < 100;
    }

    public static String getFreqString() {
        String rate = String.format("%.02f", (freq[0] / 100.0f));
        return "[" + rate + "x]";
    }


}
