package bms.player.beatoraja.ir;

public class LeaderboardEntry {
    private final IRScoreData irScore;
    private final IRType type;
    private long LR2Id;

    private LeaderboardEntry(IRScoreData irScore, IRType type) {
        this.irScore = irScore;
        this.type = type;
    }

    public enum IRType { Primary, LR2 }

    public static LeaderboardEntry newEntryPrimaryIR(IRScoreData irScore) {
        var newEntry = new LeaderboardEntry(irScore, IRType.Primary);
        return newEntry;
    }

    public static LeaderboardEntry newEntryLR2IR(IRScoreData irScore, long LR2Id) {
        var newEntry = new LeaderboardEntry(irScore, IRType.LR2);
        newEntry.LR2Id = LR2Id;
        return newEntry;
    }

    public IRScoreData getIrScore() { return irScore; }

    public boolean isPrimaryIR() { return type == IRType.Primary; }
    public boolean isLR2IR() { return type == IRType.LR2; }

    public long getLR2Id() { return LR2Id; }
}
