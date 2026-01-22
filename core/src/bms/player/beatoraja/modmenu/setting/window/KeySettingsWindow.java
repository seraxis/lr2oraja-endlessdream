package bms.player.beatoraja.modmenu.setting.window;

import bms.model.Mode;
import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayModeConfig;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.FontAwesomeIcons;
import bms.player.beatoraja.modmenu.ImGuiKeyHelper;
import bms.player.beatoraja.modmenu.setting.KeyBinding;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.player.beatoraja.modmenu.setting.widget.VerticalKeyBindingWidget;
import com.badlogic.gdx.Input;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

public class KeySettingsWindow extends BaseSettingWindow {
	// NOTE: The initial value must be null to trigger the update process for initialization
	private Mode previousMode = null;
	private int[] previousKeys;
	private int[] keys;
	private int currentEditing = 0;
	private final ImBoolean editing = new ImBoolean(false);

	private final List<KeyBinding> keyBindings = new ArrayList<>();
	private final VerticalKeyBindingWidget verticalKeyBindingWidget = new VerticalKeyBindingWidget("##Play Keys Vertical Binding", keyBindings, this::rebindPlayKey);

	public KeySettingsWindow(Config config, PlayerConfig playerConfig) {
		super(config, playerConfig);
	}

	@Override
	public String getName() {
		return "Key";
	}

	@Override
	public void render() {
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
		if (ImGui.checkbox("Edit##KeySettings", editing)) {
			if (editing.get()) {
				// From not editing to editing
				System.arraycopy(keys, 0, previousKeys, 0, keys.length);
			} else {
				// From editing to not editing
				resetEditingState();
			}
		}
		if (ImGui.beginTable("##KeySettingsMenuKeyTable", keys.length, ImGuiTableFlags.SizingFixedFit)) {
			for (int i = 0; i < keys.length;++i) {
				ImGui.tableSetupColumn("" + i, 50, ImGuiTableColumnFlags.WidthFixed);
			}
			ImGui.tableNextRow();
			for (int i = 0; i < keys.length;++i) {
				if (i == currentEditing) {
					ImGui.tableSetColumnIndex(i);
					float width = ImGui.getColumnWidth();
					float arrowWidth = ImGui.calcTextSizeX(FontAwesomeIcons.ArrowDown);
					ImGui.setCursorPosX(ImGui.getCursorPosX() + (width - arrowWidth) * 0.5F);
					ImGui.text(FontAwesomeIcons.ArrowDown);
				}
			}
			ImGui.tableNextRow();
			for (int i = 0; i < keys.length; ++i) {
				ImGui.pushID(i);
				ImGui.tableSetColumnIndex(i);
				if (i == keys.length - 2 || i == keys.length - 1) {
					ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(125   ,0,0));
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230,230,230));
				} else if (i % 2 == 0) {
					ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(0,0,139));
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230,230,230));
				} else {
					ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(230,230,230));
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(49,49,49));
				}
				ImGui.button(Input.Keys.toString(keys[i]), 50, 80);
				if (i != keys.length - 1) {
					ImGui.sameLine();
				}
				ImGui.popStyleColor(2);
				ImGui.popID();
			}
			ImGui.endTable();
		}
		if (editing.get()) {
			int lastPressedKey = ImGuiKeyHelper.getLastPressedKey();
			if (lastPressedKey != -1) {
				if (lastPressedKey == Input.Keys.ESCAPE) {
					// Escaping before every key has been set, rollback the changes
					resetEditingState();
					return ;
				}
				keys[currentEditing] = lastPressedKey;
				currentEditing++;
			}
			if (currentEditing == keys.length) {
				currentEditing = 0;
				editing.set(false);
			}
		}
	}

	@Override
	public void refresh() {
		keys = getPlayModeConfig().getKeyboardConfig().getKeyAssign();
		previousKeys = new int[keys.length];
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

	private void resetEditingState() {
		System.arraycopy(previousKeys, 0, keys, 0, keys.length);
		currentEditing = 0;
		editing.set(false);
	}
}
