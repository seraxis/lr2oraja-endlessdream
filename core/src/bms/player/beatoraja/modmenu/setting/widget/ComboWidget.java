package bms.player.beatoraja.modmenu.setting.widget;

import bms.tool.util.Pair;
import imgui.ImGui;
import imgui.type.ImInt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ComboWidget<T> extends ActionWidget<T> {
	private final String name;
	private final float width;
	private final ImInt value = new ImInt();
	protected String[] items;
	protected final Map<Integer, T> mapping;
	protected final Map<T, Integer> reverseMapping;

	public enum PredefinedWidth {
		Short(64F),
		Medium(128F),
		Long(192F);

		final float width;

		PredefinedWidth(float width) {
			this.width = width;
		}
	}

	public ComboWidget(String name, Map<Integer, T> mapping, Consumer<T> setter) {
		this(name, mapping, mapping.entrySet().stream().map( entry ->
				Pair.of(entry.getValue(), entry.getKey())
		).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)), PredefinedWidth.Long, setter);
	}

	public ComboWidget(String name, T[] items, Consumer<T> setter) {
		this(name, items, PredefinedWidth.Long, setter);
	}

	public ComboWidget(String name, T[] items, PredefinedWidth predefinedWidth, Consumer<T> setter) {
		// Blame java for this rubbish code
		this(name, new HashMap<>(), new HashMap<>(), predefinedWidth.width, setter);
		for (int i = 0; i < items.length; i++) {
			this.mapping.put(i, items[i]);
			this.reverseMapping.put(items[i], i);
		}
	}

	public ComboWidget(String name, Map<Integer, T> mapping, Map<T, Integer> reverseMapping, PredefinedWidth predefinedWidth, Consumer<T> setter) {
		this(name, mapping, reverseMapping, predefinedWidth.width, setter);
	}

	public ComboWidget(String name, Map<Integer, T> mapping, Map<T, Integer> reverseMapping, float width, Consumer<T> setter) {
		super(setter);
		this.mapping = mapping;
		this.reverseMapping = reverseMapping;
		this.name = name;
		this.width = width;
		this.items = Arrays.stream(mapping.values().stream().map(Objects::toString).toArray(String[]::new)).map(Object::toString).toArray(String[]::new);
	}

	public void update(T value) {
		this.value.set(reverseMapping.get(value));
	}

	@Override
	public void render() {
		ImGui.pushItemWidth(width);
		if (ImGui.combo(name, value, items)) {
			setter.accept(mapping.get(value.get()));
		}
	}

	@Override
	public float getWidth() {
		return width;
	}
}
