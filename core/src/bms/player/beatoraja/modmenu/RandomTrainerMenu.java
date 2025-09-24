package bms.player.beatoraja.modmenu;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

public class RandomTrainerMenu {
    private static ImBoolean RANDOM_TRAINER_ENABLED = new ImBoolean(false);

    private static ImBoolean BLACK_WHITE_RANDOM_PERMUTATION = new ImBoolean(false);

    private static ArrayList<String> LANE_ORDER = new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7"));

    private static final ImBoolean TRACK_RAN_WHEN_DISABLED = new ImBoolean(false);


    public static void show(ImBoolean showRandomTrainer) {
        float relativeX = windowWidth * 0.455f;
        float relativeY = windowHeight * 0.04f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if(ImGui.begin("Random Trainer", showRandomTrainer, ImGuiWindowFlags.AlwaysAutoResize)) {
            // Update key display when tracking random
            if (TRACK_RAN_WHEN_DISABLED.get() && !RandomTrainer.getRandomHistory().isEmpty()) {
                String lastRan = RandomTrainer.getRandomHistory().getFirst().getRandom();
                changeLaneOrder(lastRan);
            }

            if (BLACK_WHITE_RANDOM_PERMUTATION.get()) {
                RandomTrainer.setBlackWhitePermute(true);
            } else {
                RandomTrainer.setBlackWhitePermute(false);
            }

            // Key display
            dragAndDropKeyDisplay();
            //ImGui.newLine();

            // Random History
            randomHistory();
            ImGui.newLine();

            // Controls
            ImGui.text("Controls");

            ImGui.indent();
            ImGui.checkbox("Trainer Enabled", RANDOM_TRAINER_ENABLED);
            ImGui.sameLine();
            helpMarker("When enabled the RANDOM play option will produce the selected random until disabled.\n\nThe selected random can be changed and the trainer toggled on or off between quick retries without needing to return to song select");
            ImGui.checkbox("Track Current Random", TRACK_RAN_WHEN_DISABLED);
            ImGui.sameLine();
            helpMarker("While the trainer is disabled this option will update the key display to reflect the current random");
            ImGui.checkbox("Black/White Random Select", BLACK_WHITE_RANDOM_PERMUTATION);
            ImGui.unindent();

            ImGui.newLine();
            if (ImGui.button("Mirror")) {
                mirrorLaneOrder();
            }
            ImGui.sameLine();
            if (ImGui.button("Shift Left")) {
                shiftLeftLaneOrder();
            }
            ImGui.sameLine();
            if (ImGui.button("Shift Right")) {
                shiftRightLaneOrder();
            }

            RandomTrainer.setActive(RANDOM_TRAINER_ENABLED.get());
            if (RANDOM_TRAINER_ENABLED.get()) {
                String currentUILaneOrder = String.join("", LANE_ORDER);
                if (currentUILaneOrder != RandomTrainer.getLaneOrder()) {
                    RandomTrainer.setLaneOrder(currentUILaneOrder);
                }
            }
        }
        ImGui.end();
    }

    private static void randomHistory() {
        if (ImGui.treeNode("Random History")) {
            ImGui.sameLine();
            helpMarker("Double click the contents of a row to select it as the current random");
            int flags = ImGuiTableFlags.ScrollY | ImGuiTableFlags.RowBg | ImGuiTableFlags.BordersOuter | ImGuiTableFlags.Resizable | ImGuiTableFlags.SizingStretchSame;

            float outer_size = ImGui.getTextLineHeightWithSpacing() * 8;
            if (ImGui.beginTable("RanTrainerLaneOrderHistory", 2, flags, 0, outer_size)) {

                ImGui.tableSetupScrollFreeze(0, 1);
                ImGui.tableSetupColumn("Song Title");
                ImGui.tableSetupColumn("Random");
                ImGui.tableHeadersRow();

                RandomTrainer.getRandomHistory().forEach(entry -> {
                    ImGui.tableNextRow();
                    for (int col = 0; col < 2; col++) {
                        ImGui.tableSetColumnIndex(col);
                        if (col % 2 == 0) {
                            ImGui.text(entry.getTitle());
                        } else {
                            ImGui.text(entry.getRandom());
                        }
                        if(ImGui.isItemHovered()) {
                            ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, ImColor.rgb(110, 90, 20));
                            if (ImGui.isMouseDoubleClicked(0)) {
                                changeLaneOrder(entry.getRandom());
                            }
                        }
                    }
                });
                ImGui.endTable();
            }

            ImGui.treePop();
        }
    }

    private static void dragAndDropKeyDisplay() {
        ImGui.text("Random Select");
        ImGui.sameLine();
//        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(196,196,196));
//        ImGui.text("(drag and drop to reorder lanes)");
//        ImGui.popStyleColor();
        helpMarker("Drag and drop to reorder lanes, right click to toggle random.");
        ImGui.newLine();
        for(int i = 0; i < LANE_ORDER.size(); i++) {
            ImGui.pushID(i);
            ImGui.sameLine();
            boolean toRandom = RandomTrainer.isLaneToRandom(LANE_ORDER.get(i).charAt(0));
            if (toRandom) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(180,100,140));
                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230,230,230));
            } else if (Integer.parseInt(LANE_ORDER.get(i)) % 2 == 0) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(0,0,139));
                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230,230,230));
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(230,230,230));
                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(49,49,49));
            }
            if (BLACK_WHITE_RANDOM_PERMUTATION.get()) {
                ImGui.button("", 50, 80);
            } else if (toRandom) {
                ImGui.button("?", 50, 80);
            } else {
                ImGui.button(LANE_ORDER.get(i), 50, 80);
            }

            ImGui.popStyleColor(2);

            if (ImGui.beginDragDropSource(ImGuiDragDropFlags.None)) {
                ImGui.setDragDropPayload("RT_LANE_MEMBER", (Object) i);
                ImGui.endDragDropSource();
            }
            if (ImGui.beginDragDropTarget()) {
                if (ImGui.acceptDragDropPayload("RT_LANE_MEMBER", Integer.class) != null) {
                    int payload_i = ImGui.acceptDragDropPayload("RT_LANE_MEMBER");

                    Collections.swap(LANE_ORDER, i, payload_i);
                }

                ImGui.endDragDropTarget();
            }
            if (ImGui.isItemClicked(1)) {
                if (toRandom) {
                    RandomTrainer.removeLaneToRandom(LANE_ORDER.get(i).charAt(0));
                } else {
                    RandomTrainer.setLaneToRandom(LANE_ORDER.get(i).charAt(0));
                }
            }
            ImGui.popID();

        }
    }

    private static void changeLaneOrder(String random) {
        for (int i = 0; i < LANE_ORDER.size(); i++) {
            LANE_ORDER.set(i, String.valueOf(random.charAt(i)));
        };
    }

    private static String getLaneOrder() {
        return String.join("", LANE_ORDER);
    }

    /**
     * 1234567 -> 7654321
     */
    public static void mirrorLaneOrder() {
        changeLaneOrder(new StringBuilder(getLaneOrder()).reverse().toString());
    }

    /**
     * 1234567 -> 2345671
     *  |----| -> |----|
     */
    public static void shiftLeftLaneOrder() {
        String s = getLaneOrder();
        changeLaneOrder(s.substring(1) + s.charAt(0));
    }

    /**
     * 1234567 -> 7123456
     * |----|  ->  |----|
     */
    public static void shiftRightLaneOrder() {
        String s = getLaneOrder();
        changeLaneOrder(s.charAt(s.length() - 1) + s.substring(0, s.length() - 1));
    }
}
