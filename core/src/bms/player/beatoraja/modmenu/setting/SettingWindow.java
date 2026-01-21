package bms.player.beatoraja.modmenu.setting;

public interface SettingWindow {
	String getName();
	void render();

	/**
	 * Updating the internal states. For most cases, it's updating the widgets' value
	 */
	void refresh();
}
