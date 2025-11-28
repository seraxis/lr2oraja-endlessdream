package bms.player.beatoraja.play;

public enum JudgeResult {
	PGREAT("PGREAT", 0),
	GREAT("GREAT", 1),
    GOOD("GOOD", 2),
    BAD("BAD", 3),
    POOR("POOR", 4),
	MISS("MISS", 5);

	private String name;
	private int value;

	JudgeResult(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public static JudgeResult valueOf(int value) {
		return switch (value) {
			case 0 -> PGREAT;
			case 1 -> GREAT;
			case 2 -> GOOD;
			case 3 -> BAD;
			case 4 -> POOR;
			case 5 -> MISS;
			default -> null;
		};
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
}
