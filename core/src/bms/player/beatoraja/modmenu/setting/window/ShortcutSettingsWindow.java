package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.keybinding.*;
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
					/* TODO: Unable to configure at present */ new PlainKeyBinding("Change fullscreen/windowed", Input.Keys.F4, 0, -1),
					/* TODO: Unable to configure at present */ new PlainKeyBinding("Toggle Mod Menu", Input.Keys.F5, 0, -1),
					/* F6 -> */ GlobalKeyBindings.FIRE_SCREEN_SHOT.keyBinding(),
					/* TODO: Unable to configure at present */ new PlainKeyBinding("Post on twitter", Input.Keys.F7, 0, -1)
			)),
			Pair.of("Music Select", Arrays.asList(
					/* TODO: Unable to configure at present */ new PlainKeyBinding("Open Settings Menu", Input.Keys.F1, 0, -1),
					/* F2 -> */ MusicSelectKeyBindings.UPDATE_FOLDER.keyBinding(),
					/* F3 -> */ MusicSelectKeyBindings.OPEN_CHART_DIRECTORY_IN_EXPLORER.keyBinding(),
					/* F8 -> */ MusicSelectKeyBindings.FAVORITE_SONG.keyBinding(),
					/* F9 -> */ MusicSelectKeyBindings.FAVORITE_CHART.keyBinding(),
					/* F10 -> */ MusicSelectKeyBindings.AUTOPLAY_ALL_SONGS_IN_FOLDER.keyBinding(),
					/* F11 -> */ MusicSelectKeyBindings.OPEN_CHART_PAGE_IN_PRIMARY_IR.keyBinding(),
					/* F12 -> */ MusicSelectKeyBindings.OPEN_SKIN_SETTINGS.keyBinding(),
					/* NUM_0 -> */ MusicSelectKeyBindings.FOCUS_SEARCH_TEXT.keyBinding(),
					/* NUM_1 -> */ MusicSelectKeyBindings.SWITCH_PLAY_MODE.keyBinding(),
					/* NUM_2 -> */ MusicSelectKeyBindings.SWITCH_SORT_STRATEGY.keyBinding(),
					/* NUM_3 -> (Changing LN Mode in endless dream is banned) */
					/* NUM_4 -> */ MusicSelectKeyBindings.SELECT_REPLAY_LOG.keyBinding(),
					/* NUM_6 -> */ MusicSelectKeyBindings.OPEN_KEY_CONFIGURATION.keyBinding(),
					/* NUM_7 -> */ MusicSelectKeyBindings.CYCLE_THROUGH_RIVALS.keyBinding(),
					/* NUM_8 -> */ MusicSelectKeyBindings.SHOW_SONGS_IN_SAME_FOLDER.keyBinding(),
					/* NUM_9 -> */ MusicSelectKeyBindings.DISPLAY_SONG_TEXT_FILE.keyBinding(),
					/* PLAY KEY RELATED -> */ new PlayKeyBinding("Play", 0, getKeyboardConfig().getKeyAssign()[0], -1, SettingMenu.getCurrentPlayMode()),
					/* PLAY KEY RELATED -> */ new PlayKeyBinding("Open Context Menu", 2, getKeyboardConfig().getKeyAssign()[2], -1, SettingMenu.getCurrentPlayMode()),
					/* PLAY KEY RELATED -> */ new PlayKeyBinding("Open Context Menu", 4, getKeyboardConfig().getKeyAssign()[4], -1, SettingMenu.getCurrentPlayMode()),
					/* PLAY KEY RELATED -> */ new PlayKeyBinding("Replay", 6, getKeyboardConfig().getKeyAssign()[6], -1, SettingMenu.getCurrentPlayMode()),
					/* PLAY KEY RELATED -> */ new PlayKeyBinding("Options menu 1", -1, getKeyboardConfig().getSelect(), -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					/* PLAY KEY RELATED -> */ new PlayKeyBinding("Options menu 2", -2, getKeyboardConfig().getStart(), -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					/* NUM_5 -> */ MusicSelectKeyBindings.OPTIONS_MENU_3.keyBinding(),
					/* PLAY KEY RELATED -> */ new ArbitraryKeyBinding("Options menu 3", "", "START+SELECT KEY (hold)"),
					/* CTRL+F3 -> */ MusicSelectKeyBindings.COPY_SONG_MD5_HASH.keyBinding(),
					/* SHIFT+F3 -> */ MusicSelectKeyBindings.COPY_SONG_SHA256_HASH.keyBinding(),
					/* CTRL+C */ MusicSelectKeyBindings.COPY_HIGHLIGHTED_MENU_TEXT.keyBinding()
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
					new PlayKeyBinding("Back to music select", 0, getKeyboardConfig().getKeyAssign()[0], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Back to music select", 2, getKeyboardConfig().getKeyAssign()[2], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Retry same song with same pattern option", 4, getKeyboardConfig().getKeyAssign()[4], -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					new PlayKeyBinding("Switch gauge display", 5, getKeyboardConfig().getKeyAssign()[5], -1, SettingMenu.getCurrentPlayMode()),
					new PlayKeyBinding("Retry same song with same note pattern", 6, getKeyboardConfig().getKeyAssign()[6], -1, SettingMenu.getCurrentPlayMode()).requiringHold(),
					/* F11 -> */ ResultKeyBindings.OPEN_CHART_PAGE_IN_PRIMARY_IR.keyBinding()
			))
	);

	private final List<Pair<String, VerticalKeyBindingWidget>> widgets = keyBindings.stream()
			.map(category -> Pair.of(
							category.getFirst(),
							new VerticalKeyBindingWidget(
									category.getSecond(),
									playerConfig::submitKeyBindingModifier, null
							).removeKeyBindingOperationsOnDemand(keyBinding -> !keyBinding.scene().isEmpty())
					)
			)
			.toList();

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
