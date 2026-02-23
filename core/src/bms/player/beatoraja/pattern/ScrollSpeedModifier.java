package bms.player.beatoraja.pattern;

import bms.model.BMSModel;
import bms.model.Timeline;
import bms.player.beatoraja.PlayerConfig;

/**
 * スクロールスピード変更に関するオプション
 *
 * @author exch
 */
public class ScrollSpeedModifier extends PatternModifier {

    private Mode mode = Mode.REMOVE;

    /**
     * スクロールを変更する小節単位
     */
    private int section = 4;

    /**
     * 変更するスクロール幅
     */
    private double rate = 0.5;
    
    public ScrollSpeedModifier() {
    }

    public ScrollSpeedModifier(int mode, int section, double scrollrate) {
        this.mode = Mode.values()[mode];
        this.section = section;
        this.rate = scrollrate;
    }

    @Override
    public void modify(BMSModel model) {
        if(mode == Mode.REMOVE) {
            // スクロールスピード変更、ストップシーケンス無効化
            AssistLevel assist = AssistLevel.NONE;
            Timeline starttl = model.getAllTimelines()[0];

            for (Timeline tl : model.getAllTimelines()) {
                if(tl.getBpm() != starttl.getBpm() || tl.getScroll() != starttl.getScroll() || tl.getStop() != 0) {
                    assist = AssistLevel.LIGHT_ASSIST;
                }
                tl.setSection(starttl.getBpm() * tl.getMicroTime() / 240000000);
                tl.setMicroStop(0);
                tl.setBpm(starttl.getBpm());
                tl.setScroll(starttl.getScroll());
            }
            setAssistLevel(assist);
        } else {
            final double base = model.getAllTimelines()[0].getScroll();
            double current = base;
            int sectioncount = 0;
            for (Timeline tl : model.getAllTimelines()) {
                if(tl.getHasSectionLine()) {
                	sectioncount++;
                	if(section == sectioncount) {
                        current = base * (1.0 + Math.random() * rate * 2 - rate);
                        sectioncount = 0;
                	}
                }
                tl.setScroll(current);
            }
        }
    }

    public enum Mode {
        REMOVE, ADD;
    }
}
