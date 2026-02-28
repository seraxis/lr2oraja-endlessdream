package bms.player.beatoraja.modmenu.setting.widget;

import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EnumComboWidget<T extends Enum<T>> extends ComboWidget<T> {
	public EnumComboWidget(String name, Class<T> clazz, Consumer<T> setter) {
		this(name, clazz, PredefinedWidth.Long, setter);
	}

	public EnumComboWidget(String name, Class<T> clazz, PredefinedWidth predefinedWidth, Consumer<T> setter) {
		this(name, clazz, predefinedWidth.width, setter);
	}

	public EnumComboWidget(String name, Class<T> clazz, float width, Consumer<T> setter) {
		// Blame java for this rubbish again X2
		super(name, new HashMap<>(), new HashMap<>(), width, setter);
		T[] enumConstants = clazz.getEnumConstants();
		super.items = new String[enumConstants.length];
		super.mapping.putAll(Arrays.stream(enumConstants)
				.map(e -> Pair.of(e.ordinal(), e))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
		super.reverseMapping.putAll(Arrays.stream(enumConstants)
				.map(e -> Pair.of(e, e.ordinal()))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
		for (T enumConstant : enumConstants) {
			super.items[enumConstant.ordinal()] = enumConstant.toString();
		}
	}
}
