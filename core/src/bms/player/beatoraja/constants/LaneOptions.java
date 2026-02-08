package bms.player.beatoraja.constants;

import java.util.Arrays;

public enum LaneOptions {
	NORMAL("NORMAL"),
	MIRROR("MIRROR"),
	RANDOM("RANDOM"),
	RRANDOM("R-RANDOM"),
	SRANDOM("S-RANDOM"),
	SPIRAL("SPIRAL"),
	HRANDOM("HRANDOM"),
	ALLSCR("ALL-SCR"),
	RANDOMEX("RANDOM-EX"),
	SRANDOMEX("S-RANDOM-EX");

	private final String name;

	LaneOptions(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static String[] names() {
		return Arrays.stream(LaneOptions.values()).map(LaneOptions::getName).toArray(String[]::new);
	}

	public static String getName(int v) {
		return Arrays.stream(LaneOptions.values()).filter(op -> op.ordinal() == v).findAny().map(laneOptions -> laneOptions.name).orElse(null);
	}
}
