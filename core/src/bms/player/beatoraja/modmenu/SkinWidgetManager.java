package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

public class SkinWidgetManager {
    private static MainController main;
    private static ImFloat editingWidgetX = new ImFloat(0);
    private static ImFloat editingWidgetY = new ImFloat(0);

    public static void setMainController(MainController main) {
        SkinWidgetManager.main = main;
    }

    public static void show(ImBoolean showSkinWidgetManagerMenu) {
        Skin currentSkin = main.getCurrentState().getSkin();
        if (ImGui.begin("Skin Widgets", showSkinWidgetManagerMenu, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.beginTable("Skin Widgets", 3, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
                ImGui.tableSetupScrollFreeze(0, 1);
                ImGui.tableSetupColumn("ID");
                ImGui.tableSetupColumn("x");
                ImGui.tableSetupColumn("y");
                ImGui.tableHeadersRow();
                SkinObject[] allSkinObjects = currentSkin.getAllSkinObjects();
                ImGuiListClipper.forEach(allSkinObjects.length, new ImListClipperCallback() {
                    @Override
                    public void accept(int row) {
                        SkinObject.SkinObjectDestination[] destinations = allSkinObjects[row].getAllDestination();
                        for (int i = 0; i < destinations.length; ++i) {
                            ImGui.tableNextRow();
                            ImGui.pushID(String.format("%d-%d", row, i));
                            SkinObject.SkinObjectDestination dst = destinations[i];
                            String widgetName = destinations.length == 1 ? dst.name : String.format("%s(%d)", dst.name, i + 1);

                            ImGui.tableSetColumnIndex(0);
                            ImGui.text(widgetName);
                            ImGui.sameLine();
                            if (ImGui.button("Edit")) {
                                editingWidgetX.set(dst.region.x);
                                editingWidgetY.set(dst.region.y);
                                ImGui.openPopup("Edit Skin Widget");
                            }
                            if (ImGui.beginPopup("Edit Skin Widget", ImGuiWindowFlags.AlwaysAutoResize)) {
                                ImGui.inputFloat("x", editingWidgetX);
                                ImGui.inputFloat("y", editingWidgetY);
                                if (ImGui.button("Submit")) {
                                    dst.region.setX(editingWidgetX.get());
                                    dst.region.setY(editingWidgetY.get());
                                }
                                ImGui.endPopup();
                            }

                            ImGui.tableSetColumnIndex(1);
                            ImGui.text(String.format("%.4f", dst.region.x));
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text(String.format("%.4f", dst.region.y));
                            ImGui.popID();
                        }
                    }
                });
                ImGui.endTable();
            }
            ImGui.end();
        }
    }
}
