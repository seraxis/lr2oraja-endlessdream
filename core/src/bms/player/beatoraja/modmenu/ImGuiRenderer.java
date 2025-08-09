package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.controller.Lwjgl3ControllerManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.controllers.Controller;

import imgui.*;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;


public class ImGuiRenderer {

    private static long windowHandle;

    public static int windowWidth;
    public static int windowHeight;

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;

    private static Lwjgl3ControllerManager manager;

    private static InputProcessor tmpProcessor;

    private static ImBoolean SHOW_MOD_MENU = new ImBoolean(false);
    private static ImBoolean SHOW_RANDOM_TRAINER = new ImBoolean(false);

    private static ImBoolean SHOW_FREQ_PLUS = new ImBoolean(false);

    private static ImBoolean SHOW_JUDGE_TRAINER = new ImBoolean(false);


    public static void init() {
        Lwjgl3Graphics lwjglGraphics = ((Lwjgl3Graphics) Gdx.graphics);

        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        manager = new Lwjgl3ControllerManager();

        windowHandle = lwjglGraphics.getWindow().getWindowHandle();
        windowWidth = lwjglGraphics.getWidth();
        windowHeight = lwjglGraphics.getHeight();

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename("layout.ini");
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

    public static void render() {
        // Relative from top left corner, so 44% from the left, 2% from the top
        float relativeX = windowWidth * 0.44f;
        float relativeY = windowHeight * 0.02f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.Once);

        if (SHOW_MOD_MENU.get()) {
            ImGui.begin("Endless Dream", ImGuiWindowFlags.AlwaysAutoResize);

            ImGui.checkbox("Show Rate Modifier Window", SHOW_FREQ_PLUS);
            ImGui.checkbox("Show Random Trainer Window", SHOW_RANDOM_TRAINER);
            ImGui.checkbox("Show Judge Trainer Window", SHOW_JUDGE_TRAINER);

            if (SHOW_FREQ_PLUS.get()) {
                FreqTrainerMenu.show(SHOW_FREQ_PLUS);
            }
            if (SHOW_RANDOM_TRAINER.get()) {
                RandomTrainerMenu.show(SHOW_RANDOM_TRAINER);
            }
            if (SHOW_JUDGE_TRAINER.get()) {
                JudgeTrainerMenu.show(SHOW_JUDGE_TRAINER);
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

    public static void toggleMenu() {
        SHOW_MOD_MENU.set(!SHOW_MOD_MENU.get());
    }

    public static void helpMarker(String desc) {
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
            ImGui.textUnformatted(desc);
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }

    }


    private static byte[] loadFromResources(String name) {
        try {
            return Files.readAllBytes(Gdx.files.internal(name).file().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
