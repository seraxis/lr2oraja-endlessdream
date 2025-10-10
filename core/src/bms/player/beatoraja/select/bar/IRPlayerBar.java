package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.ir.IRScoreData;

public class IRPlayerBar extends SelectableBar {
	private final String title;
	private final int lamp;

	public IRPlayerBar(int rank, IRScoreData irScoreData) {
		super();
		setScore(irScoreData.convertToScoreData());
		this.title = rank + ". " + irScoreData.player;
		this.lamp = irScoreData.clear.id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getLamp(boolean isPlayer) {
		return this.lamp;
	}

	public static IRPlayerBar[] fromIRScoreData(IRScoreData[] irScoreData) {
		IRPlayerBar[] bars = new IRPlayerBar[irScoreData.length];
		for (int i = 0; i < irScoreData.length; i++) {
			bars[i] = new IRPlayerBar(i + 1, irScoreData[i]);
		}
		return bars;
	}
}
