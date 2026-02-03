package bms.player.beatoraja.modmenu.setting.widget;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import imgui.ImGui;

import java.util.function.Consumer;

public class DragIntegerWidget extends ActionWidget<Integer> {
	private final String name;
	private final int[] value = new int[]{1};

	public DragIntegerWidget(String name, Consumer<Integer> setter) {
		super(setter);
		this.name = name;
	}

	@Override
	protected void update(Integer value) {
		this.value[0] = value;
	}

	@Override
	public float getWidth() {
		return 64F;
	}

	@Override
	public void render() {
		ImGui.pushItemWidth(getWidth());
		if (ImGui.dragInt(name, value)) {
			setter.accept(value[0]);
		}
		ImGui.popItemWidth();
	}
}
