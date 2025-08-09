package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class JudgeTrainerMenu {
    private static ImBoolean OVERRIDE_CHART_JUDGE = new ImBoolean(false);
    private static ImInt OVERRIDE_JUDGE_RANK = new ImInt(0);

    public static void show(ImBoolean showJudgeTrainer) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Judge Trainer", showJudgeTrainer, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.checkbox("Override chart's judge", OVERRIDE_CHART_JUDGE)) {
                JudgeTrainer.setActive(OVERRIDE_CHART_JUDGE.get());
            }
            if (ImGui.combo("judge", OVERRIDE_JUDGE_RANK, JudgeTrainer.JUDGE_OPTIONS)) {
                JudgeTrainer.setJudgeRank(OVERRIDE_JUDGE_RANK.get());
            }
            ImGui.end();
        }
    }
}
