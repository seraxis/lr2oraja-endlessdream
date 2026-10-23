package bms.player.beatoraja.constants;

import java.util.Arrays;

public enum BGAOptions {
	ON("ON"),
	AUTO("AUTO"),
	OFF("OFF");

	private final String name;

	BGAOptions(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static String[] names() {
		return Arrays.stream(BGAOptions.values()).map(BGAOptions::getName).toArray(String[]::new);
	}

	public static String getName(int v) {
		return Arrays.stream(BGAOptions.values()).filter(op -> op.ordinal() == v).findAny().map(bgaOption -> bgaOption.name).orElse(null);
	}
}
