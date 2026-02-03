package bms.player.beatoraja.modmenu.setting.widget;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImInt;

import java.util.function.Consumer;

/**
 * This is an unfinished widget
 */
public class IntegerModalWidget extends ActionWidget<Integer> {
	private final ImInt value = new ImInt(0);
	private final String name;
	private final String desc;
	private float width = 32F;

	public IntegerModalWidget(String name, Consumer<Integer> setter) {
		this(name, "", setter);
	}

	public IntegerModalWidget(String name, String desc, Consumer<Integer> setter) {
		super(setter);
		this.name = name;
		this.desc = desc;
	}

	@Override
	protected void update(Integer value) {
		this.value.set(value);
	}

	@Override
	public void render() {
		ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, new ImVec2(0, 0));
		if (ImGui.button(Integer.toString(value.get()))) {
			ImGui.openPopup(name + "##Modal");
		}
		ImGui.popStyleVar();
		if (ImGui.beginPopupModal(name + "##Modal")) {
			if (desc != null && !desc.isEmpty()) {
				ImGui.textDisabled(desc);
				ImGui.newLine();
			}
			ImGui.inputInt(name + "##input int", value);
			ImGui.newLine();
			if (ImGui.button("Cancel##" + name)) {
				ImGui.closeCurrentPopup();
			}
			ImGui.sameLine();
			if (ImGui.button("Save##" + name)) {
				setter.accept(value.get());
				ImGui.closeCurrentPopup();
			}
			ImGui.endPopup();
		}
	}

	@Override
	public float getWidth() {
		return width;
	}
}
