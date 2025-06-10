package bms.player.beatoraja.modmenu;

import bms.tool.mdprocessor.DownloadTask;
import bms.tool.mdprocessor.HttpDownloadProcessor;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

public class DownloadTaskMenu {
    // A reference to HttpDownloadProcessor, initialized by initialize() function
    private static HttpDownloadProcessor httpDownloadProcessor;
    private static final AtomicReference<List<DownloadTask>> downloadTaskSnapshot = new AtomicReference<>(new ArrayList<>());

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
            for (int i = 0;i < tasks.size();++i) {
                DownloadTask downloadTask = tasks.get(i);
                ImGui.pushID(i);
                float spacing = ImGui.getStyle().getItemInnerSpacingX();
                ImGui.alignTextToFramePadding();
                ImGui.bulletText(downloadTask.getName());
                ImGui.newLine();
                ImGui.popID();
            }
        }
        ImGui.end();
    }
}
