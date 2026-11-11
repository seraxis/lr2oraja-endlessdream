package bms.player.beatoraja.obs;

import java.util.ArrayList;
import java.util.List;
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
	private volatile boolean mainStartTriggered = false;

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

	public void triggerMainStarted() {
		scheduleMainStartTrigger(0);
	}

	private void scheduleMainStartTrigger(final int retryCount) {
		if (obsClient == null) {
			return;
		}
		try {
			obsClient.scheduler.schedule(() -> {
				if (mainStartTriggered) {
					return;
				}
				if (obsClient.isConnected() && obsClient.isIdentified()) {
					mainStartTriggered = true;
					triggerStateChange(ObsConfigurationView.TIMING_MAIN + ObsConfigurationView.TIMING_SUFFIX_START);
				} else if (retryCount < 20) {
					scheduleMainStartTrigger(retryCount + 1);
				}
			}, retryCount == 0 ? 0 : 500, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.warn("Failed to schedule OBS main start trigger: {}", e.getMessage());
		}
	}

	public synchronized void triggerMainEnded() {
		triggerStateChange(ObsConfigurationView.TIMING_MAIN + ObsConfigurationView.TIMING_SUFFIX_END);
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
		triggerStateChange(MainStateType.PLAY.name() + ObsConfigurationView.TIMING_SUFFIX_END);
	}

	public synchronized void triggerStateChange(MainStateType stateType) {
		triggerStateChange(stateType.name() + ObsConfigurationView.TIMING_SUFFIX_START);
	}

	public synchronized void triggerStateChange(String timing) {
		if (obsClient == null || !obsClient.isConnected()) {
			return;
		}

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
			for (ObsControlCommand command : getConfiguredCommands()) {
				if (!timing.equals(command.getTiming())) {
					continue;
				}
				executeCommand(command, stopRecordNow);
				stopRecordNow = false;
			}
		} catch (Exception e) {
			logger.warn("Failed to send OBS request: {}", e.getMessage());
		}
	}

	private List<ObsControlCommand> getConfiguredCommands() {
		if (!config.getObsCommands().isEmpty()) {
			return config.getObsCommands();
		}

		final List<ObsControlCommand> commands = new ArrayList<>();
		for (final MainStateType state : MainStateType.values()) {
			addLegacyCommands(commands, state.name());
		}
		addLegacyCommands(commands, "PLAY_ENDED");
		return commands;
	}

	private void addLegacyCommands(final List<ObsControlCommand> commands, final String oldStateName) {
		final String timing = oldStateName.equals("PLAY_ENDED") ? MainStateType.PLAY.name()
				+ ObsConfigurationView.TIMING_SUFFIX_END : oldStateName + ObsConfigurationView.TIMING_SUFFIX_START;
		final String scene = config.getObsScene(oldStateName);
		if (scene != null && !scene.isEmpty()) {
			commands.add(new ObsControlCommand(timing, ObsWsClient.ACTION_SET_SCENE, "", "", scene));
		}
		final String action = config.getObsAction(oldStateName);
		if (action != null && !action.isEmpty()) {
			commands.add(new ObsControlCommand(timing, action, "", "", ""));
		}
	}

	private void executeCommand(ObsControlCommand command, boolean stopRecordNow) {
		final String action = command.getAction();
		if (ObsWsClient.ACTION_SET_SCENE.equals(action)) {
			if (!command.getTransitionScene().isEmpty()) {
				obsClient.setScene(command.getTransitionScene());
			}
		} else if (ObsWsClient.ACTION_SHOW_SOURCE.equals(action)) {
			obsClient.setSourceVisible(command.getTargetScene(), command.getTargetSource(), true);
		} else if (ObsWsClient.ACTION_HIDE_SOURCE.equals(action)) {
			obsClient.setSourceVisible(command.getTargetScene(), command.getTargetSource(), false);
		} else if (ObsWsClient.ACTION_STOP_RECORD.equals(action)) {
			if (stopRecordNow) {
				return;
			}
			scheduleStopRecord();
		} else if (ObsWsClient.ACTION_START_RECORD.equals(action)) {
			obsClient.requestStartRecord();
		} else if (action != null && !action.isEmpty()) {
			obsClient.sendRequest(action);
		}
	}

	private void scheduleStopRecord() {
		int delay = config.getObsWsRecStopWait();
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
			if (lastStateType != null) {
				triggerStateChange(lastStateType.name() + ObsConfigurationView.TIMING_SUFFIX_END);
			}
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
			if (cancelScheduledStop()) {
				try {
					obsClient.requestStopRecord();
				} catch (Exception e) {
					logger.warn("Failed to stop recording before closing OBS client: {}", e.getMessage());
				}
			}
			obsClient.close();
		}
	}
}
