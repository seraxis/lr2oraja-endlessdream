package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SkinWidgetManager {
    public static AtomicReference<Skin> skinRef = new AtomicReference<>();

    private static MainController main;
    private static ImFloat editingWidgetX = new ImFloat(0);
    private static ImFloat editingWidgetY = new ImFloat(0);

    public static void setMainController(MainController main) {
        SkinWidgetManager.main = main;
    }

    public static void show(ImBoolean showSkinWidgetManagerMenu) {
        try {
            if (ImGui.begin("Skin Widgets", showSkinWidgetManagerMenu, ImGuiWindowFlags.AlwaysAutoResize)) {
                Skin currentSkin = skinRef.get();
                if (currentSkin == null || currentSkin.getAllSkinObjects().length == 0) {
                    ImGui.text("No skin is loaded");
                } else {
                    if (ImGui.beginTable("Skin Widgets", 3, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
                        ImGui.tableSetupScrollFreeze(0, 1);
                        ImGui.tableSetupColumn("ID");
                        ImGui.tableSetupColumn("x");
                        ImGui.tableSetupColumn("y");
                        ImGui.tableHeadersRow();
                        SkinObject[] allSkinObjects = currentSkin.getAllSkinObjects();
                        List<SkinWidget> widgets = new ArrayList<>();
                        for (SkinObject skinObject : allSkinObjects) {
                            SkinObject.SkinObjectDestination[] dsts = skinObject.getAllDestination();
                            for (int i = 0; i < dsts.length; i++) {
                                String combinedName = dsts.length == 1 ? dsts[i].name : String.format("%s(%d)", dsts[i].name, i);
                                widgets.add(new SkinWidget(combinedName, dsts[i]));
                            }
                        }
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
                                    editingWidgetX.set(widget.destination.region.x);
                                    editingWidgetY.set(widget.destination.region.y);
                                    ImGui.openPopup("Edit Skin Widget");
                                }
                                if (ImGui.beginPopup("Edit Skin Widget", ImGuiWindowFlags.AlwaysAutoResize)) {
                                    ImGui.inputFloat("x", editingWidgetX);
                                    ImGui.inputFloat("y", editingWidgetY);
                                    if (ImGui.button("Submit")) {
                                        widget.destination.region.setX(editingWidgetX.get());
                                        widget.destination.region.setY(editingWidgetY.get());
                                    }
                                    ImGui.endPopup();
                                }

                                ImGui.tableSetColumnIndex(1);
                                ImGui.text(String.format("%.4f", widget.destination.region.x));
                                ImGui.tableSetColumnIndex(2);
                                ImGui.text(String.format("%.4f", widget.destination.region.y));
                                ImGui.popID();
                            }
                        });

                        ImGui.endTable();
                    }
                }
            }
            ImGui.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A simple wrapper class for tweaking destinations' name
    private static class SkinWidget {
        private String name;
        private SkinObject.SkinObjectDestination destination;

        public SkinWidget(String name, SkinObject.SkinObjectDestination destination) {
            this.name = name;
            this.destination = destination;
        }
    }
}
