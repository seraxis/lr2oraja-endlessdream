package bms.player.beatoraja.modmenu.setting.widget;

import bms.model.Mode;
import bms.player.beatoraja.modmenu.FontAwesomeIcons;
import bms.player.beatoraja.modmenu.ImGuiKeyHelper;
import bms.player.beatoraja.modmenu.setting.keybinding.KeyBinding;
import bms.player.beatoraja.modmenu.setting.SettingMenu;
import bms.tool.util.Pair;
import com.badlogic.gdx.Input;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * BlockKeyBindingWidget is a widget renders the play scene key bindings as blocks. It shares the same capability of
 * VerticalKeyBindingWidget but it's designed and optimized for 'play scene' key bindings. It cannot serve more complex
 * requirements like "bind all shortcuts in whole system".
 *
 * @implSpec Like VerticalKeyBindingWidget, BlockKeyBindingWidget doesn't have any capability of modifying the value of
 * keybindings/configurations directly and 'keyBindings' is only a readonly reference.
 */
public class BlockKeyBindingWidget implements Widget {
	private final List<KeyBinding> keyBindings;
	private List<KeyBinding> previousKeyBindings;
	private final Consumer<KeyBinding> newBindingHook;
	private final Consumer<Boolean> editingHook;
	private ImBoolean editing = new ImBoolean(false);
	private int currentEditing = 0;

	/**
	 * @param keyBindings    key bindings, read only reference
	 * @param newBindingHook hook function, triggered when this widget wants to submit a new key binding
	 * @param editingHook    hook function, triggered when this widget changed editing state
	 */
	public BlockKeyBindingWidget(List<KeyBinding> keyBindings, Consumer<KeyBinding> newBindingHook, Consumer<Boolean> editingHook) {
		this.keyBindings = keyBindings;
		this.newBindingHook = newBindingHook;
		this.editingHook = editingHook;
	}

	@Override
	public void render() {
		Mode currentPlayMode = SettingMenu.getCurrentPlayMode();
		if (currentPlayMode != Mode.BEAT_5K && currentPlayMode != Mode.BEAT_7K && currentPlayMode != Mode.POPN_5K && currentPlayMode != Mode.POPN_9K) {
			ImGui.text("Current play mode hasn't been supported");
			return ;
		}
		if (ImGui.checkbox("Edit##KeySettings", editing)) {
			if (editing.get()) {
				// From not editing to editing
				previousKeyBindings = new ArrayList<>(keyBindings);
			} else {
				// From editing to not editing
				resetEditingState();
			}
			editingHook.accept(editing.get());
		}
		// TODO: Currently this widget doesn't support binding SELECT or START key
		List<KeyBinding> playKeyBindings = getPlayKeyBindings();
		if (ImGui.beginTable("##BlockKeyBindingWidgetTable", playKeyBindings.size(), ImGuiTableFlags.SizingFixedFit)) {
			for (int i = 0; i < playKeyBindings.size(); ++i) {
				ImGui.tableSetupColumn(Integer.toString(i), 50, ImGuiTableColumnFlags.WidthFixed);
			}
			ImGui.tableNextRow();
			for (int i = 0; i < playKeyBindings.size(); ++i) {
				if (i == currentEditing) {
					ImGui.tableSetColumnIndex(i);
					float width = ImGui.getColumnWidth();
					float arrowWidth = ImGui.calcTextSizeX(FontAwesomeIcons.ArrowDown);
					ImGui.setCursorPosX(ImGui.getCursorPosX() + (width - arrowWidth) * 0.5F);
					ImGui.text(FontAwesomeIcons.ArrowDown);
				}
			}
			ImGui.tableNextRow();
			for (int i = 0; i < playKeyBindings.size(); ++i) {
				ImGui.pushID(i);
				ImGui.tableSetColumnIndex(i);
				if (i == playKeyBindings.size() - 2 || i == playKeyBindings.size() - 1) {
					ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(125, 0, 0));
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230, 230, 230));
				} else if (i % 2 == 0) {
					ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(0, 0, 139));
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(230, 230, 230));
				} else {
					ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgb(230, 230, 230));
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(49, 49, 49));
				}
				ImGui.button(Input.Keys.toString(playKeyBindings.get(i).keyCode()), 50, 80);
				if (i != playKeyBindings.size() - 1) {
					ImGui.sameLine();
				}
				ImGui.popStyleColor(2);
				ImGui.popID();
			}
			ImGui.endTable();
		}
		if (editing.get()) {
			Pair<Integer, Integer> lastPressedKey = ImGuiKeyHelper.getLastPressedKey();
			Integer keyCode = lastPressedKey.getFirst();
			Integer modifier = lastPressedKey.getSecond();
			if (keyCode != -1) {
				if (keyCode == Input.Keys.ESCAPE) {
					// Escaping before every key has been set, rollback the changes
					resetEditingState();
					return;
				}
				playKeyBindings.get(currentEditing).setKeyCode(keyCode);
				playKeyBindings.get(currentEditing).setModifier(modifier);
				newBindingHook.accept(playKeyBindings.get(currentEditing));
				currentEditing++;
			}
			if (currentEditing == playKeyBindings.size()) {
				currentEditing = 0;
				editing.set(false);
				editingHook.accept(false);
			}
		}
	}

	/**
	 * @return a copy of keyBindings that don't have SELECT & START key
	 */
	private List<KeyBinding> getPlayKeyBindings() {
		return keyBindings.stream().filter(keyBinding -> keyBinding.mapping() != -1 && keyBinding.mapping() != -2).toList();
	}

	private void resetEditingState() {
		currentEditing = 0;
		editing.set(false);
		editingHook.accept(false);
		previousKeyBindings.forEach(newBindingHook);
	}
}
