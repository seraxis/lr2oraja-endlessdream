package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.PerformanceMetrics;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.util.*;

public class PerformanceMonitor {

    static HashMap<Integer, Vector<PerformanceMetrics.EventResult>> eventTree;
    static long lastEventUpdate = 0;

    private record WatchStats(Float avg, Float std) {}
    private static IdentityHashMap<String, WatchStats> watchData =
        new IdentityHashMap<String, WatchStats>();
    static long lastStatUpdate = 0;


    public static void show(ImBoolean showPerformanceMonitor) {
        long now = System.nanoTime();
        if (eventTree == null || lastEventUpdate < (now - 500000000L)) {
            lastEventUpdate = now;
            reloadEventTree();
        }

        // TODO: render 'watch' times in the same table
        if (ImGui.begin("Performance Monitor", showPerformanceMonitor)) {
            if (ImGui.collapsingHeader("Watch")) {
                updateWatchData();
                renderWatchData();
            }

            if (ImGui.collapsingHeader("Events", ImGuiTreeNodeFlags.DefaultOpen)) {
                renderEventTable();
            }
        }
        ImGui.end();
    }

    private static void updateWatchData() {
        boolean update_stats = lastStatUpdate < (System.nanoTime() - 100000000L);
        if (!update_stats)
            return;
        lastStatUpdate = System.nanoTime();

        Set<String> names = PerformanceMetrics.get().getWatchNames();
        for (String name : names) {
            var record = PerformanceMetrics.get().getWatchRecords(name);
            if (record.isEmpty()) {
                watchData.put(name, new WatchStats(0.f, 0.f));
                continue;
            }

            long sum = 0;
            for (var sample : record) {
                sum += sample.getValue();
            }
            float avg_ms = (sum / record.size()) / 1000.f;
            float variance = 0;
            for (var sample : record) {
                float ms = sample.getValue() / 1000.f;
                variance += (avg_ms - ms) * (avg_ms - ms);
            }
            variance /= record.size();
            float std = (float)Math.sqrt(variance);
            watchData.put(name, new WatchStats(avg_ms, std));
        }
    }

    private static void renderWatchData() {
        watchData.forEach((name, data) -> {
            ImGui.text(name);
            ImGui.text(String.format("avg = %.1fus, std = %.1fus", data.avg(), data.std()));
        });
    }

    public static void reloadEventTree() {
        // copy the vector to avoid constantly reading the events  while other threads might be writing
        eventTree = new HashMap<Integer, Vector<PerformanceMetrics.EventResult>>();
        Vector<PerformanceMetrics.EventResult> events;
        synchronized (PerformanceMetrics.get().eventResults) {
            events = new Vector<PerformanceMetrics.EventResult>(PerformanceMetrics.get().eventResults);
        }
        for (var event : events) {
            if (!eventTree.containsKey(event.parent())) {
                eventTree.put(event.parent(), new Vector<PerformanceMetrics.EventResult>());
            }
            // I'm not sure if this works out to always be sorted chronologically,
            // should make sure that it is
            eventTree.get(event.parent()).add(event);
        }
    }

    public static float[] filterShortThreshold = {1.0f};

    private static void renderEventTable() {
        ImGui.setNextItemWidth(ImGui.getContentRegionAvail().x / 5.f);
        ImGui.sliderFloat("Filter short events", filterShortThreshold, 0.0f, 4.0f);

        if (ImGui.beginTable("event-table", 3, ImGuiTableFlags.ScrollY)) {
            ImGui.tableSetupColumn("Event", ImGuiTableColumnFlags.WidthStretch, 3.0f);
            ImGui.tableSetupColumn("Time", ImGuiTableColumnFlags.WidthStretch, 1.5f);
            ImGui.tableSetupColumn("Thread", ImGuiTableColumnFlags.WidthStretch, 1.0f);
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

        // TODO: toggle for sorting results by duration instead of chronologically
        Vector<PerformanceMetrics.EventResult> group = eventTree.get(groupId);

        for (var event : group) {
            float duration_ms = (float)(event.duration() / 1000000.0);
            if (duration_ms < filterShortThreshold[0])
                continue;

            boolean leaf = !eventTree.containsKey(event.id());
            var flags = leaf ? ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen |
                                   ImGuiTreeNodeFlags.Bullet
                             : 0;
            boolean open = ImGui.treeNodeEx(event.id(), flags, event.name());
            ImGui.tableNextColumn();
            ImGui.text(String.format("%9.2fms", duration_ms));
            ImGui.tableNextColumn();
            ImGui.text(String.format("%s", event.thread()));
            ImGui.tableNextColumn();
            if (!leaf && open) {
                renderEventTree(event.id());
                ImGui.treePop();
            }
        }
    }
}
