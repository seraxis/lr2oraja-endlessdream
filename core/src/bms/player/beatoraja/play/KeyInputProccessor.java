package bms.player.beatoraja.play;

import static bms.player.beatoraja.skin.SkinProperty.*;

import bms.player.beatoraja.modmenu.RandomTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.MathUtils;

import bms.model.BMSModel;
import bms.model.TimeLine;
import bms.player.beatoraja.BMSPlayerMode;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;
import bms.player.beatoraja.input.KeyInputLog;
import bms.player.beatoraja.skin.SkinPropertyMapper;

/**
 * キー入力処理用スレッド
 *
 * @author exch
 */
class KeyInputProccessor {
	private static final Logger logger = LoggerFactory.getLogger(KeyInputProccessor.class);

	private final BMSPlayer player;

	private JudgeThread judge;

	private long prevtime = -1;
	private float[] scratch;
	private int[] scratchKey;
	private float[] scratchTTGraphicSpeed;

	private final LaneProperty laneProperty;

	//キービーム判定同期用
	private boolean isJudgeStarted = false;

	//キービーム停止用
	private boolean keyBeamStop = false;

	public KeyInputProccessor(BMSPlayer player, LaneProperty laneProperty) {
		this.player = player;
		this.laneProperty = laneProperty;
		this.scratch = new float[laneProperty.getScratchKeyAssign().length];
		this.scratchKey = new int[laneProperty.getScratchKeyAssign().length];
		this.scratchTTGraphicSpeed = new float[laneProperty.getScratchKeyAssign().length];
	}

	public void startJudge(BMSModel model, KeyInputLog[] keylog, long milliMarginTime) {
		judge = new JudgeThread(model.getAllTimeLines(), keylog, milliMarginTime);
		judge.start();
		isJudgeStarted = true;
	}

	public void input() {
		final MainController main = player.main;
		final long now = player.timer.getNowTime();
		final BMSPlayerInputProcessor input = main.getInputProcessor();
		final long[] auto_presstime = player.getJudgeManager().getAutoPresstime();

		final int[] laneoffset = laneProperty.getLaneSkinOffset();
		for (int lane = 0; lane < laneoffset.length; lane++) {
			// キービームフラグON/OFF
			final int offset = laneoffset[lane];
			boolean pressed = false;
			boolean scratch = false;
			if(!keyBeamStop) {
				for (int key : laneProperty.getLaneKeyAssign()[lane]) {
					if (input.getKeyState(key) || auto_presstime[key] != Long.MIN_VALUE) {
						pressed = true;
						if(laneProperty.getLaneScratchAssign()[lane] != -1
								&& scratchKey[laneProperty.getLaneScratchAssign()[lane]] != key) {
							scratch = true;
							scratchKey[laneProperty.getLaneScratchAssign()[lane]] = key;
						}
					}
				}
			}
			final int timerOn = SkinPropertyMapper.keyOnTimerId(laneProperty.getLanePlayer()[lane], offset);
			final int timerOff = SkinPropertyMapper.keyOffTimerId(laneProperty.getLanePlayer()[lane], offset);
			if (pressed) {
				if(!isJudgeStarted || player.resource.getPlayMode().mode == BMSPlayerMode.Mode.AUTOPLAY) {
					if (!player.timer.isTimerOn(timerOn) || scratch) {
						player.timer.setTimerOn(timerOn);
						player.timer.setTimerOff(timerOff);
					}
				}
			} else {
				if (player.timer.isTimerOn(timerOn)) {
					player.timer.setTimerOn(timerOff);
					player.timer.setTimerOff(timerOn);
				}
			}
		}

		if(prevtime >= 0) {
			final float deltatime = (now - prevtime) / 1000.0f;
			for (int s = 0; s < scratch.length; s++) {
				final int key0 = laneProperty.getScratchKeyAssign()[s][1];
				final int key1 = laneProperty.getScratchKeyAssign()[s][0];

				float targetSpeed = 1.0f;
				float moveTowardsSpeed = 4.0f;

				if (player.resource.getPlayMode().mode != BMSPlayerMode.Mode.AUTOPLAY) {
					if (input.getKeyState(key0) || auto_presstime[key0] != Long.MIN_VALUE) {
						targetSpeed = -0.75f;
						moveTowardsSpeed = 16.0f;
						scratchTTGraphicSpeed[s] = Math.min(scratchTTGraphicSpeed[s], 0);
					} else if (input.getKeyState(key1) || auto_presstime[key1] != Long.MIN_VALUE) {
						targetSpeed = 2.0f;
						moveTowardsSpeed = 16.0f;
						scratchTTGraphicSpeed[s] = Math.max(scratchTTGraphicSpeed[s], 0);
					}
				}

				// Move towards
				if (Math.abs(1.0f - scratchTTGraphicSpeed[s]) <= deltatime)
					scratchTTGraphicSpeed[s] = targetSpeed;
				else
					scratchTTGraphicSpeed[s] += Math.signum(targetSpeed - scratchTTGraphicSpeed[s]) * deltatime * moveTowardsSpeed;

				// Apply TT speed
				if (scratchTTGraphicSpeed[s] > 0.0f)
					scratch[s] += 360.0f - scratchTTGraphicSpeed[s] * deltatime * 270.0f;
				else if (scratchTTGraphicSpeed[s] < 0.0f)
					scratch[s] += -scratchTTGraphicSpeed[s] * deltatime * 270.0f;

				scratch[s] %= 360.0f;

				main.getOffset(OFFSET_SCRATCHANGLE_1P + s).r = scratch[s];
			}
		}
		prevtime = now;
	}

	// キービームフラグON 判定同期用
	public void inputKeyOn(int lane) {
		final int offset = laneProperty.getLaneSkinOffset()[lane];
		if(!keyBeamStop) {
			final int timerOn = SkinPropertyMapper.keyOnTimerId(laneProperty.getLanePlayer()[lane], offset);
			final int timerOff = SkinPropertyMapper.keyOffTimerId(laneProperty.getLanePlayer()[lane], offset);
			if (!player.timer.isTimerOn(timerOn) || laneProperty.getLaneScratchAssign()[lane] != -1) {
				player.timer.setTimerOn(timerOn);
				player.timer.setTimerOff(timerOff);
			}
		}
	}

	public void stopJudge() {
		if (judge != null) {
			keyBeamStop = true;
			isJudgeStarted = false;
			judge.stop = true;
			judge = null;
		}
	}

	public void setKeyBeamStop(boolean inputStop) {
		this.keyBeamStop = inputStop;
	}

	/**
	 * プレイログからのキー自動入力、判定処理用スレッド
	 */
	class JudgeThread extends Thread {

		// TODO 判定処理スレッドはJudgeManagerに渡した方がいいかも

		private final TimeLine[] timelines;
		private boolean stop = false;
		/**
		 * 自動入力するキー入力ログ
		 */
		private final KeyInputLog[] keylog;
		private final long microMarginTime;

		public JudgeThread(TimeLine[] timelines, KeyInputLog[] keylog, long milliMarginTime) {
			this.timelines = timelines;
			this.keylog = keylog;
			this.microMarginTime = milliMarginTime * 1000;
		}

		@Override
		public void run() {
			int index = 0;

			long frametime = 1;
			final BMSPlayerInputProcessor input = player.main.getInputProcessor();
			final JudgeManager judge = player.getJudgeManager();
			final long lasttime = timelines[timelines.length - 1].getMicroTime() + BMSPlayer.TIME_MARGIN * 1000;

			long prevtime = -1;
			while (!stop) {
//				final long time = player.main.getNowTime(TIMER_PLAY);
				final long mtime = player.timer.getNowMicroTime(TIMER_PLAY);
				if (mtime != prevtime) {
					// リプレイデータ再生
					if (keylog != null) {
						while (index < keylog.length && keylog[index].getTime() + microMarginTime <= mtime) {
							final KeyInputLog key = keylog[index];
							// if(input.getKeystate()[key.keycode] ==
							// key.pressed) {
							// System.out.println("押し離しが行われていません : key - " +
							// key.keycode + " pressed - " + key.pressed +
							// " time - " + key.time);
							// }
							input.setKeyState(key.getKeycode(), key.isPressed(), key.getTime() + microMarginTime);
							index++;
						}
					}

					judge.update(mtime);

					if (prevtime != -1) {
						final long nowtime = mtime - prevtime;
						frametime = nowtime < frametime ? frametime : nowtime;
					}

					prevtime = mtime;
				} else {
					try {
						sleep(0, 500000);
					} catch (InterruptedException e) {
					}
				}

				if (mtime >= lasttime) {
					break;
				}
			}

			if (keylog != null) {
				input.resetAllKeyState();
			}

			logger.info("入力パフォーマンス(max ms) : {}", frametime);
		}
	}
}
