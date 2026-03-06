package bms.player.beatoraja.play;

public enum JudgeResult {
	EARLY_PGREAT("EARLY_PGREAT", 6),
	LATE_PGREAT("LATE_PGREAT", 7),
	EARLY_GREAT("EARLY_GREAT", 8),
	LATE_GREAT("LATE_GREAT", 9),
	EARLY_GOOD("EARLY_GOOD", 10),
	LATE_GOOD("LATE_GOOD", 11),
	EARLY_BAD("EARLY_BAD", 12),
	LATE_BAD("LATE_BAD", 13),
	EARLY_POOR("EARLY_POOR", 14),
	LATE_POOR("LATE_POOR", 15),
	EARLY_MISS("EARLY_MISS", 16),
	LATE_MISS("LATE_MISS", 17);

	private final String name;
	private final int value;

	JudgeResult(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public static JudgeResult valueOf(int value, boolean isFast) {
		return switch (value) {
			case 0 -> isFast ? EARLY_PGREAT : LATE_PGREAT;
			case 1 -> isFast ? EARLY_GREAT : LATE_GREAT;
			case 2 -> isFast ? EARLY_GOOD : LATE_GOOD;
			case 3 -> isFast ? EARLY_BAD : LATE_BAD;
			case 4 -> isFast ? EARLY_POOR : LATE_POOR;
			case 5 -> isFast ? EARLY_MISS : LATE_MISS;
			case 6 -> EARLY_PGREAT;
			case 7 -> LATE_PGREAT;
			case 8 -> EARLY_GREAT;
			case 9 -> LATE_GREAT;
			case 10 -> EARLY_GOOD;
			case 11 -> LATE_GOOD;
			case 12 -> EARLY_BAD;
			case 13 -> LATE_BAD;
			case 14 -> EARLY_POOR;
			case 15 -> LATE_POOR;
			case 16 -> EARLY_MISS;
			case 17 -> LATE_MISS;
			default -> throw new IllegalArgumentException();
		};
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
}
