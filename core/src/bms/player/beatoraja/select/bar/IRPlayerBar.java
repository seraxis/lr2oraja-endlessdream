package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.ir.IRScoreData;

public class IRPlayerBar extends SelectableBar {
	private final String title;
	private final int lamp;
	private final boolean isLocalScore;

	public IRPlayerBar(int rank, IRScoreData irScoreData, boolean isLocalScore) {
		super();
		setScore(irScoreData.convertToScoreData());
		this.title = rank + ". " + irScoreData.player;
		this.lamp = irScoreData.clear.id;
		this.isLocalScore = isLocalScore;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getLamp(boolean isPlayer) {
		return this.lamp;
	}

	public boolean isLocalScore() {
		return isLocalScore;
	}

	/**
	 * Convert some ir scores to bars
	 *
	 * @param irScoreData ir scores, should be ordered by exscore. More specifically, the score has larger exscore
	 *                    should be positioned before a smaller one
	 * @return bars
	 */
	public static IRPlayerBar[] fromIRScoreData(IRScoreData[] irScoreData) {
		IRPlayerBar[] bars = new IRPlayerBar[irScoreData.length];
		for (int i = 0; i < irScoreData.length; i++) {
			bars[i] = new IRPlayerBar(i + 1, irScoreData[i], false);
		}
		return bars;
	}

	/**
	 * Convert some ir scores to bars and insert the local score into. Used for
	 *
	 * @param localScore  local score, would be inserted into scores
	 * @param irScoreData ir scores, should be ordered by exscore. More specifically, the score has larger exscore
	 *                    should be positioned before a smaller one
	 * @return bars
	 * @implNote This function is the reason why ir scores need to be sorted before the function call, because we need
	 * to insert the local score into it. An alternative idea is marking the local score so we can mix them and sort,
	 * and tell which one is the local score to give it a specific bar type. However, this needs to change the original
	 * class fields to achieve. (But actually, this isn't impossible if we have a good pair type implementation, we can
	 * attach a boolean with score and sort by score's exscore)
	 */
	public static IRPlayerBar[] fromIRScoreData(IRScoreData localScore, IRScoreData[] irScoreData) {
		IRPlayerBar[] bars = new IRPlayerBar[irScoreData.length + 1];
		int id = 0;
		boolean inserted = false;
		if (irScoreData.length == 0 || localScore.getExscore() > irScoreData[0].getExscore()) {
			id++;
			bars[0] = new IRPlayerBar(id, localScore, true);
			inserted = true;
		}
		for (int i = 0; i < irScoreData.length; i++) {
			IRScoreData score = irScoreData[i];
			bars[id] = new IRPlayerBar(id + 1, score, false);
			id++;
			if (!inserted && score.getExscore() > localScore.getExscore() && (i == irScoreData.length - 1 || irScoreData[i + 1].getExscore() <= localScore.getExscore())) {
				bars[id] = new IRPlayerBar(id + 1, localScore, true);
				id++;
				inserted = true;
			}
		}
		if (!inserted) {
			bars[id] = new IRPlayerBar(id + 1, localScore, true);
		}
		return bars;
	}
}
