package bms.player.beatoraja.modmenu.skin.debugger;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinHeader;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

/**
 * Facade of the debugging facility of skins
 */
public class SkinDebugger {
	private static final Object LOCK = new Object();
	private static int skinType = SkinHeader.TYPE_BEATORJASKIN;

	public static void init(MainController main) {
		SkinPropertyWatcher.init(main);
	}

	public static void listenBeforeSetSkin() {
		SkinOptions.removeCurrentOptions();
	}

	public static void listenAfterSetSkin(Skin skin) {
		changeSkin(skin);
	}

	private static void changeSkin(Skin skin) {
		synchronized (LOCK) {
			skinType = skin.header.getType();
			SkinWidgetManager.changeSkin(skin);
			SkinOptions.changeSkin(skin);
		}
	}

	public static void show(ImBoolean showSkinDebuggerMenu) {
		synchronized (LOCK) {
			if (ImGui.begin("Skin Debugger", showSkinDebuggerMenu, ImGuiWindowFlags.AlwaysAutoResize)) {
				if (ImGui.beginTabBar("##TabBar##Skin Debugger"))	{
					if (ImGui.beginTabItem("Widgets##Skin Debugger"))	{
						SkinWidgetManager.render();
						ImGui.endTabItem();
					} else {
						SkinWidgetManager.focus = false;
					}
					if (ImGui.beginTabItem("Watcher##Skin Debugger")) {
						SkinPropertyWatcher.render();
						ImGui.endTabItem();
					}
					if (skinType == SkinHeader.TYPE_LR2SKIN) {
						if (ImGui.beginTabItem("Options##Skin Debugger")) {
							SkinOptions.render();
							ImGui.endTabItem();
						}
					}
					ImGui.endTabBar();
				}
			}
			ImGui.end();
		}
	}
}
