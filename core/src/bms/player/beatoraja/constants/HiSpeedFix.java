package bms.player.beatoraja.constants;

import java.util.Arrays;

public enum HiSpeedFix {
	OFF("OFF"),
	START("START BPM"),
	MAX("MAX BPM"),
	MAIN("MAIN BPM"),
	MIN("MIN BPM");

	private final String name;

	HiSpeedFix(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static String[] names() {
		return Arrays.stream(HiSpeedFix.values()).map(HiSpeedFix::getName).toArray(String[]::new);
	}

	public static String getName(int v) {
		return Arrays.stream(HiSpeedFix.values()).filter(op -> op.ordinal() == v).findAny().map(laneOptions -> laneOptions.name).orElse(null);
	}
}
