package bms.player.beatoraja.modmenu.setting.widget;

import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.function.Consumer;

public class CheckboxWidget extends ActionWidget<Boolean> {
	private static final float width = 19F;
	private final String name;
	private final ImBoolean value = new ImBoolean(false);

	public CheckboxWidget(String name, Consumer<Boolean> setter) {
		super(setter);
		this.name = name;
	}

	@Override
	protected void update(Boolean value) {
		this.value.set(value);
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void render() {
		ImGui.pushItemWidth(width);
		if (ImGui.checkbox("##" + name, value)) {
			setter.accept(value.get());
		}
	}
}
