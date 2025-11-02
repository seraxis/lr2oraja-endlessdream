package bms.player.beatoraja.play;

import java.util.Optional;
import bms.player.beatoraja.pattern.Random;

public class GhostBattlePlay {
    private static Settings battle = null;

    public record Settings(Random random, int lanes) {}

    public static Optional<Settings> consume() {
        if (battle == null) return Optional.empty();
        Settings start = battle;
        battle = null;
        return Optional.of(start);
    }

    public static void setup(Random random, int laneSequence) {
        battle = new Settings(random, laneSequence);
    }
}
