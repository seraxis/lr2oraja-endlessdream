package bms.player.beatoraja;

import bms.player.beatoraja.RandomTrainer;
import bms.player.beatoraja.controller.Lwjgl3Controller;
import bms.player.beatoraja.controller.Lwjgl3ControllerManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.desktop.JamepadControllerManager;

import imgui.*;
import imgui.flag.*;
import imgui.callback.ImGuiInputTextCallback;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.IntStream;


public class ImGuiRenderer {

    private static long windowHandle;

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;

    private static Lwjgl3ControllerManager manager;

    private static InputProcessor tmpProcessor;

    private static final ImBoolean TRACK_RAN_WHEN_DISABLED = new ImBoolean(false);
    private static ImBoolean TRAINER_ENABLED = new ImBoolean(false);
    private static ImBoolean SHOW_RANDOM_TRAINER = new ImBoolean(false);

    private static ImBoolean BLACK_WHITE_RANDOM_PERMUTATION = new ImBoolean(false);

    private static ArrayList<String> LANE_ORDER = new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7"));

    public static void init() {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        manager = new Lwjgl3ControllerManager();
        windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());

        // Font config for additional fonts
        // This is a natively allocated struct so don't forget to call destroy after atlas is built
        final ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setMergeMode(true);  // Enable merge mode to merge cyrillic, japanese and icons with default font

        final short[] glyphRanges = rangesBuilder.buildRanges();
        io.getFonts().addFontFromMemoryTTF(loadFromResources("skin/default/VL-Gothic-Regular.ttf"), 14, fontConfig, glyphRanges); // japanese glyphs
        io.getFonts().build();

        fontConfig.destroy();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 150");
    }

    public static void start() {
        if (tmpProcessor != null) {
           Gdx.input.setInputProcessor(tmpProcessor);
            tmpProcessor = null;
        }
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

 /*   private void bar_segment(ArrayList<Integer> segment) {
        for (Integer note_position : segment) {
            if (note_position = 0) {

            }
        }
    }*/

    public static void render() {
        if (SHOW_RANDOM_TRAINER.get()) {
            ImGui.begin("Random Trainer", ImGuiWindowFlags.AlwaysAutoResize);

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
            ImGui.checkbox("Trainer Enabled", TRAINER_ENABLED);
            ImGui.sameLine();
            helpMarker("When enabled the RANDOM play option will produce the selected random until disabled.\n\nThe selected random can be changed and the trainer toggled on or off between quick retries without needing to return to song select");
            ImGui.checkbox("Track Current Random", TRACK_RAN_WHEN_DISABLED);
            ImGui.sameLine();
            helpMarker("While the trainer is disabled this option will update the key display to reflect the current random");
            ImGui.checkbox("Black/White Random Select", BLACK_WHITE_RANDOM_PERMUTATION);
            ImGui.unindent();

            RandomTrainer.setActive(TRAINER_ENABLED.get());
            if (TRAINER_ENABLED.get()) {
                String currentUILaneOrder = String.join("", LANE_ORDER);
                if (currentUILaneOrder != RandomTrainer.getLaneOrder()) {
                    RandomTrainer.setLaneOrder(currentUILaneOrder);
                }
            }


            if (ImGui.treeNode("Controller Input Debug Information")) {
                float axis;

                for (Controller con : manager.getControllers()) {
                    ImGui.text("Controller Name: " + con.getName());
                    ImGui.text("Axis: " + con.getAxis(0));
                }
                ImGui.text("GLFW version: " + GLFW.glfwGetVersionString());
                ImGui.treePop();
            }
            ImGui.end();
        }

    }


    public static void end() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().getWantCaptureKeyboard()
                || ImGui.getIO().getWantCaptureMouse()) {
            tmpProcessor = Gdx.input.getInputProcessor();
            Gdx.input.setInputProcessor(null);
        }
    }

    public static void dispose() {
        imGuiGl3.dispose();
        imGuiGl3 = null;
        imGuiGlfw.dispose();
        imGuiGlfw = null;
        ImGui.destroyContext();
    }

    public static void toggleTrainer() {
        if (SHOW_RANDOM_TRAINER.get()) {
            SHOW_RANDOM_TRAINER.set(false);
        } else {
            SHOW_RANDOM_TRAINER.set(true);
        }
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
        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(196,196,196));
        ImGui.text("(drag and drop to reorder lanes)");
        ImGui.popStyleColor();
        ImGui.newLine();
        for(int i = 0; i < LANE_ORDER.size(); i++) {
            ImGui.pushID(i);
            ImGui.sameLine();
            if (Integer.parseInt(LANE_ORDER.get(i)) % 2 == 0) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(0,0,139));
                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230,230,230));
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(230,230,230));
                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(49,49,49));
            }
            if (BLACK_WHITE_RANDOM_PERMUTATION.get()) {
                ImGui.button("", 50, 80);
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
            ImGui.popID();

        }
    }

    private static void helpMarker(String desc) {
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
            ImGui.textUnformatted(desc);
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }

    }

    private static void changeLaneOrder(String random) {
        for (int i = 0; i < LANE_ORDER.size(); i++) {
            LANE_ORDER.set(i, String.valueOf(random.charAt(i)));
        };
    }

    private static byte[] loadFromResources(String name) {
        try {
            return Files.readAllBytes(Gdx.files.internal(name).file().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
