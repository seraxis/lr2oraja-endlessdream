package bms.player.beatoraja.arena.enums;

public enum ClientToServer {
    CTS_SELECTED_BMS(1),
    CTS_PLAYER_SCORE(2),
    CTS_CHART_CANCELLED(3),
    CTS_LOADING_COMPLETE(4),
    CTS_USERNAME(5),
    CTS_MESSAGE(6),
    CTS_MISSING_CHART(7),
    CTS_SET_HOST(8),
    CTS_KICK_USER(9),
    CTS_ITEM(10),
    CTS_ITEM_SETTINGS(11);

    char value;

    ClientToServer(int value) {
        this.value = (char)value;
    }

    public static ClientToServer from(char value) {
        return switch (value) {
            case 1 -> CTS_SELECTED_BMS;
            case 2 -> CTS_PLAYER_SCORE;
            case 3 -> CTS_CHART_CANCELLED;
            case 4 -> CTS_LOADING_COMPLETE;
            case 5 -> CTS_USERNAME;
            case 6 -> CTS_MESSAGE;
            case 7 -> CTS_MISSING_CHART;
            case 8 -> CTS_SET_HOST;
            case 9 -> CTS_KICK_USER;
            case 10 -> CTS_ITEM;
            case 11 -> CTS_ITEM_SETTINGS;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public char getValue() {
        return value;
    }
}
