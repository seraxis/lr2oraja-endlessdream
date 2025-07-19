package bms.player.beatoraja.modmenu.fm;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.List;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class FolderManagerMenu {
    private static ImString inputName = new ImString();
    private static ImString errorText = new ImString();
    private static ImBoolean showErrorPopup = new ImBoolean(false);

    public static void show(ImBoolean showFolderManager) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Folder Manager", showFolderManager, ImGuiWindowFlags.AlwaysAutoResize)) {
            // Render misc buttons
            if (ImGui.button("New Folder")) {
                ImGui.openPopup("Create a new folder");
            }
            if (ImGui.beginPopupModal("Create a new folder", ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.inputText("name", inputName);
                if (ImGui.button("OK")) {
                    try {
                        FolderManager.newFolder(inputName.get());
                    } catch (IllegalStateException e) {
                        // Popup is handled as a stack, which means we cannot directly open an
                        // error popup here and expect it 'replace' the current popup
                        errorText.set(e.getMessage());
                        showErrorPopup.set(true);
                    }
                    inputName.clear();
                    ImGui.closeCurrentPopup();
                }
                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    inputName.clear();
                    ImGui.closeCurrentPopup();
                }

                ImGui.endPopup();
            }

            if (showErrorPopup.get()) {
                ImGui.openPopup("Error");
                showErrorPopup.set(false);
            }

            // Render error popup
            if (ImGui.beginPopupModal("Error", ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.text(errorText.get());
                if (ImGui.button("OK")) {
                    ImGui.closeCurrentPopup();
                }
                ImGui.endPopup();
            }

            // Render folders
            List<FolderDefinition> folderDefinitions = FolderManager.getFolderDefinitions();
            for (int i = 0;i < folderDefinitions.size();++i) {
                FolderDefinition folderDefinition = folderDefinitions.get(i);
                ImGui.pushID(i);
                float spacing = ImGui.getStyle().getItemInnerSpacingX();
                ImGui.alignTextToFramePadding();
                ImGui.bulletText(folderDefinition.getName());
                ImGui.sameLine(0.0f, spacing);
                if (ImGui.button("E")) {

                }
                ImGui.sameLine(0.0f, spacing);
                if (ImGui.button("X")) {
                    ImGui.openPopup("Delete?");
                }
                if (ImGui.beginPopupModal("Delete?", ImGuiWindowFlags.AlwaysAutoResize)) {
                    ImGui.text("Do you really want to delete this folder?All related data would be deleted directly and can never be restored!");
                    if (ImGui.button("Yes")) {
                        FolderManager.removeFolder(folderDefinition.getBits());
                        ImGui.closeCurrentPopup();
                    }
                    if (ImGui.button("Cancel")) {
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }
                ImGui.newLine();
                ImGui.popID();
            }
        }

        ImGui.end();
    }
}
