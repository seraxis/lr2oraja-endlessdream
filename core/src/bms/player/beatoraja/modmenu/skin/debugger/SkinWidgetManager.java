package bms.player.beatoraja.modmenu.skin.debugger;

import bms.player.beatoraja.modmenu.FontAwesomeIcons;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.modmenu.skin.debugger.events.ChangeSingleFieldEvent;
import bms.player.beatoraja.modmenu.skin.debugger.events.ToggleVisibleEvent;
import bms.player.beatoraja.play.SkinJudge;
import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import bms.tool.util.Pair;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;
import com.badlogic.gdx.math.Rectangle;
import com.fasterxml.jackson.databind.ObjectMapper;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.ImVec2;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.helpMarker;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;

/**
 * Debug menu for controlling the widgets in current skin
 */
public class SkinWidgetManager {
    private static final Logger logger = LoggerFactory.getLogger(SkinWidgetManager.class);
    private static final Map<String, EventHistory> eventHistories = new HashMap<>();
    private static final List<SkinWidget> widgets = new ArrayList<>();
    private static final List<WidgetTableColumn> WIDGET_TABLE_COLUMNS = new ArrayList<>();
    private static final List<Pair<String, String>> removedObjects = new ArrayList<>();

    private static EventHistory eventHistory = new EventHistory();
    private static String skinDirectory;

    static {
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("ID", new ImBoolean(true), true, null, null));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("x", new ImBoolean(true), false, SkinWidgetDestination::getDstX, Event.EventType.CHANGE_X));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("y", new ImBoolean(true), false, SkinWidgetDestination::getDstY, Event.EventType.CHANGE_Y));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("w", new ImBoolean(true), false, SkinWidgetDestination::getDstW, Event.EventType.CHANGE_W));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("h", new ImBoolean(true), false, SkinWidgetDestination::getDstH, Event.EventType.CHANGE_H));
        WIDGET_TABLE_COLUMNS.add(new WidgetTableColumn("Operation", new ImBoolean(true), true, null, null));
    }

    private static boolean enableFilteringWidgets = false;
    private static final ImString searchWidgetName = new ImString(128);

    private static final ImFloat editingWidgetX = new ImFloat(0);
    private static final ImFloat editingWidgetY = new ImFloat(0);
    private static final ImFloat editingWidgetW = new ImFloat(0);
    private static final ImFloat editingWidgetH = new ImFloat(0);
    private static final ImBoolean SHOW_CURSOR_POSITION = new ImBoolean(true);

    private static final ImBoolean move_overlay_enabled = new ImBoolean(false);
    private static boolean reset_move_overlay = false;

    public static boolean focus = false;

    static void changeSkin(Skin skin) {
        widgets.clear();
        String skinName = skin == null ? "NULL" : skin.header.getName();
        eventHistory = eventHistories.getOrDefault(skinName, new EventHistory());
        if (skin == null) {
            return;
        }
        skinDirectory = skin.header.getPath().getParent().toString();
        SkinObject[] allSkinObjects = skin.getAllSkinObjects();
        // NOTE: We're using skin object's name as id, we need to keep name is unique
        Map<String, Integer> duplicatedSkinObjectNameCount = new HashMap<>();
        for (SkinObject skinObject : allSkinObjects) {
            if (skinObject instanceof SkinJudge skinJudge) {
                // Support 2P? Currently don't have to
                registerSkinObject(skinJudge.getJudge(0), duplicatedSkinObjectNameCount);
                registerSkinObject(skinJudge.getJudgeCount(0), duplicatedSkinObjectNameCount);
            } else {
                registerSkinObject(skinObject, duplicatedSkinObjectNameCount);
            }
        }
        // Apply the custom changes
        if (skin.getCustomChanges() != null) {
            List<Event<?>> customEvents = skin.getCustomChanges().getEvents().stream().map(PersistedEvent::load).collect(Collectors.toList());
            // Before we "recover" the changes, we should filter out the events that no handle can be found
            Set<String> names = new HashSet<>();
            widgets.forEach(widget -> {
                names.add(widget.name);
                widget.destinations.forEach(dst -> names.add(dst.name));
            });
            List<Event<?>> validEvents = customEvents.stream().filter(e -> names.contains(e.getName())).toList();
            eventHistory.setEvents(validEvents);

            widgets.forEach(widget -> {
                List<Event<?>> eventsOnWidget = eventHistory.getEvents(widget.name);
                if (!eventsOnWidget.isEmpty()) {
                    eventsOnWidget.forEach(e -> {
                        if (e.handle instanceof SkinWidget) {
                            ((Event<SkinWidget>) e).handle = widget;
                        }
                    });
                }
                widget.destinations.forEach(dst -> {
                    List<Event<?>> eventsOnDst = eventHistory.getEvents(dst.name);
                    if (!eventsOnDst.isEmpty()) {
                        eventsOnDst.forEach(e -> {
                            if (e.handle instanceof SkinWidgetDestination) {
                                ((Event<SkinWidgetDestination>) e).handle = dst;
                            }
                        });
                    }
                });
            });
            eventHistory.getEvents().forEach(Event::redo);
        }
        widgets.sort(Comparator.comparing(widget -> widget.name));
    }

    public static void registerRemovedObject(String name, String reason) {
        String safeName = name == null ? "No name" : name;
        if (!removedObjects.stream().anyMatch(p -> p.getFirst().equals(name))) {
            removedObjects.add(Pair.of(safeName, reason));
        }
    }

    public static void render() {
        SkinWidgetManager.focus = true;
        if (widgets.isEmpty()) {
            ImGui.text("No skin is loaded");
            return;
        }
        if (ImGui.beginTabBar("SkinWidgetsTabBar")) {
            if (ImGui.beginTabItem("Instances##SkinWidgetManager")) {
                if (ImGui.button("Undo##SkinWidgetManager")) {
                    eventHistory.undo();
                }
                ImGui.sameLine();
                renderFilterOptions();

                renderSkinWidgetsTable();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("History##SkinWidgetManager")) {
                if (ImGui.button("Undo##SkinWidgetManager")) {
                    eventHistory.undo();
                }
                ImGui.sameLine();
                if (ImGui.button("Squash##SkinWidgetManager")) {
                    eventHistory.squash();
                }
                renderHistoryTable();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Removed Objects##SkinWidgetManager")) {
                renderRemovedObjects();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Settings##SkinWidgetManager")) {
                renderSettings();
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

    private static void renderSettings() {
        ImGui.checkbox("Show cursor position", SHOW_CURSOR_POSITION);
        if (ImGui.button("Configure Columns##SkinWidgetManager")) {
            ImGui.openPopup("PreferColumnSettings##SkinWidgetManager");
        }
        if (ImGui.beginPopup("PreferColumnSettings##SkinWidgetManager")) {
            for (WidgetTableColumn column : WIDGET_TABLE_COLUMNS) {
                if (column.persistent) {
                    continue;
                }
                ImGui.checkbox(column.name, column.show);
            }
            ImGui.endPopup();
        }
        if (ImGui.button("Export##SkinWidgetManager")) {
            exportChanges();
        }
    }

    private static void registerSkinObject(SkinObject skinObject, Map<String, Integer> duplicatedSkinObjectNameCount) {
        String skinObjectName = skinObject.getName();

        SkinObject.SkinObjectDestination[] dsts = skinObject.getAllDestination();
        List<SkinWidgetDestination> destinations = new ArrayList<>();
        for (int i = 0; i < dsts.length; ++i) {
            String dstBaseName = skinObjectName == null ? "Unnamed Destination" : skinObjectName;
            String combinedName = dsts.length == 1 ? dstBaseName : String.format("%s(%d)", dstBaseName, i);
            destinations.add(new SkinWidgetDestination(combinedName, dsts[i], eventHistory::pushEvent));
        }

        String widgetBaseName = skinObjectName == null ? "Unnamed Widget" : skinObjectName;
        Integer count = duplicatedSkinObjectNameCount.getOrDefault(widgetBaseName, 0);
        duplicatedSkinObjectNameCount.compute(widgetBaseName, (pk, pv) -> pv == null ? 1 : pv + 1);
        String widgetName = count == 0 ? widgetBaseName : String.format("%s(%d)", widgetBaseName, count);
        widgets.add(new SkinWidget(widgetName, skinObject, destinations, eventHistory::pushEvent));
    }

    private static void renderFilterOptions() {
        if (ImGui.button("Filter Options##SkinWidgetManager")) {
            ImGui.openPopup("Filter Options Popup##SkinWidgetManager");
        }

        if (ImGui.beginPopup("Filter Options Popup##SkinWidgetManager")) {
            if (ImGui.checkbox("Enable Filtering##SkinWidgetManager", enableFilteringWidgets)) {
                enableFilteringWidgets = !enableFilteringWidgets;
            }
            if (ImGui.inputText("Widget Name##FilterOptions", searchWidgetName, ImGuiInputTextFlags.EnterReturnsTrue)) {
                if (!enableFilteringWidgets) {
                    enableFilteringWidgets = true;
                }
                ImGui.closeCurrentPopup();
            }
            helpMarker("Press enter in input box to submit your search string");
            ImGui.endPopup();
        }

        if (enableFilteringWidgets) {
            ImGui.sameLine();
            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb("#FFFF66"));
            ImGui.text(FontAwesomeIcons.Search);
            ImGui.sameLine();
            ImGui.text("Filtering");
            ImGui.popStyleColor();
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
            List<SkinWidget> showingWidgets = !enableFilteringWidgets
                    ? widgets
                    : widgets.stream().filter(widget -> widget.name.contains(searchWidgetName.get())).toList();
            for (SkinWidget widget : showingWidgets) {
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
                if (ImGui.button("Toggle##SkinWidgetManager")) {
                    widget.toggleVisible();
                }

                if (!isWidgetDrawingOnScreen) {
                    ImGui.sameLine();
                    if (ImGui.button("Reason")) {
                        ImGui.openPopup("Reason##SkinWidgetManager");
                    }
                    if (ImGui.beginPopup("Reason##SkinWidgetManager")) {
                        ImGui.text(removedObjects.stream().filter(obj -> obj.getFirst().equals(widget.name)).findAny().map(Pair::getSecond).orElse("ERROR"));
                        ImGui.endPopup();
                    }
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
                        for (int i = 1; i <= colSize - 2; ++i) {
                            WidgetTableColumn column = showingColumns.get(i);
                            drawFloatValueColumn(i, eventHistory.hasEvent(dst.name, column.changeEventType), column.getter.apply(dst));
                        }

                        ImGui.tableSetColumnIndex(colSize - 1);
                        if (ImGui.button("Edit##SkinWidgetManager")) {
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
                            if (ImGui.button("Submit##SkinWidgetManager")) {
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

    private static void renderRemovedObjects() {
        if (ImGui.beginTable("RemovedObjects##Table", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("Name");
            ImGui.tableSetupColumn("Reason");
            ImGui.tableHeadersRow();
            ImGuiListClipper.forEach(removedObjects.size(), new ImListClipperCallback() {
                @Override
                public void accept(int row) {
                    ImGui.pushID(row);
                    ImGui.tableNextRow();

                    Pair<String, String> p = removedObjects.get(row);
                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(p.getFirst());

                    ImGui.tableSetColumnIndex(1);
                    ImGui.text(p.getSecond());
                    ImGui.popID();
                }
            });
            ImGui.endTable();
        }
    }

    /**
     * This is a small helper function to draw columns in table, draw red text if the cell value has been modified
     *
     * @param index    column index
     * @param modified whether current cell's value has been modified
     * @param value    cell value
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
     * Export all changes, currently it writes to a json file as 'custom.json' located as the same as the skin
     */
    private static void exportChanges() {
        eventHistory.squash();
        CustomChanges customChanges = new CustomChanges();
        customChanges.setVersion("1");
        customChanges.setEvents(eventHistory.getEvents().stream().map(Event::persist).toList());
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(Paths.get(skinDirectory, "custom.json").toFile(), customChanges);
            ImGuiNotify.success("Export successfully");
        } catch (Exception e) {
            logger.error("Failed to copy changes to clipboard: ", e);
            ImGuiNotify.error("Failed to copy changes to clipboard " + e.getMessage());
        }
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
    private record WidgetTableColumn(String name, ImBoolean show, boolean persistent,
                                     Function<SkinWidgetDestination, Float> getter, Event.EventType changeEventType) {
    }

    /**
     * A simple collections that holds all events, supporting:
     * <ul>
     *     <li> Push one event </li>
     *     <li> Pop most recent events</li>
     *     <li> Query specified widget has specific event or not </li>
     *     <li> Squash all stored events </li>
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

        public boolean isEmpty() {
            return eventStack.isEmpty();
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

        public void setEvents(List<Event<?>> events) {
            eventStack.clear();
            eventStack.addAll(events);
            targetNameToEvents.clear();
            eventStack.forEach(e -> {
                targetNameToEvents.putIfAbsent(e.getName(), new ArrayList<>());
                targetNameToEvents.get(e.getName()).add(e);
            });
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

        private void squash() {
            List<Event<?>> hand = new ArrayList<>();
            eventStack.forEach(event -> {
                if (hand.isEmpty()) {
                    hand.add(event);
                    return ;
                }

                if (event instanceof ToggleVisibleEvent) {
                    boolean isolated = true;
                    for (Event<?> e_ : hand) {
                        if (e_ instanceof ToggleVisibleEvent && e_.handle.equals(event.handle)) {
                            hand.remove(e_);
                            isolated = false;
                            break;
                        }
                    }
                    if (isolated) {
                        hand.add(event);
                    }
                } else if (event instanceof ChangeSingleFieldEvent) {
                    List<Event<?>> shouldBeRemoved = hand.stream().filter(e_ -> e_ instanceof ChangeSingleFieldEvent && e_.handle.equals(event.handle) && e_.type == event.type).toList();
                    if (!shouldBeRemoved.isEmpty()) {
                        hand.removeAll(shouldBeRemoved);
                    }
                    hand.add(event);
                }
            });

            clear();
            hand.forEach(this::pushEvent);
        }
    }
}
