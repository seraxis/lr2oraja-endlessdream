package bms.player.beatoraja.play;

import static bms.model.DecodeLog.State.WARNING;
import static bms.player.beatoraja.skin.SkinProperty.TIMER_PLAY;

import bms.model.DecodeLog;
import com.badlogic.gdx.utils.Array;

import bms.model.BMSModel;
import bms.model.Note;
import bms.model.TimeLine;
import bms.player.beatoraja.Config;
import bms.player.beatoraja.audio.AudioDriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * キー音処理用クラス
 * 
 * @author exch
 */
public class KeySoundProcessor {

	private final BMSPlayer player;
	
	private final AudioDriver audio;
	/**
	 * BGレーン再生用スレッド
	 */
	private AutoplayThread autoThread;
	
	public KeySoundProcessor(BMSPlayer player) {
		this.player = player;
		audio = player.main.getAudioProcessor();
	}

	public void startBGPlay(BMSModel model, long starttime) {
		autoThread = new AutoplayThread(model, starttime);
		autoThread.start();
	}
	
	public void stopBGPlay() {
		if (autoThread != null) {
			autoThread.stop = true;
		}
	}
	
	/**
	 * BGレーン再生用スレッド
	 *
	 * @author exch
	 */
	class AutoplayThread extends Thread {

		private boolean stop = false;

		private final long starttime;
		final TimeLine[] timelines;

		public AutoplayThread(BMSModel model, long starttime) {
			this.starttime = starttime;
			Array<TimeLine> tls = new Array<TimeLine>();
			for(TimeLine tl : model.getAllTimeLines()) {
				if(tl.getBackGroundNotes().length > 0) {
					tls.add(tl);
				}
			}
			timelines = tls.toArray(TimeLine.class);
		}

		@Override
		public void run() {
			final long lasttime = timelines.length > 0 ?
					timelines[timelines.length - 1].getMicroTime() : 0;
			final Config config = player.resource.getConfig();
			int p = 0;
			for (long time = starttime; p < timelines.length && timelines[p].getMicroTime() < time; p++)
				;

			while (!stop) {
				final long time = player.timer.getNowMicroTime(TIMER_PLAY);
				float volume = player.getAdjustedVolume();
				if (volume < 0) {
					volume = config.getAudioConfig().getBgvolume();
				}
				// BGレーン再生
				while (p < timelines.length && timelines[p].getMicroTime() <= time) {
					for (Note n : timelines[p].getBackGroundNotes()) {
						audio.play(n, volume, 0);
					}
					p++;
				}
				if (p < timelines.length) {
					try {
						final long sleeptime = timelines[p].getMicroTime() - time;
						if (sleeptime > 0) {
							sleep(sleeptime / 1000);
						}
					} catch (InterruptedException e) {
					}
				}
				if (time >= lasttime) {
					break;
				}
			}
		}
	}
}
