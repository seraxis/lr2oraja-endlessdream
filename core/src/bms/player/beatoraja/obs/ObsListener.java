package bms.player.beatoraja.obs;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.MainState.MainStateType;
import bms.player.beatoraja.MainStateListener;
import bms.player.beatoraja.launcher.ObsConfigurationView;

public class ObsListener implements MainStateListener {

	private final Config config;
	private final ObsWsClient obsClient;

	private MainStateType lastStateType;
	private ScheduledFuture<?> scheduledStopTask;

	private Boolean instantStopRecord = false;

	public ObsListener(Config config) {
		this.config = config;
		ObsWsClient client = null;
		try {
			client = new ObsWsClient(config);
			client.connectAsync();
		} catch (Exception e) {
			Logger.getGlobal().warning("Failed to initialize OBS client: " + e.getMessage());
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

	private synchronized Boolean cancelScheduledStop() {
		if (scheduledStopTask != null && !scheduledStopTask.isDone()) {
			scheduledStopTask.cancel(false);
			scheduledStopTask = null;
			return true;
		}
		return false;
	}

	public synchronized void triggerStateChange(MainStateType stateType) {
		if (obsClient == null || !obsClient.isConnected()) {
			return;
		}

		final String scene = config.getObsScene(stateType.name());
		final String action = config.getObsAction(stateType.name());

		if (cancelScheduledStop()) {
			instantStopRecord = true;
			try {
				obsClient.requestStopRecord();
			} catch (Exception e) {
				Logger.getGlobal().warning("Failed to send early StopRecord: " + e.getMessage());
			}
		}
		try {
			if (scene != null && !scene.isEmpty() && !scene.equals(ObsConfigurationView.SCENE_NONE)) {
				obsClient.setScene(scene);
			}
			if (action != null && !action.isEmpty() && !action.equals(ObsConfigurationView.ACTION_NONE)) {
				if (action.equals("StopRecord")) {
					int delay = config.getObsWsRecStopWait();
					if (instantStopRecord) {
						instantStopRecord = false;
						delay = 0;
					}
					scheduledStopTask = obsClient.scheduler.schedule(
							() -> {
								obsClient.requestStopRecord();
								scheduledStopTask = null;
							},
							delay,
							TimeUnit.MILLISECONDS);
				} else {
					obsClient.sendRequest(action);
				}
			}
		} catch (Exception e) {
			Logger.getGlobal().warning("Failed to send OBS request: " + e.getMessage());
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
		if (obsClient != null) {
			obsClient.close();
		}
	}
}
