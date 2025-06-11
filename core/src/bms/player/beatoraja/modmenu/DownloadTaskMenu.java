package bms.player.beatoraja.modmenu;

import bms.tool.mdprocessor.DownloadTask;
import bms.tool.mdprocessor.HttpDownloadProcessor;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class DownloadTaskMenu {
    public static final int MAXIMUM_TASK_NAME_LENGTH = 10;
    private static final AtomicReference<List<DownloadTask>> downloadTaskSnapshot = new AtomicReference<>(new ArrayList<>());
    // A reference to HttpDownloadProcessor, initialized by initialize() function
    private static HttpDownloadProcessor httpDownloadProcessor;

    /**
     * @implNote Static in java is infectious, I cannot think of a better idea
     */
    public static void initialize(HttpDownloadProcessor httpDownloadProcessor) {
        DownloadTaskMenu.httpDownloadProcessor = httpDownloadProcessor;
        // The reason use a thread for updating snapshot of download tasks is ImGui's render
        // function (show, in this class) is based on frame. I think we have to separate the
        // state management and render part (Or maybe not? Still a question)
        (new Thread(() -> {
            while (true) {
                try {
                    List<DownloadTask> allTaskSnapshot = httpDownloadProcessor.getAllTaskSnapshot();
                    DownloadTaskMenu.downloadTaskSnapshot.set(allTaskSnapshot);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        })).start();
    }

    public static void show(ImBoolean showDownloadTasksWindow) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Download Tasks", showDownloadTasksWindow, ImGuiWindowFlags.AlwaysAutoResize)) {
            List<DownloadTask> tasks = DownloadTaskMenu.downloadTaskSnapshot.get();
            if (tasks == null || tasks.isEmpty()) {
                ImGui.text("No Data");
            } else {
                for (int i = 0; i < tasks.size(); ++i) {
                    DownloadTask downloadTask = tasks.get(i);
                    String taskName = downloadTask.getName().substring(0, Math.min(downloadTask.getName().length(), MAXIMUM_TASK_NAME_LENGTH));
                    ImGui.pushID(i);
                    float spacing = ImGui.getStyle().getItemInnerSpacingX();
//                    ImGui.alignTextToFramePadding();
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
            }

        }
        ImGui.end();
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
