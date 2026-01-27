package bms.player.beatoraja.modmenu.setting.keybinding;

import bms.model.Mode;
import bms.player.beatoraja.PlayModeConfig;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.config.KeyConfiguration;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyBinding {
	private String name;
	private String description;
	private String scene;
	private int keyCode;
	private int modifier;
	private int mapping;
	private boolean disabled;

	public KeyBinding() {

	}

	/**
	 * Create a KeyBinding without description
	 */
	public KeyBinding(String name, int keyCode, int modifier, int mapping) {
		this(name, "", "", keyCode, modifier, mapping, false);
	}

	/**
	 * @param name key's name
	 * @param description a short explanation about this key
	 * @param keyCode -1 represents no bind, otherwise, it's a libgdx keycode
	 * @param mapping internal code, when used as the play mode keys it's the position in config file, -1 represents SELECT and -2 represents START
	 */
	public KeyBinding(String name, String description, String scene, int keyCode, int modifier, int mapping, boolean disabled) {
		this.name = name;
		this.description = description;
		this.scene = scene;
		this.keyCode = keyCode;
		this.modifier = modifier;
		this.mapping = mapping;
		this.disabled = disabled;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getMapping() {
		return mapping;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public String scene() {
		return scene;
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
			bindings.add(new PlainKeyBinding(keys[i], "", keyCode, 0, keysa[i]));
		}
		return bindings;
	}

	public KeyBinding erase() {
		this.keyCode = -1;
		return this;
	}

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

	public int modifier() {
		return modifier;
	}

	public int mapping() {
		return mapping;
	}

	public boolean disabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}
}
