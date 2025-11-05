package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.select.MusicSelector;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

import java.util.Arrays;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class ProfileSwitcherMenu {
  private static MainController main;

  private static Config config;

  private static ImInt SELECTED_PLAYER;

  private static String[] players = PlayerConfig.readAllPlayerID("player");


  public static void show(ImBoolean showPlayerManager) {
    float relativeX = windowWidth * 0.455f;
    float relativeY = windowHeight * 0.04f;
    ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

    if (ImGui.begin("Profile Switcher", showPlayerManager, ImGuiWindowFlags.AlwaysAutoResize)) {
      ImGui.text("Change profile while in-game");
      ImGui.newLine();
      ImGui.text("Player Profile");
      ImGui.combo("##Player Profile", SELECTED_PLAYER, players, 4);
      ImGui.sameLine();
      boolean clicked = ImGui.button("Switch!");
      
      if (
        main.getCurrentState() instanceof MusicSelector
          && clicked
          && !config.getPlayername().equals(players[SELECTED_PLAYER.get()])
      ) {
        PlayerConfig newPlayerConfig = PlayerConfig.readPlayerConfig("player", players[SELECTED_PLAYER.get()]);
        ImGuiRenderer.toggleMenu();

        main.saveConfig();
        main.loadNewProfile(newPlayerConfig);
      }
    }
    
    ImGui.end();
  }

  public static void setMain(MainController main) {
    ProfileSwitcherMenu.main = main;
    ProfileSwitcherMenu.config = main.getConfig();
    ProfileSwitcherMenu.SELECTED_PLAYER = new ImInt(Arrays.asList(players).indexOf(config.getPlayername()));
  }
}
