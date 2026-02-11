package bms.player.beatoraja.constants;

import java.util.Arrays;

public enum DPOptions {
	OFF("OFF"),
	FLIP("FLIP"),
	BATTLE("BATTLE"),
	BATTLEAS("BATTLE AS");

	private final String name;

	DPOptions(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static String[] names() {
		return Arrays.stream(DPOptions.values()).map(DPOptions::getName).toArray(String[]::new);
	}

	public static String getName(int v) {
		return Arrays.stream(DPOptions.values()).filter(op -> op.ordinal() == v).findAny().map(dpOption -> dpOption.name).orElse(null);
	}
}

