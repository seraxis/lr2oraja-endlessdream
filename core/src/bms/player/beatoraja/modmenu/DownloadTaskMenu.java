package bms.player.beatoraja.modmenu;

import bms.tool.mdprocessor.DownloadTask;
import bms.player.beatoraja.modmenu.DownloadTaskState;
import bms.tool.mdprocessor.HttpDownloadProcessor;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.List;
import java.util.Map;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class DownloadTaskMenu {
    public static final int MAXIMUM_TASK_NAME_LENGTH = 10;

    private static HttpDownloadProcessor processor;

    public static void setProcessor(HttpDownloadProcessor processor) {
        DownloadTaskMenu.processor = processor;
    }

    public static void show(ImBoolean showDownloadTasksWindow) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Download Tasks", showDownloadTasksWindow, ImGuiWindowFlags.AlwaysAutoResize)) {
            Map<Integer, DownloadTask> running = DownloadTaskState.runningDownloadTasks;
            Map<Integer, DownloadTask> expired = DownloadTaskState.expiredTasks;
            if (running.isEmpty() && expired.isEmpty()) {
                ImGui.text("No Download Task. Try selecting missing bms to submit new task!");
            } else {
                if (ImGui.beginTabBar("DownloadTasksTabBar")) {
                    if (ImGui.beginTabItem("Running")) {
                        renderTaskTable(running.values().stream().toList());
                        ImGui.endTabItem();
                    }
                    if (ImGui.beginTabItem("Expired")) {
                        renderTaskTable(expired.values().stream().toList());
                        ImGui.endTabItem();
                    }

                    ImGui.endTabBar();
                }
            }
        }
        ImGui.end();
    }

    private static void renderTaskTable(List<DownloadTask> tasks) {
        if (ImGui.beginTable("DownloadTaskTable", 3, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("Task");
            ImGui.tableSetupColumn("Progress");
            ImGui.tableSetupColumn("Op");
            ImGui.tableHeadersRow();
            for (DownloadTask task : tasks) {
                ImGui.tableNextRow();
                ImGui.pushID(task.getId());

                ImGui.tableSetColumnIndex(0);
                String taskName = task.getName().substring(0, Math.min(task.getName().length(), MAXIMUM_TASK_NAME_LENGTH));
                ImGui.text(String.format("%s (%s)", taskName, task.getDownloadTaskStatus().getName()));

                ImGui.tableSetColumnIndex(1);
                String errorMessage = task.getErrorMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    ImGui.text(String.format("%s/%s", humanizeFileSize(task.getDownloadSize()), humanizeFileSize(task.getContentLength())));
                } else {
                    ImGui.textColored(ImColor.rgb(255, 0, 0), errorMessage);
                }

                ImGui.tableSetColumnIndex(2);
                if (task.getDownloadTaskStatus() == DownloadTask.DownloadTaskStatus.Error) {
                    if (ImGui.button("Retry")) {
                        processor.retryDownloadTask(task);
                    }
                }

                ImGui.popID();
            }

            ImGui.endTable();
        }
    }

    public static String humanizeFileSize(long bytes) {
        int thresh = 1000;
        if (Math.abs(bytes) < thresh) {
            return String.format("%d B", bytes);
        }

        double result = bytes;
        final String[] units = new String[]{"KB", "MB", "GB", "TB"};
        int u = -1, r = 100;

        do {
            result /= thresh;
            ++u;
        } while (Math.round(Math.abs(result) * r) / r >= thresh && u < units.length - 1);

        return String.format("%.1f %s", result, units[u]);
    }
}
