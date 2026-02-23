package bms.player.beatoraja.pattern;

import bms.model.*;
import bms.player.beatoraja.PlayerConfig;

/**
 * ロングノーツを除去/追加する譜面オプション
 *
 * @author exch
 */
public class LongNoteModifier extends PatternModifier {

	private Mode mode = Mode.REMOVE;

	private double rate = 1.0;

	public LongNoteModifier() {
	}

	public LongNoteModifier(int mode, double rate) {
		this.mode = Mode.values()[mode];
		this.rate = rate;
	}

	@Override
	public void modify(BMSModel model) {

		if(mode == Mode.REMOVE) {
			AssistLevel assist = AssistLevel.NONE;
			for (Timeline tl : model.getAllTimelines()) {
				for(int lane = 0;lane < model.getMode().key;lane++) {
					if(tl.getNote(lane) instanceof LongNote ln && Math.random() < rate) {
						tl.setNote(lane, ln.isEnd() ? null : new NormalNote(ln.getWav()));
						assist = AssistLevel.ASSIST;
					}
				}
			}
			setAssistLevel(assist);
		} else {
			int r = 0;

			AssistLevel assist = AssistLevel.NONE;

			Timeline[] tls = model.getAllTimelines();
			for (int i = 0;i < tls.length - 1;i++) {
				for(int lane = 0;lane < model.getMode().key;lane++) {
					if(tls[i].getNote(lane) instanceof NormalNote && !tls[i + 1].existNote(lane) && Math.random() < rate) {
						LongNoteDef lnType = switch(mode) {
							case ADD_LN -> LongNoteDef.LONG_NOTE;
							case ADD_CN -> LongNoteDef.CHARGE_NOTE;
							case ADD_HCN -> LongNoteDef.HELL_CHARGE_NOTE;
//							case ADD_ALL -> (int) (Math.random() * 3 + 1); // TODO: Fix this
							default -> LongNoteDef.UNDEFINED;
						};

						if(lnType != LongNoteDef.LONG_NOTE) {
							assist = AssistLevel.ASSIST;
						}

						LongNote lnstart = new LongNote(tls[i].getNote(lane).getWav(),tls[i].getNote(lane).getMicroStart(),tls[i].getNote(lane).getDuration());
						lnstart.setType(lnType);
						LongNote lnend = new LongNote(-2, lnType);

						tls[i].setNote(lane, lnstart);
						tls[i + 1].setNote(lane, lnend);
						lnstart.connectPair(lnend);
					}
				}
			}
			setAssistLevel(assist);
		}
	}

	public enum Mode {
		REMOVE, ADD_LN, ADD_CN, ADD_HCN, ADD_ALL;
	}

}
