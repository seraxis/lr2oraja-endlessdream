package bms.player.beatoraja.modmenu.setting.keybinding;

import com.badlogic.gdx.Input;

public class PlainKeyBinding extends KeyBinding {
	public PlainKeyBinding(String name, int keyCode, int mapping) {
		super(name, keyCode, mapping);
	}

	public PlainKeyBinding(String name, String description, int keyCode, int mapping) {
		super(name, description, keyCode, mapping);
	}

	@Override
	public KeyBinding newKeyCode(int newKeyCode) {
		return new PlainKeyBinding(name(), description(), keyCode(), mapping());
	}

	@Override
	public String keyName() {
		return Input.Keys.toString(keyCode());
	}
}
