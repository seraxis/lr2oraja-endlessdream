package bms.player.beatoraja.modmenu.setting.widget;

import bms.player.beatoraja.modmenu.ImGuiKeyHelper;
import bms.player.beatoraja.modmenu.setting.keybinding.KeyBinding;
import bms.tool.util.Pair;
import com.badlogic.gdx.Input;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.lwjgl.system.windows.INPUT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
	private final List<KeyBinding> keyBindings;
	private boolean hasOperations = true;
	private Predicate<KeyBinding> removeKeyBindingOperations;
	private final Consumer<KeyBinding> newBindingHook;
	private final Consumer<Boolean> editingHook;
	private int editingLine;
	private int listeningState = 0;
	private List<Pair<Integer, Integer>> listeningKeys = new ArrayList<>();

	/**
	 * @param keyBindings    key bindings, read only reference
	 * @param newBindingHook hook function, triggered when this widget wants to submit a new key binding
	 * @param editingHook    hook function, triggered when this widget changed editing state
	 */
	public VerticalKeyBindingWidget(List<KeyBinding> keyBindings, Consumer<KeyBinding> newBindingHook, Consumer<Boolean> editingHook) {
		this.keyBindings = keyBindings;
		this.newBindingHook = newBindingHook;
		this.editingHook = editingHook;
	}

	public VerticalKeyBindingWidget removeOperations() {
		hasOperations = false;
		return this;
	}

	public VerticalKeyBindingWidget removeKeyBindingOperationsOnDemand(Predicate<KeyBinding> predicate) {
		this.removeKeyBindingOperations = predicate;
		return this;
	}

	@Override
	public void render() {
		int columns = hasOperations ? 3 : 2;
		if (ImGui.beginTable("##VerticalKeyBindingWidgetTable", columns)) {
			ImGui.tableSetupColumn("Keys");
			ImGui.tableSetupColumn("Bind");
			if (hasOperations) {
				ImGui.tableSetupColumn("Operations");
			}
			ImGui.tableHeadersRow();
			for (int i = 0; i < keyBindings.size(); i++) {
				ImGui.pushID(i);
				KeyBinding keyBinding = keyBindings.get(i);
				ImGui.tableNextRow();
				boolean disabledLine = keyBinding.disabled();
				if (disabledLine) {
					ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb("#808080"));
				}
				ImGui.tableSetColumnIndex(0);
				ImGui.text(keyBinding.name());
				ImGui.tableSetColumnIndex(1);
				ImGui.text(keyBinding.keyName());
				if (hasOperations && (removeKeyBindingOperations != null && removeKeyBindingOperations.test(keyBinding))) {
					ImGui.tableSetColumnIndex(2);
					ImGui.beginDisabled(disabledLine);
					if (ImGui.button("Edit##VerticalKeyBindingWidget")) {
						editingLine = i;
						if (editingHook != null) {
							editingHook.accept(true);
						}
						ImGui.openPopup("##VerticalKeyBindingWidget##ListenKeyPressed");
					}
					if (ImGui.beginPopup("##VerticalKeyBindingWidget##ListenKeyPressed")) {
						ImGui.text("Listening...Please press any key you want to bind");
						Pair<Integer, Integer> lastPressedKey = ImGuiKeyHelper.getLastDownKey();
						Integer keyCode = lastPressedKey.getFirst();
						if (keyCode == Input.Keys.ESCAPE) {
							listeningState = 0;
							listeningKeys = new ArrayList<>();
						} else {
							if (listeningState == 0) {
								if (keyCode != -1) {
									listeningState = 1;
									listeningKeys.add(lastPressedKey);
								}
							} else {
								if (keyCode != -1) {
									listeningKeys.add(lastPressedKey);
								} else {
									if (!listeningKeys.isEmpty()) {
										int mainKeyCode = listeningKeys.get(0).getFirst();
										int modifier = 0;
										for (Pair<Integer, Integer> key : listeningKeys) {
											modifier |= key.getSecond();
										}
										keyBindings.get(editingLine).setKeyCode(mainKeyCode);
										keyBindings.get(editingLine).setModifier(modifier);
										newBindingHook.accept(keyBindings.get(editingLine));
									}
									listeningState = 0;
									listeningKeys = new ArrayList<>();
									if (editingHook != null) {
										editingHook.accept(false);
									}
									ImGui.closeCurrentPopup();
								}
							}
						}
						ImGui.endPopup();
					}
					ImGui.sameLine();
					if (ImGui.button("Clear##VerticalKeyBindingWidget")) {
						newBindingHook.accept(keyBinding.erase());
					}
					ImGui.endDisabled();
					if (disabledLine) {
						ImGui.popStyleColor();
					}
					ImGui.sameLine();
					String disableButtonText = keyBinding.disabled()
							? "Enable##VerticalKeyBindingWidget"
							: "Disable##VerticalKeyBindingWidget";
					if (ImGui.button(disableButtonText)) {
						keyBinding.setDisabled(!keyBinding.isDisabled());
						newBindingHook.accept(keyBinding);
					}
				} else if (disabledLine) {
					ImGui.popStyleColor();
				}

				ImGui.popID();
			}
			ImGui.endTable();
		}
	}
}
