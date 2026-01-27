package bms.player.beatoraja.modmenu.setting.keybinding;

import com.badlogic.gdx.Input;

import java.util.Arrays;
import java.util.Optional;

public enum ResultKeyBindings {
	OPEN_CHART_PAGE_IN_PRIMARY_IR(builder("Open chart page in the primary IR", Input.Keys.F11).build());
	private final KeyBinding keyBinding;

	ResultKeyBindings(KeyBinding keyBinding) {
		this.keyBinding = keyBinding;
	}

	private static PlainKeyBinding.Builder builder(String name, int keyCode) {
		return new PlainKeyBinding.Builder(name, keyCode)
				.scene("Result");
	}

	public int keyCode() {
		return keyBinding.keyCode();
	}

	public void setKeyCode(int keyCode) {
		keyBinding.setKeyCode(keyCode);
	}

	public int modifier() {
		return keyBinding.modifier();
	}

	public boolean disabled() {
		return keyBinding.disabled();
	}

	public KeyBinding keyBinding() {
		return keyBinding;
	}

	public void setDisabled(boolean disabled) {
		this.keyBinding.setDisabled(disabled);
	}

	public void setModifier(int modifier) {
		this.keyBinding.setModifier(modifier);
	}


	public static Optional<ResultKeyBindings> fromName(String name) {
		return Arrays.stream(ResultKeyBindings.values()).filter(bind -> bind.keyBinding.name().equals(name)).findAny();
	}
}
