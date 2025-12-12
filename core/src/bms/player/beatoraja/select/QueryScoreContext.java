package bms.player.beatoraja.select;

import bms.player.beatoraja.modmenu.FreqTrainerMenu;
import bms.player.beatoraja.modmenu.JudgeTrainer;

/**
 * Wrapper of parameters when querying the score data
 * @implNote primitive fields are must be provided, boxed fields are not.
 */
public record QueryScoreContext(int lnMode, Integer freqValue, Integer overrideJudge) {
	public boolean isQueryModdedScore() {
		return freqValue != null || overrideJudge != null;
	}

	public static QueryScoreContext create(int lnMode) {
		Integer freqValue = FreqTrainerMenu.isFreqTrainerEnabled() ? FreqTrainerMenu.getFreq() : null;
		Integer overrideJudge = JudgeTrainer.isActive() ? JudgeTrainer.getJudgeRank() : null;
		return new  QueryScoreContext(lnMode, freqValue, overrideJudge);
	}

	@Override
	public int hashCode() {
		// lnMode \in [0, 3), freqValue \in [0, 200), overrideJudge \in [0, 5)
		// So we can simply "concat" them as the hash code
		return freqValue * 100 + lnMode * 10 + overrideJudge;
	}
}
