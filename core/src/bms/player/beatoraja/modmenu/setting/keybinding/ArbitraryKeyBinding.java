package bms.player.beatoraja.modmenu.setting.keybinding;

public class ArbitraryKeyBinding extends KeyBinding {
	private String keyName;

	public ArbitraryKeyBinding(String name, String description, String keyName) {
		super(name, description, -1, -1);
		this.keyName = keyName;
	}

	@Override
	public KeyBinding newKeyCode(int newKeyCode) {
		return null;
	}

	@Override
	public String keyName() {
		return keyName;
	}
}
