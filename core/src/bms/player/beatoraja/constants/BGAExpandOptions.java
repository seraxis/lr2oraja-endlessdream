package bms.player.beatoraja.constants;

import java.util.Arrays;

public enum BGAExpandOptions {
	FULL("FULL"),
	KEEP_ASPECT_RATIO("KEEP ASPECT RATIO"),
	OFF("OFF");

	private final String name;

	BGAExpandOptions(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static String[] names() {
		return Arrays.stream(BGAExpandOptions.values()).map(BGAExpandOptions::getName).toArray(String[]::new);
	}

	public static String getName(int v) {
		return Arrays.stream(BGAExpandOptions.values()).filter(op -> op.ordinal() == v).findAny().map(bgaExpandOption -> bgaExpandOption.name).orElse(null);
	}
}
