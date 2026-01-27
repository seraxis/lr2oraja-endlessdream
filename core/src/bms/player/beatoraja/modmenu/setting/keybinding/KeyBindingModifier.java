package bms.player.beatoraja.modmenu.setting.keybinding;

public class KeyBindingModifier {
	private String name;
	private int keyCode;
	private int modifier;
	private boolean disabled;

	public KeyBindingModifier() {
		
	}

	public KeyBindingModifier(KeyBinding keyBinding) {
		this(keyBinding.name(), keyBinding.keyCode(), keyBinding.modifier(), keyBinding.disabled());
	}

	public KeyBindingModifier(String name, int keyCode, int modifier, boolean disabled) {
		this.name = name;
		this.keyCode = keyCode;
		this.modifier = modifier;
		this.disabled = disabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getModifier() {
		return modifier;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}
}
