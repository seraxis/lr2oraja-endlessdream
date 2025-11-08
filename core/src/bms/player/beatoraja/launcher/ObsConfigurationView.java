package bms.player.beatoraja.launcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainState.MainStateType;
import bms.player.beatoraja.obs.ObsWsClient;
import bms.player.beatoraja.obs.ObsWsClient.ObsVersionInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ObsConfigurationView implements Initializable {
	@FXML
	private CheckBox obsWsEnabled;
	@FXML
	private TextField obsWsHost;
	@FXML
	private Spinner<Integer> obsWsPort;
	@FXML
	private PasswordField obsWsPass;
	@FXML
	private Label obsWsConnectLabel;
	@FXML
	private ComboBox<String> obsWsRecMode;
	@FXML
	private Spinner<Integer> obsWsRecStopWait;
	@FXML
	private VBox listContainer;

	private Config config;
	private String status;
	private ObsWsClient obsCfgClient;

	private final List<String> states = new ArrayList<>();
	private final Map<String, ComboBox<String>> sceneBoxes = new HashMap<>();
	private final Map<String, ComboBox<String>> actionBoxes = new HashMap<>();

	public static final String SCENE_NONE = "(No Change)";
	public static final String ACTION_NONE = "(Do Nothing)";

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		obsWsRecMode.getItems().addAll(resources.getString("OBSWS_REC_DEFAULT"),
				resources.getString("OBSWS_REC_ONSCREENSHOT"),
				resources.getString("OBSWS_REC_ONREPLAY"));
	}

	public void init(final PlayConfigurationView main) {
		for (final MainStateType state : MainStateType.values()) {
			states.add(state.name());
			createStateRow(state.name());
			if (state.name().equals("PLAY")) {
				states.add("PLAY_ENDED");
				createStateRow("PLAY_ENDED");
			}
		}
	}

	private void createStateRow(final String stateName) {
		final HBox row = new HBox(10);

		final Label label = new Label(stateName);
		label.setMinWidth(100);

		final ComboBox<String> sceneBox = new ComboBox<>();
		sceneBox.setDisable(true);
		sceneBox.setMinWidth(150);
		sceneBox.getItems().add(SCENE_NONE);
		sceneBoxes.put(stateName, sceneBox);

		final ComboBox<String> actionBox = new ComboBox<>();
		actionBox.setMinWidth(150);
		actionBoxes.put(stateName, actionBox);

		row.getChildren().addAll(label, sceneBox, actionBox);
		listContainer.getChildren().add(row);
	}

	public void update(final Config config) {
		this.config = config;

		obsWsEnabled.setSelected(config.isUseObsWs());
		obsWsHost.setText(config.getObsWsHost());
		obsWsPort.getValueFactory().setValue(config.getObsWsPort());
		obsWsPass.setText(config.getObsWsPass());
		obsWsRecStopWait.getValueFactory().setValue(config.getObsWsRecStopWait());
		obsWsRecMode.getSelectionModel().select(config.getObsWsRecMode());
		resetConnectionStatus();

		loadSavedSelections();
	}

	private void loadSavedSelections() {
		for (final String state : states) {

			final ComboBox<String> sceneBox = sceneBoxes.get(state);
			if (sceneBox != null) {
				final String savedScene = config.getObsScene(state);
				if (savedScene != null && !savedScene.isEmpty()) {
					sceneBox.setValue(savedScene);
				} else {
					sceneBox.setValue(SCENE_NONE);
				}
			}

			final ComboBox<String> actionBox = actionBoxes.get(state);
			if (actionBox != null) {
				final String savedAction = config.getObsAction(state);
				final String savedActionLabel = ObsWsClient.getActionLabel(savedAction);
				if (savedActionLabel != null && !savedActionLabel.isEmpty()) {
					actionBox.setValue(savedActionLabel);
				} else {
					actionBox.setValue(ACTION_NONE);
				}
			}
		}
	}

	public void commit() {
		config.setUseObsWs(obsWsEnabled.isSelected());
		config.setObsWsHost(obsWsHost.getText());
		config.setObsWsPort(obsWsPort.getValue());
		config.setObsWsPass(obsWsPass.getText());
		config.setObsWsRecStopWait(obsWsRecStopWait.getValue());
		config.setObsWsRecMode(obsWsRecMode.getSelectionModel().getSelectedIndex());

		saveSelections();
	}

	private void saveSelections() {
		if (obsCfgClient == null || !obsCfgClient.isConnected())
			return;

		for (final String state : states) {
			final ComboBox<String> sceneBox = sceneBoxes.get(state);
			if (sceneBox != null) {
				final String value = sceneBox.getValue();
				if (value == null || value.equals(SCENE_NONE)) {
					config.setObsScene(state, "");
				} else {
					config.setObsScene(state, value);
				}
			}

			final ComboBox<String> actionBox = actionBoxes.get(state);
			if (actionBox != null) {
				final String value = actionBox.getValue();
				if (value == null || value.equals(ACTION_NONE)) {
					config.setObsAction(state, "");
				} else {
					final String req = ObsWsClient.OBS_ACTIONS.get(value);
					if (req != null)
						config.setObsAction(state, req);
				}
			}
		}

		closeExistingConnection();
	}

	@FXML
	private void connect() {
		setConnectionStatus("connecting", "Connecting...");

		new Thread(() -> {
			try {
				closeExistingConnection();

				final Config tempConfig = new Config();
				tempConfig.setObsWsHost(obsWsHost.getText());
				tempConfig.setObsWsPort(obsWsPort.getValue());
				tempConfig.setObsWsPass(obsWsPass.getText());

				obsCfgClient = new ObsWsClient(tempConfig);
				obsCfgClient.setAutoReconnect(false);
				obsCfgClient.setOnError(this::handleObsError);
				obsCfgClient.setOnClose(this::handleObsClose);
				obsCfgClient.setOnVersionReceived(this::handleVersionReceived);
				obsCfgClient.setOnScenesReceived(this::handleScenesReceived);
				obsCfgClient.connect();

			} catch (final Exception ex) {
				handleObsError(ex);
			}
		}).start();
	}

	private void closeExistingConnection() {
		if (obsCfgClient != null && obsCfgClient.isConnected()) {
			obsCfgClient.close();
			obsCfgClient = null;
		}
	}

	private void handleObsError(final Exception ex) {
		setConnectionStatus("connect_fail", "Failed to connect!");
	}

	private void handleObsClose() {
		if (status.equals("connecting")) {
			setConnectionStatus("auth_fail", "Authentication failed!");
		}
	}

	private void handleVersionReceived(final ObsVersionInfo version) {
		setConnectionStatus("version_received", version.toString());
	}

	private void handleScenesReceived(final List<String> scenes) {
		Platform.runLater(() -> {
			for (final Map.Entry<String, ComboBox<String>> entry : sceneBoxes.entrySet()) {
				final ComboBox<String> sceneBox = entry.getValue();

				final String previousValue = sceneBox.getValue();
				final String savedScene = config.getObsScene(entry.getKey());

				sceneBox.getItems().setAll(SCENE_NONE);
				sceneBox.getItems().addAll(scenes);
				sceneBox.setDisable(false);

				if (savedScene != null && !savedScene.isEmpty() && scenes.contains(savedScene)) {
					sceneBox.setValue(savedScene);
				} else if (previousValue != null && scenes.contains(previousValue)) {
					sceneBox.setValue(previousValue);
				} else {
					sceneBox.setValue(SCENE_NONE);
				}
			}

			for (final Map.Entry<String, ComboBox<String>> entry : actionBoxes.entrySet()) {
				final ComboBox<String> actionBox = entry.getValue();

				final String previousValue = actionBox.getValue();
				final String savedActionLabel = ObsWsClient.getActionLabel(config.getObsAction(entry.getKey()));

				actionBox.getItems().setAll(ACTION_NONE);
				actionBox.getItems().addAll(ObsWsClient.OBS_ACTIONS.keySet());

				if (savedActionLabel != null && !savedActionLabel.isEmpty() &&
						ObsWsClient.OBS_ACTIONS.keySet().contains(savedActionLabel)) {
					actionBox.setValue(savedActionLabel);
				} else if (previousValue != null && ObsWsClient.OBS_ACTIONS.keySet().contains(previousValue)) {
					actionBox.setValue(previousValue);
				} else {
					actionBox.setValue(ACTION_NONE);
				}
			}
		});
	}

	private void setConnectionStatus(final String status, final String labelText) {
		this.status = status;
		Platform.runLater(() -> obsWsConnectLabel.setText(labelText));
	}

	private void resetConnectionStatus() {
		obsWsConnectLabel.setText("");
	}
}
