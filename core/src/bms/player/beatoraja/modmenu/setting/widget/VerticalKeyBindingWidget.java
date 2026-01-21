package bms.player.beatoraja.modmenu.setting.widget;

import bms.player.beatoraja.modmenu.ImGuiKeyHelper;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.modmenu.setting.KeyBinding;
import com.badlogic.gdx.Input;
import imgui.ImGui;

import java.util.List;
import java.util.function.Consumer;

/**
 * VerticalKeyBindingWidget is a widget renders the key bindings vertically and helps configure bindings. Illustration:
 *
 * <pre>
 * +----------------------------------------------+
 * |    |  Keys  |   Bind    |     Operations     |
 * | -> |   K1   |           |Edit|Remove|Clear   |
 * |    |   K2   |           |Edit|Remove|Clear   |
 * |    |   K3   |           |Edit|Remove|Clear   |
 * |    | ...... | ......... | .................. |
 * +----------------------------------------------+
 * </pre>
 * <p>
 * This widget is in charge of:
 * <ul>
 *     <li>Renders the key bindings</li>
 *     <li>Listens user's inputs and invoke hooks</li>
 *     <li>TODO: Serial rebind mode: Allow user rebind keys from top to down</li>
 * </ul>
 *
 * @implSpec VerticalKeyBindingWidget doesn't have the capability of modifying the value of keyBindings. In other words,
 *  'keyBindings' is not a part of the internal states of VerticalKeyBindingWidget, it's a read-only reference.
 *  The caller side must handles the reference of keyBindings correctly: if the caller side wants to abandon the pointer
 *  to keyBindings, method setKeyBindings must be called.
 */
public class VerticalKeyBindingWidget implements Widget {
	private final String name;
	private final List<KeyBinding> keyBindings;
	private final Consumer<KeyBinding> newBindingHook;
	private int editingLine;

	public VerticalKeyBindingWidget(String name, List<KeyBinding> keyBindings, Consumer<KeyBinding> newBindingHook) {
		this.name = name;
		this.keyBindings = keyBindings;
		this.newBindingHook = newBindingHook;
	}

	@Override
	public void render() {
		if (ImGui.beginTable(name, 3)) {
			ImGui.tableSetupColumn("Keys");
			ImGui.tableSetupColumn("Bind");
			ImGui.tableSetupColumn("Operations");
			ImGui.tableHeadersRow();
			for (int i = 0; i < keyBindings.size(); i++) {
				ImGui.pushID(i);
				KeyBinding keyBinding = keyBindings.get(i);
				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(0);
				ImGui.text(keyBinding.name());
				ImGui.tableSetColumnIndex(1);
				ImGui.text(keyBinding.keyCode() == -1 ? "-" : Input.Keys.toString(keyBinding.keyCode()));
				ImGui.tableSetColumnIndex(2);
				if (ImGui.button("Edit##VerticalKeyBindingWidget")) {
					editingLine = i;
					ImGui.openPopup("##VerticalKeyBindingWidget##ListenKeyPressed");
				}
				if (ImGui.beginPopup("##VerticalKeyBindingWidget##ListenKeyPressed")) {
					ImGui.text("Listening...Please press any key you want to bind");
					int lastPressedKey = ImGuiKeyHelper.getLastPressedKey();
					if (lastPressedKey != -1) {
						if (lastPressedKey != Input.Keys.ESCAPE) {
							newBindingHook.accept(keyBindings.get(editingLine).newKeyCode(lastPressedKey));
						}
						ImGui.closeCurrentPopup();
					}
					ImGui.endPopup();
				}
				ImGui.sameLine();
				if (ImGui.button("Clear##VerticalKeyBindingWidget")) {
					newBindingHook.accept(keyBinding.erase());
				}
				ImGui.popID();
			}
			ImGui.endTable();
		}

	}
}
