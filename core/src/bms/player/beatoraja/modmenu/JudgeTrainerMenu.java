package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.play.JudgeResult;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;

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
            renderJudgeCountTable();
            ImGui.end();
        }
    }

    private static void renderJudgeCountTable() {
        JudgeCountTracker tracker = JudgeTrainer.getJudgeCountTracker();
        int columnCount = tracker.getColumnCount();
        int[] scratchKeys = tracker.getScratchKeys();
        if (ImGui.beginTable("Judge Count", columnCount + 1, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupColumn("/");
            for (int i = 0; i < columnCount; i++) {
                int finalI = i;
                boolean isScratchKey = Arrays.stream(scratchKeys).anyMatch(lane -> lane == finalI);
                if (isScratchKey) {
                    ImGui.tableSetupColumn(String.format("%d(SC)", i));
                } else {
                    ImGui.tableSetupColumn(String.valueOf(i));
                }
            }
            ImGui.tableHeadersRow();
            // Judgements
            for (JudgeResult judgeResult : JudgeResult.values()) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.text(judgeResult.getName());
                for (int i = 0;i < columnCount; ++i) {
                    ImGui.tableNextColumn();
                    ImGui.text(String.valueOf(tracker.getCount(i, judgeResult)));
                }
            }
            // Fast/Slow
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Fast");
            for (int i = 0;i < columnCount; ++i) {
                ImGui.tableNextColumn();
                ImGui.text(String.valueOf(tracker.getFastCount(i)));
            }
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Slow");
            for (int i = 0;i < columnCount; ++i) {
                ImGui.tableNextColumn();
                ImGui.text(String.valueOf(tracker.getSlowCount(i)));
            }
            ImGui.endTable();
        }
    }
}
