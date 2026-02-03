package bms.player.beatoraja.modmenu.setting.keybinding;

public class ArbitraryKeyBinding extends KeyBinding {
	private String keyName;

	public ArbitraryKeyBinding(String name, String description, String keyName) {
		super(name, description, "", -1, 0, -1, false);
		this.keyName = keyName;
	}

	@Override
	public String keyName() {
		return keyName;
	}
}
