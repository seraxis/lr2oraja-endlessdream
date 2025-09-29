package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.PerformanceMetrics;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;

import java.util.*;

public class PerformanceMonitor {

    static HashMap<Integer, Vector<PerformanceMetrics.EventResult>> eventTree;

    public static void show(ImBoolean showPerformanceMonitor) {
        if (eventTree == null) {
            reloadEventTree();
        }
        if (ImGui.begin("Performance Monitor", showPerformanceMonitor)) {
            if (ImGui.collapsingHeader("Events")) {
                renderEventTable();
            }
        }
        ImGui.end();
    }




    public static void reloadEventTree() {
        // copy the vector to avoid constantly reading the events  while other threads might be writing
        eventTree = new HashMap<Integer, Vector<PerformanceMetrics.EventResult>>();
        Vector<PerformanceMetrics.EventResult> events =
            new Vector<PerformanceMetrics.EventResult>(PerformanceMetrics.get().eventResults);
        for (var event : events) {
            if (!eventTree.containsKey(event.parent())) {
                eventTree.put(event.parent(), new Vector<PerformanceMetrics.EventResult>());
            }
            // I'm not sure if this works out to always be sorted chronologically,
            // should make sure that it is
            eventTree.get(event.parent()).add(event);
        }
    }

    private static void renderEventTable() {
        if (ImGui.beginTable("event-table", 2, ImGuiTableFlags.ScrollY)) {
            ImGui.tableSetupColumn("Event", ImGuiTableColumnFlags.WidthStretch, 2.0f);
            ImGui.tableSetupColumn("Time", ImGuiTableColumnFlags.WidthStretch, 1.0f);
            ImGui.tableHeadersRow();

            ImGui.tableNextRow();
            ImGui.tableNextColumn();

            renderEventTree(0);

            ImGui.endTable();
        }
    }

    private static void renderEventTree(int groupId) {
        if (!eventTree.containsKey(groupId))
            return;

        Vector<PerformanceMetrics.EventResult> group = eventTree.get(groupId);

        for (var event : group) {
            boolean leaf = !eventTree.containsKey(event.id());
            var flags = leaf ? ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen |
                                   ImGuiTreeNodeFlags.Bullet
                             : 0;
            boolean open = ImGui.treeNodeEx(event.name(), flags);
            ImGui.tableNextColumn();
            ImGui.text(String.format("%12.2fms", event.duration() / 1000000.0));
            ImGui.tableNextColumn();
            if (!leaf && open) {
                renderEventTree(event.id());
                ImGui.treePop();
            }
        }
    }
}
