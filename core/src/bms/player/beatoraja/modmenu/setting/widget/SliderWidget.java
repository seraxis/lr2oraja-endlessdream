package bms.player.beatoraja.modmenu.setting.widget;

import imgui.ImGui;

import java.util.function.Consumer;

public class SliderWidget extends ActionWidget<Integer> {
	private static final float width = 120F;
	private final int[] value = new int[]{50};
	private final String name;

	public SliderWidget(String name, Consumer<Integer> setter) {
		super(setter);
		this.name = name;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void update(Integer value) {
		this.value[0] = value;
	}

	@Override
	public void render() {
		ImGui.pushItemWidth(width);
		if (ImGui.sliderInt(name, value, 0, 100)) {
			super.setter.accept(value[0]);
		}
	}
}
