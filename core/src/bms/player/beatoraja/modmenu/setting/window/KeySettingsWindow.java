package bms.player.beatoraja.modmenu.setting.window;

import bms.model.Mode;
import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayModeConfig;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.FontAwesomeIcons;
import bms.player.beatoraja.modmenu.setting.KeyBinding;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.widget.BlockKeyBindingWidget;
import bms.player.beatoraja.modmenu.setting.widget.Label;
import bms.player.beatoraja.modmenu.setting.widget.VerticalKeyBindingWidget;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public class KeySettingsWindow extends BaseSettingWindow {
	// NOTE: The initial value must be null to trigger the update process for initialization
	private Mode previousMode = null;
	public static boolean editing = false;

	private final List<KeyBinding> keyBindings = new ArrayList<>();
	private final VerticalKeyBindingWidget verticalKeyBindingWidget = new VerticalKeyBindingWidget(keyBindings, this::rebindPlayKey, newValue -> editing = newValue);
	private final BlockKeyBindingWidget blockKeyBindingWidget = new BlockKeyBindingWidget(keyBindings, this::rebindPlayKey, newValue -> editing = newValue);

	public KeySettingsWindow(Config config, PlayerConfig playerConfig) {
		super(config, playerConfig);
	}

	@Override
	public String getName() {
		return "Key";
	}

	@Override
	public void render() {
		new Label.Builder(FontAwesomeIcons.ExclamationTriangle + "Please don't leave this panel until you have configured your bindings.").colorHex("#CC5C76").build().render();
		Mode newPlayMode = SettingMenu.getCurrentPlayMode();
		if (previousMode != newPlayMode) {
			keyBindings.clear();
			keyBindings.addAll(KeyBinding.keyBoardPlayKeys(playerConfig, newPlayMode));
		}
		previousMode = newPlayMode;
		if (ImGui.beginTabBar("##KeySettings Tab Bar")) {
			if (ImGui.beginTabItem("Vertical##KeySettings")) {
				renderVerticalTab();
				ImGui.endTabItem();
			}
			if (ImGui.beginTabItem("Block##KeySettings")) {
				renderBlockTab();
				ImGui.endTabItem();
			}
			ImGui.endTabBar();
		}
	}

	private void renderVerticalTab() {
		verticalKeyBindingWidget.render();
	}

	private void renderBlockTab() {
		blockKeyBindingWidget.render();
	}

	@Override
	public void refresh() {
	}

	private void rebindPlayKey(KeyBinding keyBinding) {
		PlayModeConfig.KeyboardConfig kbConfig = getPlayModeConfig().getKeyboardConfig();
		switch (keyBinding.mapping()) {
			case -1 -> kbConfig.setSelect(keyBinding.keyCode());
			case -2 -> kbConfig.setStart(keyBinding.keyCode());
			default -> kbConfig.getKeyAssign()[keyBinding.mapping()] = keyBinding.keyCode();
		}
		for (int i = 0; i < keyBindings.size(); ++i) {
			if (keyBindings.get(i).name().equals(keyBinding.name())) {
				keyBindings.set(i, keyBinding);
				break;
			}
		}
	}
}
