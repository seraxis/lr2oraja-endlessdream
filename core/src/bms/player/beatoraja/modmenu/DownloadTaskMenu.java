package bms.player.beatoraja.modmenu;

import bms.tool.mdprocessor.DownloadTask;
import bms.player.beatoraja.modmenu.DownloadTaskState;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.Map;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class DownloadTaskMenu {
    public static final int MAXIMUM_TASK_NAME_LENGTH = 10;

    public static void show(ImBoolean showDownloadTasksWindow) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Download Tasks", showDownloadTasksWindow, ImGuiWindowFlags.AlwaysAutoResize)) {
            Map<Integer, DownloadTask> running = DownloadTaskState.runningDownloadTasks;
            Map<Integer, DownloadTask> expired = DownloadTaskState.expiredTasks;
            if (running.isEmpty() && expired.isEmpty()) {
                ImGui.text("No Download Task. Try selecting missing bms to submit new task!");
            }
            else {
                for (Integer taskId : running.keySet()) { showTask(running.get(taskId)); }
                for (Integer taskId : expired.keySet()) { showTask(expired.get(taskId)); }
            }
        }
        ImGui.end();
    }

    public static void showTask(DownloadTask downloadTask) {
        int taskId = downloadTask.getId();
        String taskName = downloadTask.getName().substring(0, Math.min(downloadTask.getName().length(), MAXIMUM_TASK_NAME_LENGTH));
        ImGui.pushID(taskId);
        float spacing = ImGui.getStyle().getItemInnerSpacingX();
//        ImGui.alignTextToFramePadding();
        ImGui.bulletText(String.format("%s (%s)", taskName, downloadTask.getDownloadTaskStatus().getName()));
        ImGui.sameLine(0.0f, spacing);
        String errorMessage = downloadTask.getErrorMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            ImGui.text(String.format("%s/%s", humanizeFileSize(downloadTask.getDownloadSize()), humanizeFileSize(downloadTask.getContentLength())));
        } else {
            ImGui.textColored(ImColor.rgb(255, 0, 0), errorMessage);
        }
        ImGui.newLine();
        ImGui.popID();
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
