package bms.player.beatoraja.modmenu.setting;

import bms.model.Mode;
import bms.player.beatoraja.PlayModeConfig;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.config.KeyConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple data class illustrates the key binding
 *
 * @param name what does this key used for
 * @param keyCode -1 represents no bind, otherwise, it's a libgdx keycode
 * @param mapping internal code, when used as the play mode keys it's the position in config file, -1 represents SELECT and -2 represents START
 */
public record KeyBinding(String name, int keyCode, int mapping) {
	public static List<KeyBinding> keyBoardPlayKeys(PlayerConfig playerConfig, Mode mode) {
		String[] keys = KeyConfiguration.getKeyNames(mode);
		int[] keysa = KeyConfiguration.getKeyAssigns(mode);
		PlayModeConfig playConfig = playerConfig.getPlayConfig(mode);
		PlayModeConfig.KeyboardConfig kbConfig = playConfig.getKeyboardConfig();
		List<KeyBinding> bindings = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			int keyCode = switch (keysa[i]) {
				case -2 -> kbConfig.getSelect();
				case -1 -> kbConfig.getStart();
				default -> kbConfig.getKeyAssign()[keysa[i]];
			};
			bindings.add(new KeyBinding(keys[i], keyCode, keysa[i]));
		}
		return bindings;
	}

	public KeyBinding erase() {
		return newKeyCode(-1);
	}

	public KeyBinding newKeyCode(int newKeyCode) {
		return new KeyBinding(this.name, newKeyCode, this.mapping);
	}
}
