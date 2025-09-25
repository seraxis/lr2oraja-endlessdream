package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkinWidgetManager {
    private static final double eps = 1e-5;
    private static final Object LOCK = new Object();
    private static final EventHistory eventHistory = new EventHistory();
    private static final List<SkinWidget> widgets = new ArrayList<>();

    private static final ImFloat editingWidgetX = new ImFloat(0);
    private static final ImFloat editingWidgetY = new ImFloat(0);
    private static final ImFloat editingWidgetW = new ImFloat(0);
    private static final ImFloat editingWidgetH = new ImFloat(0);

    public static void changeSkin(Skin skin) {
        synchronized (LOCK) {
            widgets.clear();
            eventHistory.clear();
            if (skin == null) {
                return ;
            }
            SkinObject[] allSkinObjects = skin.getAllSkinObjects();
            // NOTE: We're using skin object's name as id, we need to keep name is unique
            Map<String, Integer> duplicatedSkinObjectNameCount = new HashMap<>();
            for (SkinObject skinObject : allSkinObjects) {
                SkinObject.SkinObjectDestination[] dsts = skinObject.getAllDestination();
                List<SkinWidgetDestination> destinations = new ArrayList<>();
                for (int i = 0; i < dsts.length; ++i) {
                    String combinedName = dsts.length == 1 ? skinObject.getName() : String.format("%s(%d)", skinObject.getName(), i);
                    destinations.add(new SkinWidgetDestination(combinedName, dsts[i]));
                }
                String skinObjectName = skinObject.getName();
                Integer count = duplicatedSkinObjectNameCount.getOrDefault(skinObjectName, 0);
                if (count > 0) {
                    skinObjectName += String.format("(%d)", count);
                }
                widgets.add(new SkinWidget(skinObjectName, skinObject, destinations));
                duplicatedSkinObjectNameCount.compute(skinObject.getName(), (pk, pv) -> pv == null ? 1 : pv + 1);
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
        if (ImGui.beginTable("Skin Widgets", 6, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("ID");
            ImGui.tableSetupColumn("x");
            ImGui.tableSetupColumn("y");
            ImGui.tableSetupColumn("w");
            ImGui.tableSetupColumn("h");
            ImGui.tableSetupColumn("Operation");
            ImGui.tableHeadersRow();
            for (SkinWidget widget : widgets) {
                ImGui.tableNextRow();
                ImGui.pushID(widget.name);

                ImGui.tableSetColumnIndex(0);
                boolean isWidgetDrawingOnScreen = widget.isDrawingOnScreen();
                if (!isWidgetDrawingOnScreen) {
                    ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(128, 128, 128));
                }
                boolean isOpen = ImGui.treeNodeEx(widget.name);
                if (!isWidgetDrawingOnScreen) {
                    ImGui.popStyleColor();
                }

                for (int i = 1; i <= 4; ++i) {
                    ImGui.tableSetColumnIndex(i);
                    ImGui.textDisabled("--");
                }

                ImGui.tableSetColumnIndex(5);
                if (ImGui.button("Toggle")) {
                    widget.toggleVisible();
                }

                if (isOpen) {
                    for (SkinWidgetDestination dst : widget.destinations) {
                        ImGui.pushID(dst.name);

                        ImGui.tableNextRow();
                        ImGui.tableSetColumnIndex(0);
                        if (!isWidgetDrawingOnScreen) {
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(128, 128, 128));
                        }
                        ImGui.text(dst.name);
                        if (!isWidgetDrawingOnScreen) {
                            ImGui.popStyleColor();
                        }

                        // NOTE for further dev:
                        // If you want to implement a dynamic system, you can combine the event type & getter
                        // in a pair type: Pair<EventType, Function<SkinWidget, Float>
                        // The remaining things are trivial
                        drawFloatValueColumn(1, eventHistory.hasEvent(dst.name, Event.EventType.CHANGE_X), dst.getDstX());
                        drawFloatValueColumn(2, eventHistory.hasEvent(dst.name, Event.EventType.CHANGE_Y), dst.getDstY());
                        drawFloatValueColumn(3, eventHistory.hasEvent(dst.name, Event.EventType.CHANGE_W), dst.getDstW());
                        drawFloatValueColumn(4, eventHistory.hasEvent(dst.name, Event.EventType.CHANGE_H), dst.getDstH());

                        ImGui.tableSetColumnIndex(5);
                        if (ImGui.button("Edit")) {
                            editingWidgetX.set(dst.getDstX());
                            editingWidgetY.set(dst.getDstY());
                            editingWidgetW.set(dst.getDstW());
                            editingWidgetH.set(dst.getDstH());
                            ImGui.openPopup("Edit Skin Widget");
                        }
                        if (ImGui.beginPopup("Edit Skin Widget", ImGuiWindowFlags.AlwaysAutoResize)) {
                            ImGui.inputFloat("x", editingWidgetX);
                            ImGui.inputFloat("y", editingWidgetY);
                            ImGui.inputFloat("w", editingWidgetW);
                            ImGui.inputFloat("h", editingWidgetH);
                            if (ImGui.button("Submit")) {
                                dst.setDstX(editingWidgetX.get());
                                dst.setDstY(editingWidgetY.get());
                                dst.setDstW(editingWidgetW.get());
                                dst.setDstH(editingWidgetH.get());
                                ImGui.closeCurrentPopup();
                            }
                            ImGui.endPopup();
                        }

                        ImGui.popID();
                    }
                    ImGui.treePop();
                }

                ImGui.popID();
            }
            ImGui.endTable();
        }
    }

    /**
     * Render modification history
     */
    private static void renderHistoryTable() {
        if (ImGui.beginTable("History", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("Description");
            ImGui.tableHeadersRow();
            List<Event<?>> events = eventHistory.getEvents();
            ImGuiListClipper.forEach(events.size(), new ImListClipperCallback() {
                @Override
                public void accept(int row) {
                    ImGui.pushID(row);

                    Event<?> event = events.get(row);
                    ImGui.tableNextRow();

                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(event.getDescription());
                    ImGui.tableSetColumnIndex(1);
                    if (ImGui.button("Undo to here")) {
                        ImGuiNotify.warning("TODO: MY DEAR PLEASE IMPLEMENT ME");
                    }

                    ImGui.popID();
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

    // A simple wrapper class of SkinObject
    private static class SkinWidget {
        private final String name;
        // DON'T ACCESS THESE FIELDS DIRECTLY, USE GETTER/SETTER INSTEAD
        private final SkinObject skinObject;
        private final List<SkinWidgetDestination> destinations;

        public SkinWidget(String name, SkinObject skinObject, List<SkinWidgetDestination> destinations) {
            this.name = name;
            this.skinObject = skinObject;
            this.destinations = destinations;
        }

        public boolean isDrawingOnScreen() {
            return skinObject.draw && skinObject.visible;
        }

        public void toggleVisible() {
            boolean isVisibleBefore = skinObject.visible;
            eventHistory.pushEvent(new ToggleVisibleEvent(this, isVisibleBefore));
            skinObject.visible = !isVisibleBefore;
        }
    }

    // A simple wrapper class of SkinObject.SkinObjectDestination
    private static class SkinWidgetDestination {
        private final String name;
        private final SkinObject.SkinObjectDestination destination;

        public SkinWidgetDestination(String name, SkinObject.SkinObjectDestination destination) {
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
                eventHistory.pushEvent(new ChangeSingleFieldEvent(Event.EventType.CHANGE_X, this, previous, x));
            }
            destination.region.x = x;
        }

        public void setDstY(float y) {
            float previous = this.getDstY();
            if (Math.abs(y - previous) > eps) {
                eventHistory.pushChangeSingleFieldEvent(Event.EventType.CHANGE_Y, this, previous, y);
            }
            destination.region.y = y;
        }

        public void setDstW(float w) {
            float previous = this.getDstW();
            if (Math.abs(w - previous) > eps) {
                eventHistory.pushChangeSingleFieldEvent(Event.EventType.CHANGE_W, this, previous, w);
            }
            destination.region.width = w;
        }

        public void setDstH(float h) {
            float previous = this.getDstH();
            if (Math.abs(h - previous) > eps) {
                eventHistory.pushChangeSingleFieldEvent(Event.EventType.CHANGE_H, this, previous, h);
            }
            destination.region.height = h;
        }
    }

    private abstract static class Event<T> {
        protected EventType type;
        protected T handle; // reference to the event object

        // NOTE: This is kinda naive, but it works...
        enum EventType {
            // ChangeSingleFieldEvent
            CHANGE_X,
            CHANGE_Y,
            CHANGE_W,
            CHANGE_H,
            // ToggleVisibleEvent
            TOGGLE_VISIBLE
        }

        public Event(EventType type, T handle) {
            this.type = type;
            this.handle = handle;
        }

        public abstract void undo();

        public abstract String getDescription();

        public abstract String getName();
    }

    /**
     * Records the event when changing a single field from a widget
     */
    private static class ChangeSingleFieldEvent extends Event<SkinWidgetDestination> {
        private final float previous;
        private final float current;

        public ChangeSingleFieldEvent(EventType type, SkinWidgetDestination dst, float previous, float current) {
            super(type, dst);
            this.previous = previous;
            this.current = current;
        }

        @Override
        public void undo() {
            switch (type) {
                case CHANGE_X -> handle.setDstX(previous);
                case CHANGE_Y -> handle.setDstY(previous);
                case CHANGE_W -> handle.setDstW(previous);
                case CHANGE_H -> handle.setDstH(previous);
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
                default -> "[ERROR] Not a ChangeSingleFieldEvent";
            };
            return String.format("Changed %s's %s from %.4f to %.4f", handle.name, fieldName, previous, current);
        }

        @Override
        public String getName() {
            return handle.name;
        }
    }

    /**
     * Records the event when toggling the visibility of a widget
     */
    private static class ToggleVisibleEvent extends Event<SkinWidget> {
        private final boolean isVisibleBefore;

        public ToggleVisibleEvent(SkinWidget handle, boolean isVisibleBefore) {
            super(EventType.TOGGLE_VISIBLE, handle);
            this.isVisibleBefore = isVisibleBefore;
        }

        @Override
        public void undo() {
            handle.skinObject.draw = isVisibleBefore;
        }

        @Override
        public String getDescription() {
            return isVisibleBefore
                    ? String.format("Make %s widget invisible", handle.name)
                    : String.format("Make %s widget visible", handle.name);
        }

        @Override
        public String getName() {
            return handle.name;
        }
    }

    /**
     * A simple collections that holds all events, supporting:
     * <ul>
     *     <li> Push one event </li>
     *     <li> Pop most recent events</li>
     *     <li> Query specified widget has specific event or not </li>
     * </ul>
     *
     * @apiNote Requires lock
     */
    private static class EventHistory {
        private static final Map<String, List<Event<?>>> targetNameToEvents = new HashMap<>();
        private static final List<Event<?>> eventStack = new ArrayList<>();

        public void clear() {
            targetNameToEvents.clear();
            eventStack.clear();
        }

        public boolean hasEvent(String widgetName, Event.EventType eventType) {
            if (!targetNameToEvents.containsKey(widgetName)) {
                return false;
            }
            List<Event<?>> events = targetNameToEvents.get(widgetName);
            return events.stream().anyMatch(event -> event.type == eventType);
        }

        public List<Event<?>> getEvents() {
            return eventStack;
        }

        /**
         * Push one "change single field" event into history
         *
         * @param type event type
         * @param previous previous value
         * @param current current value
         */
        private void pushChangeSingleFieldEvent(Event.EventType type, SkinWidgetDestination dst, float previous, float current) {
            ChangeSingleFieldEvent event = new ChangeSingleFieldEvent(type, dst, previous, current);
            targetNameToEvents.putIfAbsent(dst.name, new ArrayList<>());
            targetNameToEvents.get(dst.name).add(event);
            eventStack.add(event);
        }

        private void pushEvent(Event<?> event) {
            targetNameToEvents.putIfAbsent(event.getName(), new ArrayList<>());
            targetNameToEvents.get(event.getName()).add(event);
            eventStack.add(event);
        }
    }
}
