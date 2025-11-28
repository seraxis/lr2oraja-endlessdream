package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;
import com.badlogic.gdx.math.Rectangle;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.ImVec2;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;

public class SkinWidgetManager {
    private static final double eps = 1e-5;
    private static final Object LOCK = new Object();
    private static final EventHistory eventHistory = new EventHistory();
    private static final List<SkinWidget> widgets = new ArrayList<>();

    private static final List<WidgetTableColumn> WIDGET_TABLE_COLUMNS = new ArrayList<>();

    static {
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("ID", new ImBoolean(true), true, null, null));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("x", new ImBoolean(true), false, SkinWidgetDestination::getDstX, Event.EventType.CHANGE_X));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("y", new ImBoolean(true), false, SkinWidgetDestination::getDstY, Event.EventType.CHANGE_Y));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("w", new ImBoolean(true), false, SkinWidgetDestination::getDstW, Event.EventType.CHANGE_W));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("h", new ImBoolean(true), false, SkinWidgetDestination::getDstH, Event.EventType.CHANGE_H));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("Operation", new ImBoolean(true), true, null, null));
    }

    private static final ImFloat editingWidgetX = new ImFloat(0);
    private static final ImFloat editingWidgetY = new ImFloat(0);
    private static final ImFloat editingWidgetW = new ImFloat(0);
    private static final ImFloat editingWidgetH = new ImFloat(0);
    private static final ImBoolean SHOW_CURSOR_POSITION = new ImBoolean(true);

    private static final ImBoolean move_overlay_enabled = new ImBoolean(false);
    private static boolean reset_move_overlay = false;

    public static boolean focus = false;

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
                String skinObjectName = skinObject.getName();
                SkinObject.SkinObjectDestination[] dsts = skinObject.getAllDestination();
                List<SkinWidgetDestination> destinations = new ArrayList<>();
                for (int i = 0; i < dsts.length; ++i) {
                    String dstBaseName = skinObjectName == null ? "Unnamed Destination" : skinObjectName;
                    String combinedName = dsts.length == 1 ? dstBaseName : String.format("%s(%d)", dstBaseName, i);
                    destinations.add(new SkinWidgetDestination(combinedName, dsts[i]));
                }
                String widgetBaseName = skinObjectName == null ? "Unnamed Widget" : skinObjectName;
                Integer count = duplicatedSkinObjectNameCount.getOrDefault(widgetBaseName, 0);
                duplicatedSkinObjectNameCount.compute(widgetBaseName, (pk, pv) -> pv == null ? 1 : pv + 1);
                String widgetName = count == 0 ? widgetBaseName : String.format("%s(%d)", widgetBaseName, count);
                widgets.add(new SkinWidget(widgetName, skinObject, destinations));
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
                            if (ImGui.button("undo")) {
                                eventHistory.undo();
                            }
                            ImGui.sameLine();
                            renderPreferColumnSetting();
                            ImGui.sameLine();
                            ImGui.checkbox("Show Position", SHOW_CURSOR_POSITION);
                            ImGui.sameLine();
                            if (ImGui.button("export")) {
                                exportChanges();
                            }


                            renderSkinWidgetsTable();
                            ImGui.endTabItem();
                        }
                        if (ImGui.beginTabItem("History")) {
                            renderHistoryTable();
                            ImGui.endTabItem();
                        }
                        ImGui.endTabBar();
                    }
                    // Overlay cursor position
                    if (SHOW_CURSOR_POSITION.get()) {
                        ImGui.beginTooltip();
                        ImGui.text(
                                String.format("(%s, %s)",
                                normalizeFloat(Gdx.input.getX()),
                                normalizeFloat(windowHeight - Gdx.input.getY()))
                        );
                        ImGui.endTooltip();
                    }
                }
            }
            ImGui.end();
        }
    }

    private static void renderPreferColumnSetting() {
        if (ImGui.button("Columns")) {
            ImGui.openPopup("PreferColumnSetting");
        }

        if (ImGui.beginPopup("PreferColumnSetting")) {
            for (WidgetTableColumn column : WIDGET_TABLE_COLUMNS) {
                if (column.persistent) {
                    continue;
                }
                ImGui.checkbox(column.name, column.show);
            }
            ImGui.endPopup();
        }
    }

    /**
     * Render skin widgets as a table
     */
    private static void renderSkinWidgetsTable() {
        // NOTE: This will create a snapshot for us, which can kinda prevent us step into race condition
        List<WidgetTableColumn> showingColumns = WIDGET_TABLE_COLUMNS.stream().filter(column -> column.show.get()).toList();
        int colSize = showingColumns.size();
        if (ImGui.beginTable("Skin Widgets", colSize, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            showingColumns.forEach(column -> ImGui.tableSetupColumn(column.name));
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

                for (int i = 1; i <= colSize - 2; ++i) {
                    ImGui.tableSetColumnIndex(i);
                    ImGui.textDisabled("--");
                }

                ImGui.tableSetColumnIndex(colSize - 1);
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
                        for (int i = 1; i <= colSize - 2;++i) {
                            WidgetTableColumn column = showingColumns.get(i);
                            drawFloatValueColumn(i, eventHistory.hasEvent(dst.name, column.changeEventType), column.getter.apply(dst));
                        }

                        ImGui.tableSetColumnIndex(colSize - 1);
                        if (ImGui.button("Edit")) {
                            editingWidgetX.set(dst.getDstX());
                            editingWidgetY.set(dst.getDstY());
                            editingWidgetW.set(dst.getDstW());
                            editingWidgetH.set(dst.getDstH());
                            reset_move_overlay = true;
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

                            if ((ImGui.checkbox("Move", move_overlay_enabled)
                                 && move_overlay_enabled.get())
                                || reset_move_overlay) {
                                float w = dst.getDstW();
                                float h = dst.getDstH();
                                float x = dst.getDstX();
                                float y = windowHeight - dst.getDstY() - h;
                                ImGui.setNextWindowPos(x, y);
                                ImGui.setNextWindowSize(w, h);
                                reset_move_overlay = false;
                            }

                            if (move_overlay_enabled.get()) {
                                if (dst.movingState == 0) {
                                    Rectangle clonedRegion = new Rectangle(dst.getDstX(), dst.getDstY(), dst.getDstW(), dst.getDstH());
                                    dst.beforeMove = new SkinObject.SkinObjectDestination(0, clonedRegion, null, 0, 0);
                                    dst.movingState = 1;
                                }
                                ImGui.pushStyleColor(ImGuiCol.WindowBg, 0, 0, 0, 0.4f);
                                ImGui.pushStyleColor(ImGuiCol.Border, 0.2f, 0.4f, 1.f, 1.f);
                                ImGui.pushStyleColor(ImGuiCol.ResizeGrip, 1.f, .3f, .3f, 1.f);
                                ImGui.pushStyleColor(ImGuiCol.ResizeGripHovered, 1.f, 0.7f, .7f, 1.f);
                                if (ImGui.begin("widget-overlay-popup",
                                                move_overlay_enabled,
                                                ImGuiWindowFlags.NoNav |
                                                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoFocusOnAppearing |
                                                ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.NoCollapse |
                                                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoTitleBar)) {
                                    ImVec2 pos = ImGui.getWindowPos();
                                    ImVec2 size = ImGui.getWindowSize();
                                    float w = size.x;
                                    float h = size.y;
                                    float x = pos.x;
                                    float y = windowHeight - pos.y - h;
                                    ImGui.text(String.format("x = %.1f y = %.1f", x, y));
                                    ImGui.text(String.format("w = %.1f h = %.1f", w, h));
                                    // NOTE: This approach is actually moving the "REAL" widget in-time, so we have to:
                                    // Don't produce any change field events
                                    // Make a true set when move is "submitted"
                                    dst.setDstX(x, false);
                                    dst.setDstY(y, false);
                                    dst.setDstW(w, false);
                                    dst.setDstH(h, false);
                                }
                                if (ImGui.isWindowFocused()) {
                                    if (dst.movingState == 1) {
                                        dst.movingState = 2;
                                    }
                                } else {
                                    if (dst.movingState == 2) {
                                        dst.movingState = 0;
                                        dst.submitMovement();
                                    }
                                }
                                ImGui.end();
                                ImGui.popStyleColor();
                                ImGui.popStyleColor();
                                ImGui.popStyleColor();
                                ImGui.popStyleColor();
                            } else {
                                dst.movingState = 0;
                            }
                            ImGui.endPopup();
                        } else {
                            // If user clicked the empty space while moving widgets, the whole popup would be closed too
                            // So we have to catch the "escaping" widget here
                            if (dst.movingState == 2) {
                                dst.movingState = 0;
                                dst.submitMovement();
                            }
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
        if (ImGui.beginTable("History", 1, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
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
            ImGui.textColored(ImColor.rgb(255, 0, 0), normalizeFloat(value));
        } else {
            ImGui.text(normalizeFloat(value));
        }
    }

    /**
     * Export all changes, currently it simply copies the change logs to clipboard
     */
    private static void exportChanges() {
        List<String> changes = new ArrayList<>();
        widgets.forEach(widget -> {
            widget.destinations.forEach(dst -> {
                boolean hasChangedX = false, hasChangedY = false, hasChangedW = false, hasChangedH = false;
                for (Event<?> event : eventHistory.getEvents(dst.name)) {
                    switch (event.type) {
                        case CHANGE_X -> hasChangedX = true;
                        case CHANGE_Y -> hasChangedY = true;
                        case CHANGE_W -> hasChangedW = true;
                        case CHANGE_H -> hasChangedH = true;
                    }
                }
                if (!(hasChangedX || hasChangedY || hasChangedW || hasChangedH)) {
                    return ;
                }
                StringBuilder sb = new StringBuilder("{dst=").append(dst.name);
                if (hasChangedX) {
                    sb.append(", x=").append(dst.getDstX());
                }
                if (hasChangedY) {
                    sb.append(", y=").append(dst.getDstY());
                }
                if (hasChangedX) {
                    sb.append(", w=").append(dst.getDstW());
                }
                if (hasChangedY) {
                    sb.append(", h=").append(dst.getDstH());
                }
                sb.append("}");
                changes.add(sb.toString());
            });
        });
        String changeLogs = String.join("\n", changes);
        Lwjgl3Clipboard clipboard = new Lwjgl3Clipboard();
        clipboard.setContents(changeLogs);
        ImGuiNotify.info("Copied changes to clipboard");
    }

    /**
     * All float value in this widget shares a 4-width limitation
     */
    private static String normalizeFloat(float value) {
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(value);
    }

    /**
     * Represents one widget table's column
     */
    private record WidgetTableColumn(String name, ImBoolean show, boolean persistent, Function<SkinWidgetDestination, Float> getter, Event.EventType changeEventType) {
    }

    /**
     * A simple wrapper class of SkinObject
     *
     * @implNote setter functions must provide an extra argument to not trigger event system
     */
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
            toggleVisible(true);
        }

        public void toggleVisible(boolean createEvent) {
            boolean isVisibleBefore = skinObject.visible;
            if (createEvent) {
                eventHistory.pushEvent(new ToggleVisibleEvent(this, isVisibleBefore));
            }
            skinObject.visible = !isVisibleBefore;
        }
    }

    /**
     * A simple wrapper class of SkinObject.SkinObjectDestination
     *
     * @implNote setter functions must provide an extra argument to not trigger event system
     */
    private static class SkinWidgetDestination {
        private final String name;
        private final SkinObject.SkinObjectDestination destination;
        private SkinObject.SkinObjectDestination beforeMove = null;
        /**
         * <ul>
         *     <li>0: haven't started moving</li>
         *     <li>1: user enabled the move feature, but hasn't move around</li>
         *     <li>2: user has moved the widget</li>
         * </ul>
         */
        private int movingState;

        public SkinWidgetDestination(String name, SkinObject.SkinObjectDestination destination) {
            this.name = name;
            this.destination = destination;
            this.movingState = 0;
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
            setDstX(x, true);
        }

        public void setDstX(float x, boolean createEvent) {
            float previous = this.getDstX();
            if (createEvent && Math.abs(x - previous) > eps) {
                eventHistory.pushEvent(new ChangeSingleFieldEvent(Event.EventType.CHANGE_X, this, previous, x));
            }
            destination.region.x = x;
        }

        public void setDstY(float y) {
            setDstY(y, true);
        }

        public void setDstY(float y, boolean createEvent) {
            float previous = this.getDstY();
            if (createEvent && Math.abs(y - previous) > eps) {
                eventHistory.pushEvent(new ChangeSingleFieldEvent(Event.EventType.CHANGE_Y, this, previous, y));
            }
            destination.region.y = y;
        }

        public void setDstW(float w) {
            setDstW(w, true);
        }

        public void setDstW(float w, boolean createEvent) {
            float previous = this.getDstW();
            if (createEvent && Math.abs(w - previous) > eps) {
                eventHistory.pushEvent(new ChangeSingleFieldEvent(Event.EventType.CHANGE_W, this, previous, w));
            }
            destination.region.width = w;
        }

        public void setDstH(float h) {
            setDstH(h, true);
        }

        public void setDstH(float h, boolean createEvent) {
            float previous = this.getDstH();
            if (createEvent && Math.abs(h - previous) > eps) {
                eventHistory.pushEvent(new ChangeSingleFieldEvent(Event.EventType.CHANGE_H, this, previous, h));
            }
            destination.region.height = h;
        }

        /**
         * Submit the move result, producing the event
         */
        public void submitMovement() {
            if (beforeMove == null) {
                ImGuiNotify.error("Cannot submit the move result because there's no original position");
                return ;
            }
            float nextX = getDstX();
            float nextY = getDstY();
            float nextW = getDstW();
            float nextH = getDstH();
            // Reset the position, to mimic that we are never left the original position
            setDstX(beforeMove.region.x, false);
            setDstY(beforeMove.region.y, false);
            setDstW(beforeMove.region.width, false);
            setDstH(beforeMove.region.height, false);
            // Truly move to the target position
            setDstX(nextX);
            setDstY(nextY);
            setDstW(nextW);
            setDstH(nextH);
            beforeMove = null;
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
                case CHANGE_X -> handle.setDstX(previous, false);
                case CHANGE_Y -> handle.setDstY(previous, false);
                case CHANGE_W -> handle.setDstW(previous, false);
                case CHANGE_H -> handle.setDstH(previous, false);
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
            handle.toggleVisible(false);
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
        // targetNameToEvents is only a read-only copy of eventStack, to make the query function easier to write
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

        public List<Event<?>> getEvents(String name) {
            return targetNameToEvents.getOrDefault(name, new ArrayList<>());
        }

        private void pushEvent(Event<?> event) {
            targetNameToEvents.putIfAbsent(event.getName(), new ArrayList<>());
            targetNameToEvents.get(event.getName()).add(event);
            eventStack.add(event);
        }

        /**
         * Undo the most recent event
         */
        private void undo() {
            undo(1);
        }

        /**
         * Undo the most recent event multiple times
         *
         * @param times how many events to undo, do nothing if no event to undo
         */
        private void undo(int times) {
            times = Math.abs(times);
            if (times == 0) {
                return;
            }

            for (int i = 0; i < times; ++i) {
                if (eventStack.isEmpty()) {
                    break;
                }
                int last = eventStack.size() - 1;
                Event<?> lastEvent = eventStack.get(last);
                eventStack.remove(last);
                lastEvent.undo();
            }

            targetNameToEvents.clear();
            for (Event<?> event : eventStack) {
                targetNameToEvents.putIfAbsent(event.getName(), new ArrayList<>());
                targetNameToEvents.get(event.getName()).add(event);
            }
        }
    }
}
