package bms.player.beatoraja.modmenu.setting.widget;

import imgui.ImGui;
import imgui.type.ImInt;

import java.util.function.Consumer;

/**
 * StringComboWidget is a widget that renders a couple of options, which are passed as
 * a string array. Please note that this class is not a sub-class of ComboWidget
 */
public class StringComboWidget extends ActionWidget<Integer> {
	private final float width;
	private final String name;
	protected final ImInt value = new ImInt(0);
	protected final String[] items;

	public enum PredefinedWidth {
		Short(64F),
		Medium(128F),
		Long(192F);

		private final float width;

		PredefinedWidth(float width) {
			this.width = width;
		}
	}

	public StringComboWidget(String name, String[] items, Consumer<Integer> setter) {
		this(name, items, PredefinedWidth.Long, setter);
	}

	public StringComboWidget(String name, String[] items, PredefinedWidth predefinedWidth, Consumer<Integer> setter) {
		this(name, items, predefinedWidth.width, setter);
	}

	public StringComboWidget(String name, String[] items, float width, Consumer<Integer> setter) {
		super(setter);
		this.name = name;
		this.items = items;
		this.width = width;
	}

	@Override
	protected void update(Integer value) {
		this.value.set(value);
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void render() {
		ImGui.pushItemWidth(width);
		if (ImGui.combo(name, value, items)) {
			setter.accept(value.get());
		}
	}
}
