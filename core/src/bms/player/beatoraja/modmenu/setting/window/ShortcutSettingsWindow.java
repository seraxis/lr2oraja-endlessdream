package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.keybinding.ArbitraryKeyBinding;
import bms.player.beatoraja.modmenu.setting.keybinding.KeyBinding;
import bms.player.beatoraja.modmenu.setting.keybinding.PlainKeyBinding;
import bms.player.beatoraja.modmenu.setting.keybinding.PlayKeyBinding;
import bms.player.beatoraja.modmenu.setting.widget.VerticalKeyBindingWidget;
import bms.tool.util.Pair;
import com.badlogic.gdx.Input;
import imgui.ImGui;

import java.util.Arrays;
import java.util.List;

/**
 * ShortcutSettingsWindow doesn't serve binding capability. It's more like a in-game shortcuts help manual currently.
 */
public class ShortcutSettingsWindow extends BaseSettingWindow {
	private final List<Pair<String, List<KeyBinding>>> keyBindings = Arrays.asList(
			Pair.of("Global", Arrays.asList(
					new PlainKeyBinding("Refresh folder/difficult table", Input.Keys.F2, -1),
					new PlainKeyBinding("Change fullscreen/windowed", Input.Keys.F4, -1),
					new PlainKeyBinding("Toggle Mod Menu", Input.Keys.F5, -1),
					new PlainKeyBinding("Take a screen shot", Input.Keys.F6, -1)
			)),
			Pair.of("Music Select", Arrays.asList(
					new PlainKeyBinding("Open Settings Menu", Input.Keys.F1, -1),
					new PlainKeyBinding("Open chart's directory in explorer", Input.Keys.F3, -1),
					new PlainKeyBinding("Set song as favorite song", Input.Keys.F8, -1),
					new PlainKeyBinding("Set chart as favorite chart", Input.Keys.F9, -1),
					new PlainKeyBinding("Autoplay all songs in folder", Input.Keys.F10, -1),
					new PlainKeyBinding("Open chart page in the primary IR", Input.Keys.F11, -1),
					new PlainKeyBinding("Open skin settings", Input.Keys.F12, -1),
					new PlainKeyBinding("Change play mode filter", Input.Keys.NUM_1, -1),
					new PlainKeyBinding("Change chart sort strategy", Input.Keys.NUM_2, -1),
					new PlainKeyBinding("Change LN mode", Input.Keys.NUM_3, -1),
					new PlainKeyBinding("Select replay log", Input.Keys.NUM_4, -1),
					new PlainKeyBinding("Show detail options", Input.Keys.NUM_5, -1),
					new PlainKeyBinding("Open key configuration", Input.Keys.NUM_6, -1),
					new PlainKeyBinding("Cycle through rivals", Input.Keys.NUM_7, -1),
					new PlainKeyBinding("Show songs in same folder", Input.Keys.NUM_8, -1),
					new PlainKeyBinding("Display song text file", Input.Keys.NUM_9, -1),
					new PlayKeyBinding("Play", 1, getKeyboardConfig().getKeyAssign()[0], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Open Context Menu", 3, getKeyboardConfig().getKeyAssign()[2], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Open Context Menu", 5, getKeyboardConfig().getKeyAssign()[4], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Replay", 7, getKeyboardConfig().getKeyAssign()[6], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Options menu 1", -1, getKeyboardConfig().getSelect(), -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					new PlayKeyBinding("Options menu 2", -2, getKeyboardConfig().getStart(), -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					new ArbitraryKeyBinding("Options menu 3", "", "START+SELECT KEY (hold)")
			)),
			Pair.of("Play", Arrays.asList(
					new ArbitraryKeyBinding("Toggle Sudden+", "", "START KEY x2"),
					new ArbitraryKeyBinding("Adjust Sudden+ height", "", "START KEY + SCR (hold)"),
					new ArbitraryKeyBinding("Adjust green number", "", "SELECT KEY + SCR (hold)"),
					new ArbitraryKeyBinding("Return to music select", "", "START+SELECT KEY (hold)")
			)),
			Pair.of("Autoplay / Replay", Arrays.asList(
					new ArbitraryKeyBinding("x0.25 play speed", "", "1 (hold)"),
					new ArbitraryKeyBinding("x0.55 play speed", "", "2 (hold)"),
					new ArbitraryKeyBinding("x2 play speed", "", "3 (hold)"),
					new ArbitraryKeyBinding("x3 play speed", "", "4 (hold)")
			)),
			Pair.of("Music Result", Arrays.asList(
					new ArbitraryKeyBinding("Save replay in slot 1", "", "1"),
					new ArbitraryKeyBinding("Save replay in slot 2", "", "2"),
					new ArbitraryKeyBinding("Save replay in slot 3", "", "3"),
					new ArbitraryKeyBinding("Save replay in slot 4", "", "4"),
					new PlayKeyBinding("Back to music select", 1, getKeyboardConfig().getKeyAssign()[0], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Back to music select", 3, getKeyboardConfig().getKeyAssign()[2], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Retry same song with same pattern option", 5, getKeyboardConfig().getKeyAssign()[4], -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					new PlayKeyBinding("Switch gauge display", 6, getKeyboardConfig().getKeyAssign()[5], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Retry same song with same note pattern", 7, getKeyboardConfig().getKeyAssign()[6], -1, SettingMenu.getCurrentPlayMode()).requiringHold()
			))
	);

	private final List<Pair<String, VerticalKeyBindingWidget>> widgets = keyBindings.stream().map(category -> Pair.of(category.getFirst(), new VerticalKeyBindingWidget(category.getSecond(), null, null).removeOperations())).toList();

	public ShortcutSettingsWindow(Config config, PlayerConfig playerConfig) {
		super(config, playerConfig);
	}

	@Override
	public String getName() {
		return "Shortcuts";
	}

	@Override
	public void render() {
		if (ImGui.beginTabBar("##ShortcutSettingsWindowTabBar")) {
			widgets.forEach(category -> {
				if (ImGui.beginTabItem(category.getFirst())) {
					category.getSecond().render();
					ImGui.endTabItem();
				}
			});
			ImGui.endTabBar();
		}
	}

	@Override
	public void refresh() {
		// Currently we don't need to do anything here since nothing can be changed :P
	}
}
