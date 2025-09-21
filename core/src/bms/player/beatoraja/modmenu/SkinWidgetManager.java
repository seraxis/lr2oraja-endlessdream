package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.MainController;
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
import java.util.concurrent.atomic.AtomicReference;

public class SkinWidgetManager {
    private static final double eps = 1e-4;
    private static final Object LOCK = new Object();
    private static List<SkinWidget> widgets = new ArrayList<>();
    private static Map<String, List<Event>> events = new HashMap<>();

    private static ImFloat editingWidgetX = new ImFloat(0);
    private static ImFloat editingWidgetY = new ImFloat(0);

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
                    if (ImGui.beginTable("Skin Widgets", 3, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
                        ImGui.tableSetupScrollFreeze(0, 1);
                        ImGui.tableSetupColumn("ID");
                        ImGui.tableSetupColumn("x");
                        ImGui.tableSetupColumn("y");
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
                                    ImGui.openPopup("Edit Skin Widget");
                                }
                                if (ImGui.beginPopup("Edit Skin Widget", ImGuiWindowFlags.AlwaysAutoResize)) {
                                    ImGui.inputFloat("x", editingWidgetX);
                                    ImGui.inputFloat("y", editingWidgetY);
                                    if (ImGui.button("Submit")) {
                                        widget.setDstX(editingWidgetX.get());
                                        widget.setDstY(editingWidgetY.get());
                                        ImGui.closeCurrentPopup();
                                    }
                                    ImGui.endPopup();
                                }

                                ImGui.tableSetColumnIndex(1);
                                if (widget.hasEvent(Event.EventType.CHANGE_X)) {
                                    ImGui.textColored(ImColor.rgb(255, 0, 0), String.format("%.4f", widget.getDstX()));
                                } else {
                                    ImGui.text(String.format("%.4f", widget.getDstX()));
                                }
                                ImGui.tableSetColumnIndex(2);
                                if (widget.hasEvent(Event.EventType.CHANGE_Y)) {
                                    ImGui.textColored(ImColor.rgb(255, 0, 0), String.format("%.4f", widget.getDstY()));
                                } else {
                                    ImGui.text(String.format("%.4f", widget.getDstY()));
                                }
                                ImGui.popID();
                            }
                        });

                        ImGui.endTable();
                    }
                }
            }
            ImGui.end();
        }
    }

    private static void pushEvent(Event.EventType type, String target, float value) {
        events.putIfAbsent(target, new ArrayList<>());
        events.get(target).add(new Event(type, target, value));
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

        public void setDstX(float x) {
            if (Math.abs(x - this.getDstX()) > eps) {
                pushEvent(Event.EventType.CHANGE_X, this.name, x);
            }
            destination.region.x = x;
        }

        public void setDstY(float y) {
            if (Math.abs(y - this.getDstY()) > eps) {
                pushEvent(Event.EventType.CHANGE_Y, this.name, y);
            }
            destination.region.y = y;
        }

        public boolean hasEvent(Event.EventType type) {
            if (!events.containsKey(this.name)) {
                return false;
            }
            List<Event> events = SkinWidgetManager.events.get(this.name);
            return events.stream().anyMatch(event -> event.type == type);
        }
    }

    private static class Event {
        private EventType type;
        private String target; // destination name
        private float value;

        enum EventType {
            CHANGE_X,
            CHANGE_Y
        }

        public Event(EventType type, String target, float value) {
            this.type = type;
            this.target = target;
            this.value = value;
        }
    }
}
