package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayConfig;
import bms.player.beatoraja.PlayModeConfig;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.SettingWindow;

public abstract class BaseSettingWindow implements SettingWindow {
	protected Config config;
	protected PlayerConfig playerConfig;

	public BaseSettingWindow(Config config, PlayerConfig playerConfig) {
		this.config = config;
		this.playerConfig = playerConfig;
	}

	public PlayModeConfig getPlayModeConfig() {
		return playerConfig.getPlayConfig(SettingMenu.getCurrentPlayMode());
	}

	public PlayConfig getPlayConfig() {
		return getPlayModeConfig().getPlayconfig();
	}

	public PlayModeConfig.KeyboardConfig getKeyboardConfig() {
		return getPlayModeConfig().getKeyboardConfig();
	}
}
