package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.Resolution;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.widget.*;
import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.List;

public class WindowSettingsWindow extends TiledOptionBasedWindow {
	private final EnumComboWidget<Resolution> screenResolution = new EnumComboWidget<>("##Screen Resolution", Resolution.class, config::setResolution);
	private final EnumComboWidget<Config.DisplayMode> windowMode = new EnumComboWidget<>("##Window Mode", Config.DisplayMode.class, newValue -> {
		// NOTE: The reason that we use this 'complex' strategy is because:
		//  1. If we call switchDisplayMode here, inside a imgui render process, imgui would directly crash out.
		//      The reason why is clear.
		//  2. If we try to adapt a lifecycle like "afterRender" (see arena for example), it crashes too because
		//      calling libgdx's graphic functions would cause mystery multithread issues like the switching fullscreen
		//      method would be invoked random times.
		SettingMenu.mainRef.pushDisplayModeFlag(newValue);
	});
	private final DragIntegerWidget maximumFPSCap = new DragIntegerWidget("##Maximum FPS Cap", config::setMaxFramePerSecond);
	private final CheckboxWidget vsync = new CheckboxWidget("##Vsync", config::setVsyncAtRuntime);
	private final CheckboxWidget displayFPS = new CheckboxWidget("##Display FPS Counter", config::setDisplayFPS);
	private final StringComboWidget displayBGA = new StringComboWidget("##Display BGA", new String[]{"On", "Auto", "Off"}, StringComboWidget.PredefinedWidth.Short, config::setBga);
	private final StringComboWidget bgaExpand = new StringComboWidget("##BGA Expand", new String[]{"Full", "Keep Aspect Ratio", "Off"}, StringComboWidget.PredefinedWidth.Medium, config::setBgaExpand);
	private final DragIntegerWidget missLayerDuration = new DragIntegerWidget("##Miss Layer Duration", playerConfig::setMisslayerDuration);

	private final List<Pair<String, List<TiledOption<?>>>> options = List.of(
			Pair.of("Window", Arrays.asList(
					new TiledOption<>("Screen Resolution", config::getResolution, screenResolution).addIcon(
							Label.restartIconLabel()
					),
					new TiledOption<>("Window Mode", config::getDisplaymode, windowMode).addIcon(
							Label.warningIconLabel("Settings menu is working abnormally if you changed from windowed to fullscreen")
					)
			)),
			Pair.of("Frame Limiter", Arrays.asList(
					new TiledOption<>("Maximum FPS Cap", config::getMaxFramePerSecond, maximumFPSCap),
					new TiledOption<>("Vertical Sync", config::isVsync, vsync),
					new TiledOption<>("Display FPS", config::isDisplayFPS, displayFPS)
			)),
			Pair.of("BGA", Arrays.asList(
					new TiledOption<>("Display BGA", config::getBga, displayBGA),
					new TiledOption<>("Miss Layer Duration", playerConfig::getMisslayerDuration, missLayerDuration),
					new TiledOption<>("BGA Expand", config::getBgaExpand, bgaExpand)
			))
	);

	public WindowSettingsWindow(Config config, PlayerConfig playerConfig) {
		super("Window", config, playerConfig);
	}

	@Override
	public List<Pair<String, List<TiledOption<?>>>> getOptions() {
		return options;
	}
}
