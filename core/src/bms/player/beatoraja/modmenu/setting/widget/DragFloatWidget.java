package bms.player.beatoraja.modmenu.setting.widget;

import imgui.ImGui;

import java.util.function.Consumer;

public class DragFloatWidget extends ActionWidget<Float> {
	private final String name;
	private final float[] value = new float[]{1};

	public DragFloatWidget(String name, Consumer<Float> setter) {
		super(setter);
		this.name = name;
	}

	@Override
	protected void update(Float value) {
		this.value[0] = value;
	}

	@Override
	public float getWidth() {
		return 64F;
	}

	@Override
	public void render() {
		ImGui.pushItemWidth(getWidth());
		if (ImGui.dragFloat(name, value)) {
			setter.accept(value[0]);
		}
		ImGui.popItemWidth();
	}
}
