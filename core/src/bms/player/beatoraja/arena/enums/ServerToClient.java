package bms.player.beatoraja.arena.enums;

public enum ServerToClient {
    STC_PLAYERS_SCORE(1),
    STC_PLAYERS_READY_UPDATE(2),
    STC_SELECTED_CHART_RANDOM(3),
    STC_USERLIST(4),
    STC_CLIENT_REMOTE_ID(5),
    STC_MESSAGE(6),
    STC_MISSING_CHART(7),
    STC_ITEM(8),
    STC_ITEM_SETTINGS(9);

    char value;

    ServerToClient(int value) {
        this.value = (char)value;
    }

    public static ServerToClient from(char value) {
        return switch (value) {
            case 1 -> STC_PLAYERS_SCORE;
            case 2 -> STC_PLAYERS_READY_UPDATE;
            case 3 -> STC_SELECTED_CHART_RANDOM;
            case 4 -> STC_USERLIST;
            case 5 -> STC_CLIENT_REMOTE_ID;
            case 6 -> STC_MESSAGE;
            case 7 -> STC_MISSING_CHART;
            case 8 -> STC_ITEM;
            case 9 -> STC_ITEM_SETTINGS;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public char getValue() {
        return value;
    }
}
