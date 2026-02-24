package bms.player.beatoraja.modmenu.setting.keybinding;

import bms.model.Mode;
import bms.player.beatoraja.PlayModeConfig;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.config.KeyConfiguration;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyBinding {
	private final String name;
	private final String description;
	private final int keyCode;
	private final int mapping;

	/**
	 * Create a KeyBinding without description
	 */
	public KeyBinding(String name, int keyCode, int mapping) {
		this(name, "", keyCode, mapping);
	}

	/**
	 * @param name key's name
	 * @param description a short explanation about this key
	 * @param keyCode -1 represents no bind, otherwise, it's a libgdx keycode
	 * @param mapping internal code, when used as the play mode keys it's the position in config file, -1 represents SELECT and -2 represents START
	 */
	public KeyBinding(String name, String description, int keyCode, int mapping) {
		this.name = name;
		this.description = description;
		this.keyCode = keyCode;
		this.mapping = mapping;
	}

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
			if (keyCode == -1) {
				keyCode = Integer.MIN_VALUE;
			}
			bindings.add(new PlainKeyBinding(keys[i], "", keyCode, keysa[i]));
		}
		return bindings;
	}

	public KeyBinding erase() {
		return newKeyCode(-1);
	}

	public abstract KeyBinding newKeyCode(int newKeyCode);

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public abstract String keyName();

	public int keyCode() {
		return keyCode;
	}

	public int mapping() {
		return mapping;
	}
}
