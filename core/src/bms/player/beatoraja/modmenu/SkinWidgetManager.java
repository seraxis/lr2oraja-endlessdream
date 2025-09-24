package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SkinWidgetManager {
    private static final double eps = 1e-5;
    private static final Object LOCK = new Object();
    private static List<SkinWidget> widgets = new ArrayList<>();
    private static Map<String, List<Event>> events = new HashMap<>();

    private static ImFloat editingWidgetX = new ImFloat(0);
    private static ImFloat editingWidgetY = new ImFloat(0);
    private static ImFloat editingWidgetW = new ImFloat(0);
    private static ImFloat editingWidgetH = new ImFloat(0);

    public static void changeSkin(Skin skin) {
        synchronized (LOCK) {
            widgets.clear();
            events.clear();
            if (skin == null) {
                return ;
            }
            SkinObject[] allSkinObjects = skin.getAllSkinObjects();
            for (SkinObject skinObject : allSkinObjects) {
                SkinObject.SkinObjectDestination[] dsts = skinObject.getAllDestination();
                for (int i = 0; i < dsts.length; i++) {
                    String combinedName = dsts.length == 1 ? dsts[i].name : String.format("%s(%d)", dsts[i].name, i);
                    widgets.add(new SkinWidget(combinedName, dsts[i]));
                }
            }
        }
    }

    public static void show(ImBoolean showSkinWidgetManagerMenu) {
        synchronized (LOCK) {
            if (ImGui.begin("Skin Widgets", showSkinWidgetManagerMenu, ImGuiWindowFlags.AlwaysAutoResize)) {
                if (widgets.isEmpty()) {
                    ImGui.text("No skin is loaded");
                } else {
                    if (ImGui.beginTabBar("SkinWidgetsTabBar")) {
                        if (ImGui.beginTabItem("SkinWidgets")) {
                            renderSkinWidgetsTable();
                            ImGui.endTabItem();
                        }
                        if (ImGui.beginTabItem("History")) {
                            renderHistoryTable();
                            ImGui.endTabItem();
                        }
                        ImGui.endTabBar();
                    }
                }
            }
            ImGui.end();
        }
    }

    /**
     * Render skin widgets as a table
     */
    private static void renderSkinWidgetsTable() {
        if (ImGui.beginTable("Skin Widgets", 5, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("ID");
            ImGui.tableSetupColumn("x");
            ImGui.tableSetupColumn("y");
            ImGui.tableSetupColumn("w");
            ImGui.tableSetupColumn("h");
            ImGui.tableHeadersRow();
            ImGuiListClipper.forEach(widgets.size(), new ImListClipperCallback() {
                @Override
                public void accept(int row) {
                    SkinWidget widget = widgets.get(row);
                    ImGui.tableNextRow();
                    ImGui.pushID(row);

                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(widget.name);
                    ImGui.sameLine();
                    // We can wrap this in SkinWidget class if it's more complicated in the future
                    if (ImGui.button("Edit")) {
                        editingWidgetX.set(widget.getDstX());
                        editingWidgetY.set(widget.getDstY());
                        editingWidgetW.set(widget.getDstW());
                        editingWidgetH.set(widget.getDstH());
                        ImGui.openPopup("Edit Skin Widget");
                    }
                    if (ImGui.beginPopup("Edit Skin Widget", ImGuiWindowFlags.AlwaysAutoResize)) {
                        ImGui.inputFloat("x", editingWidgetX);
                        ImGui.inputFloat("y", editingWidgetY);
                        ImGui.inputFloat("w", editingWidgetW);
                        ImGui.inputFloat("h", editingWidgetH);
                        if (ImGui.button("Submit")) {
                            widget.setDstX(editingWidgetX.get());
                            widget.setDstY(editingWidgetY.get());
                            widget.setDstW(editingWidgetW.get());
                            widget.setDstH(editingWidgetH.get());
                            ImGui.closeCurrentPopup();
                        }
                        ImGui.endPopup();
                    }

                    // NOTE for further dev:
                    // If you want to implement a dynamic system, you can combine the event type & getter
                    // in a pair type: Pair<EventType, Function<SkinWidget, Float>
                    // The remaining things are trivial
                    drawFloatValueColumn(1, widget.hasEvent(Event.EventType.CHANGE_X), widget.getDstX());
                    drawFloatValueColumn(2, widget.hasEvent(Event.EventType.CHANGE_Y), widget.getDstY());
                    drawFloatValueColumn(3, widget.hasEvent(Event.EventType.CHANGE_W), widget.getDstW());
                    drawFloatValueColumn(4, widget.hasEvent(Event.EventType.CHANGE_H), widget.getDstH());

                    ImGui.popID();
                }
            });

            ImGui.endTable();
        }
    }

    /**
     * Render modification history
     */
    private static void renderHistoryTable() {
        if (ImGui.beginTable("History", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("ID");
            ImGui.tableSetupColumn("Desc");
            ImGui.tableHeadersRow();
            List<Event> flatEvents = events.values().stream().flatMap(Collection::stream).toList();
            ImGuiListClipper.forEach(flatEvents.size(), new ImListClipperCallback() {
                @Override
                public void accept(int row) {
                    Event event = flatEvents.get(row);
                    ImGui.tableNextRow();

                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(event.target);
                    ImGui.tableSetColumnIndex(1);
                    ImGui.text(event.getDescription());
                }
            });
            ImGui.endTable();
        }
    }

    /**
     * This is a small helper function to draw columns in table, draw red text if the cell value has been modified
     *
     * @param index column index
     * @param modified whether current cell's value has been modified
     * @param value cell value
     */
    private static void drawFloatValueColumn(int index, boolean modified, float value) {
        ImGui.tableSetColumnIndex(index);
        if (modified) {
            ImGui.textColored(ImColor.rgb(255, 0, 0), String.format("%.4f", value));
        } else {
            ImGui.text(String.format("%.4f", value));
        }
    }

    /**
     * Push one "change single field" event into list
     *
     * @param type event type
     * @param target widget's name
     * @param previous previous value
     * @param current current value
     */
    private static void pushChangeSingleFieldEvent(Event.EventType type, String target, float previous, float current) {
        events.putIfAbsent(target, new ArrayList<>());
        events.get(target).add(new ChangeSingleFieldEvent(type, target, previous, current));
    }

    // A simple wrapper class for tweaking destinations' name
    private static class SkinWidget {
        private String name;
        // DON'T ACCESS THIS FIELD DIRECTLY, USE GETTER/SETTER INSTEAD
        private SkinObject.SkinObjectDestination destination;

        public SkinWidget(String name, SkinObject.SkinObjectDestination destination) {
            this.name = name;
            this.destination = destination;
        }

        public float getDstX() {
            return destination.region.x;
        }

        public float getDstY() {
            return destination.region.y;
        }

        public float getDstW() {
            return destination.region.width;
        }

        public float getDstH() {
            return destination.region.height;
        }

        public void setDstX(float x) {
            float previous = this.getDstX();
            if (Math.abs(x - previous) > eps) {
                pushChangeSingleFieldEvent(Event.EventType.CHANGE_X, this.name, previous, x);
            }
            destination.region.x = x;
        }

        public void setDstY(float y) {
            float previous = this.getDstY();
            if (Math.abs(y - previous) > eps) {
                pushChangeSingleFieldEvent(Event.EventType.CHANGE_Y, this.name, previous, y);
            }
            destination.region.y = y;
        }

        public void setDstW(float w) {
            float previous = this.getDstW();
            if (Math.abs(w - previous) > eps) {
                pushChangeSingleFieldEvent(Event.EventType.CHANGE_W, this.name, previous, w);
            }
            destination.region.width = w;
        }

        public void setDstH(float h) {
            float previous = this.getDstH();
            if (Math.abs(h - previous) > eps) {
                pushChangeSingleFieldEvent(Event.EventType.CHANGE_H, this.name, previous, h);
            }
            destination.region.height = h;
        }

        public boolean hasEvent(Event.EventType type) {
            if (!events.containsKey(this.name)) {
                return false;
            }
            List<Event> events = SkinWidgetManager.events.get(this.name);
            return events.stream().anyMatch(event -> event.type == type);
        }
    }

    private abstract static class Event {
        protected EventType type;
        protected String target; // destination name

        enum EventType {
            CHANGE_X,
            CHANGE_Y,
            CHANGE_W,
            CHANGE_H
        }

        public Event(EventType type, String target) {
            this.type = type;
            this.target = target;
        }

        public abstract void undo(SkinWidget skinWidget);

        public abstract String getDescription();
    }

    private static class ChangeSingleFieldEvent extends Event {
        private final float previous;
        private final float current;

        public ChangeSingleFieldEvent(EventType type, String target, float previous, float current) {
            super(type, target);
            this.previous = previous;
            this.current = current;
        }

        @Override
        public void undo(SkinWidget skinWidget) {
            switch (type) {
                case CHANGE_X -> skinWidget.setDstX(previous);
                case CHANGE_Y -> skinWidget.setDstY(previous);
                case CHANGE_W -> skinWidget.setDstW(previous);
                case CHANGE_H -> skinWidget.setDstH(previous);
                default -> { /* Intentionally do nothing */ }
            }
        }

        @Override
        public String getDescription() {
            String fieldName = switch (type) {
                case CHANGE_X -> "x";
                case CHANGE_Y -> "y";
                case CHANGE_W -> "width";
                case CHANGE_H -> "height";
            };
            return String.format("Changed %s's %s from %.4f to %.4f", target, fieldName, previous, current);
        }
    }
}
