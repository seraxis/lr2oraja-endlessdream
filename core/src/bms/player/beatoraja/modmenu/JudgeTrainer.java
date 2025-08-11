package bms.player.beatoraja.modmenu;

import bms.model.Mode;
import bms.player.beatoraja.play.BMSPlayerRule;

public class JudgeTrainer {
    public static final String[] JUDGE_OPTIONS = new String[]{
            "EASY", "NORMAL", "HARD", "VERY_HARD"
    };
    private static boolean active;
    private static int judgeRank = 0;

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        JudgeTrainer.active = active;
    }

    public static int getJudgeRank() {
        return judgeRank;
    }

    public static void setJudgeRank(int judgeRank) {
        JudgeTrainer.judgeRank = judgeRank;
    }

    public static int getJudgeWindowRate(Mode mode) {
        // NOTE: The order of the rule is from VERY-HARD to VERY-EASY:
        // VERY-HARD | HARD | NORMAL | EASY | VERY-EASY
        //     0     |  1   |   2    |  3   |     4
        // However, the order defined here is completely reversed and VERY-EASY is not an option (LR2 doesn't
        // support VERY-EASY and LR2oraja considers it as EASY directly). Therefore, we need a transformation:
        // EASY 0 -> 3 | NORMAL: 1 -> 2 | HARD: 2 -> 1 | VERY-HARD: 3 -> 0
        // We can observe that the sum is always 3
        BMSPlayerRule rule = BMSPlayerRule.getBMSPlayerRule(mode);
        return rule.judge.windowrule.judgerank[3 - judgeRank];
    }
}

