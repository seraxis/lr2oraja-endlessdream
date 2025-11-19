package bms.player.beatoraja.obs;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.MainState.MainStateType;
import bms.player.beatoraja.MainStateListener;
import bms.player.beatoraja.launcher.ObsConfigurationView;

public class ObsListener implements MainStateListener {
	private static final Logger logger = LoggerFactory.getLogger(ObsListener.class);

	private final Config config;
	private final ObsWsClient obsClient;

	private MainStateType lastStateType;

	private volatile ScheduledFuture<?> scheduledStopTask;

	public ObsListener(Config config) {
		this.config = config;
		ObsWsClient client = null;
		try {
			client = new ObsWsClient(config);
			client.connectAsync();
		} catch (Exception e) {
			logger.warn("Failed to initialize OBS client: {}", e.getMessage());
		}
		this.obsClient = client;
	}

	public ObsWsClient getObsClient() {
		return obsClient;
	}

	private void triggerReplay() {
		if (obsClient == null || !obsClient.isConnected()) {
			return;
		}
		if (obsClient.isRecording()) {
			obsClient.restartRecording();
		}
		triggerStateChange(MainStateType.MUSICSELECT);
		obsClient.scheduler.schedule(() -> triggerStateChange(MainStateType.PLAY), 1000, TimeUnit.MILLISECONDS);
	}

	private synchronized boolean cancelScheduledStop() {
		ScheduledFuture<?> task = scheduledStopTask;
		if (task != null && !task.isDone()) {
			boolean cancelled = task.cancel(true);
			scheduledStopTask = null;
			return cancelled;
		}
		return false;
	}

	public synchronized void triggerPlayEnded() {
		triggerStateChange("PLAY_ENDED");
	}

	public synchronized void triggerStateChange(MainStateType stateType) {
		triggerStateChange(stateType.name());
	}

	public synchronized void triggerStateChange(String stateName) {
		if (obsClient == null || !obsClient.isConnected()) {
			return;
		}

		final String scene = config.getObsScene(stateName);
		final String action = config.getObsAction(stateName);

		// If a StopRecord action was already scheduled, StopRecord immediately
		boolean stopRecordNow = cancelScheduledStop();
		if (stopRecordNow) {
			try {
				obsClient.requestStopRecord();
			} catch (Exception e) {
				logger.warn("Failed to send early StopRecord: {}", e.getMessage());
			}
		}

		try {
			if (scene != null && !scene.equals(ObsConfigurationView.SCENE_NONE)) {
				obsClient.setScene(scene);
			}
			if (action != null && !action.equals(ObsConfigurationView.ACTION_NONE)) {
				if (action.equals("StopRecord")) {
					int delay = config.getObsWsRecStopWait();
					// We already executed StopRecord above
					if (stopRecordNow) {
						return;
					}
					scheduledStopTask = obsClient.scheduler.schedule(() -> {
						try {
							obsClient.requestStopRecord();
						} catch (Exception e) {
							logger.warn("Failed to stop recording: {}", e.getMessage());
						} finally {
							synchronized (ObsListener.this) {
								scheduledStopTask = null;
							}
						}
					}, delay, TimeUnit.MILLISECONDS);
				} else {
					obsClient.sendRequest(action);
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to send OBS request: {}", e.getMessage());
		}
	}

	@Override
	public void update(MainState currentState, int status) {
		if (obsClient == null) {
			return;
		}

		final MainStateType currentStateType = MainController.getStateType(currentState);
		if (currentStateType == null) {
			return;
		}

		if (currentStateType == MainStateType.PLAY && lastStateType == MainStateType.PLAY) {
			triggerReplay();
		} else if (currentStateType != lastStateType) {
			triggerStateChange(currentStateType);
		}

		lastStateType = currentStateType;
	}

	public void close() {
		synchronized (this) {
			ScheduledFuture<?> task = scheduledStopTask;
			if (task != null && !task.isDone()) {
				task.cancel(false);
				scheduledStopTask = null;
			}
		}
		if (obsClient != null) {
			obsClient.close();
		}
	}
}
