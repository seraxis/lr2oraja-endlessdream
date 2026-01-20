package bms.player.beatoraja.modmenu.setting;

import bms.model.Mode;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;
import bms.player.beatoraja.modmenu.setting.widget.Widget;
import bms.player.beatoraja.modmenu.setting.window.*;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

import java.util.Arrays;
import java.util.List;

/**
 * SettingMenu is responsible for rendering the panel of the in-game setting. More specifically, it renders two windows:
 * one is the windows that contains the vertical side bars(graphic, volumes, game plays etc...), one is the window for
 * rendering the actual configurable options for current selected topic.
 * <br>
 * Here's a small illustration tells what it shapes like:
 * <pre>
 * +----------------------------------+
 * |(*)Volumes|Volumes Settings       |
 * |Graphics  |                       |
 * |GamePlay  |Master volume       100|
 * ....................................
 * +----------------------------------+
 * </pre>
 * The left side is the vertical side bars, the right side is the actual options.
 */
public class SettingMenu {
	/**
	 * Whether we are focusing the setting menu or not
	 */
	public static boolean focus = false;
	public static MainController mainRef;

	private static final int TAB_WIDTH = 30;
	private static final int PANEL_WIDTH = 100 - TAB_WIDTH;

	private static int selectedTab = 0;

	private static float SCREEN_WIDTH = 0;
	private static float SCREEN_HEIGHT = 0;
	private static float HORIZONTAL_MARGIN = 0;
	private static float VERTICAL_MARGIN = 0;

	private static float SIDEBAR_WIDTH = 64F;
	private static float SIDEBAR_HEIGHT = 32F;

	public static BMSPlayerInputProcessor input;
	private static List<SettingWindow> windows = null;
	private static Mode currentPlayMode = Mode.BEAT_7K;
	public static Widget currentPlayModeSelect = new Widget() {
		private final ImInt value = new ImInt(SettingMenu.getCurrentPlayMode().ordinal());
		private static final String[] PLAY_MODE_OPTIONS = Arrays.stream(Mode.values())
				.map(mode -> mode.hint)
				.toArray(String[]::new);

		@Override
		public void render() {
			if (ImGui.combo("##SettingsMenu##CurrentPlayMode", value, PLAY_MODE_OPTIONS)) {
				SettingMenu.setCurrentPlayMode(Mode.getMode(PLAY_MODE_OPTIONS[value.get()]));
			}
		}
	};

	public static void init(MainController main) {
		mainRef = main;
		SCREEN_WIDTH = main.getConfig().getWindowWidth();
		SCREEN_HEIGHT = main.getConfig().getWindowHeight();
		HORIZONTAL_MARGIN = 0.15f * main.getConfig().getWindowWidth();
		VERTICAL_MARGIN = 0.15f * main.getConfig().getWindowHeight();
		windows = Arrays.asList(
				new SongSettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new AudioSettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new WindowSettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new PlaySettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new AssistSettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new KeySettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new ReplaySettingsWindow(main.getConfig(), main.getPlayerConfig()),
				new DebugWindow(main.getConfig(), main.getPlayerConfig())
		);
		input = main.getInputProcessor();
	}

	public static void render(ImBoolean show) {
		ImGui.setNextWindowPos(HORIZONTAL_MARGIN, VERTICAL_MARGIN, ImGuiCond.Once);
		float windowWidth = SCREEN_WIDTH - 2 * HORIZONTAL_MARGIN;
		float windowHeight = SCREEN_HEIGHT - 2 * VERTICAL_MARGIN;
		ImGui.setNextWindowSize(windowWidth, windowHeight, ImGuiCond.Once);

		if (ImGui.begin("Setting Menu", show, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove)) {
			if (ImGui.beginChild("Setting Menu##Sidebar", SIDEBAR_WIDTH + 8F, 0F, false, ImGuiWindowFlags.NoBackground)) {

				if (ImGui.beginTable("Setting Menu##SideBar##buttons", 1, ImGuiTableFlags.None, SIDEBAR_WIDTH + 8F, SIDEBAR_HEIGHT)) {
					for (int i = 0; i < windows.size(); i++) {
						ImGui.tableNextRow();
						ImGui.tableNextColumn();
						ImGui.pushID(String.format("Setting Menu##SideBar##%d", i));

						boolean selected = selectedTab == i;
						if (selected) {
//							ImGui.pushStyleColor(ImGuiCol.Button, buttonActiveBackground);
//							ImGui.pushStyleColor(ImGuiCol.Border, buttonActiveBorder);
						} else {
//							ImGui.pushStyleColor(ImGuiCol.Button, buttonActiveBackground);
//							ImGui.pushStyleColor(ImGuiCol.Border, buttonActiveBorder);
						}

						ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 2);
						ImGui.pushStyleVar(ImGuiStyleVar.CellPadding, 0, 0);
						if (ImGui.button(windows.get(i).getName(), SIDEBAR_WIDTH + 8F, SIDEBAR_HEIGHT)) {
							selectedTab = i;
						}
						ImGui.popStyleVar();
						ImGui.popStyleVar();

//						ImGui.popStyleColor(2);
						ImGui.popID();
					}
					ImGui.endTable();
				}
			}
			ImGui.endChild();

			float margin = SIDEBAR_WIDTH + 24F;
			ImGui.sameLine(margin);

//			ImGui.pushStyleColor(ImGuiCol.ChildBg, contentBackgroundColor);
//			ImGui.pushStyleColor(ImGuiCol.Border, contentBorderColor);

			ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.3F, 0.3F, 0.3F, 0.8F);
			if (ImGui.beginChild("Setting Menu##Content", windowWidth - margin - 8F, 0F, true, ImGuiWindowFlags.NoResize)) {
//				ImGui.popStyleColor(2);
				windows.get(selectedTab).render();
			} else {
//				ImGui.popStyleColor(2);
			}

			ImGui.popStyleColor();

			ImGui.endChild();
		}
		ImGui.end();
	}

	public static void refresh() {
		windows.forEach(SettingWindow::refresh);
	}

	public static Mode getCurrentPlayMode() {
		return currentPlayMode;
	}

	private static void setCurrentPlayMode(Mode mode) {
		currentPlayMode = mode;
	}
}
