package bms.player.beatoraja.arena.enums;

public enum Gauge {
    GROOVE(0, "GROOVE"),
    HARD(1, "HARD"),
    HAZARD(2, "HAZARD"),
    EASY(3, "GROOVE"),
    PATTACK(4, "P-ATTACK"),
    GATTACK(5, "G-ATTACK");

    final int value;
    final String name;

    Gauge(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Gauge from(int value) {
        return switch (value) {
            case 0 -> GROOVE;
            case 1 -> HARD;
            case 2 -> HAZARD;
            case 3 -> EASY;
            case 4 -> PATTACK;
            case 5 -> GATTACK;
            default -> GROOVE;
        };
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
