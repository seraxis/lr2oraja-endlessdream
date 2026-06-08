package bms.player.beatoraja.launcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainState.MainStateType;
import bms.player.beatoraja.obs.ObsControlCommand;
import bms.player.beatoraja.obs.ObsWsClient;
import bms.player.beatoraja.obs.ObsWsClient.ObsVersionInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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
	private ComboBox<String> obsActionBox;
	@FXML
	private ComboBox<String> obsTimingBox;
	@FXML
	private ComboBox<String> obsTargetSceneBox;
	@FXML
	private ComboBox<String> obsTargetSourceBox;
	@FXML
	private ComboBox<String> obsTransitionSceneBox;
	@FXML
	private Button obsAddCommandButton;
	@FXML
	private Button obsRemoveCommandButton;
	@FXML
	private TableView<ObsControlCommand> obsCommandTable;
	@FXML
	private TableColumn<ObsControlCommand, String> obsTimingColumn;
	@FXML
	private TableColumn<ObsControlCommand, String> obsActionColumn;
	@FXML
	private TableColumn<ObsControlCommand, String> obsTargetColumn;
	@FXML
	private TableColumn<ObsControlCommand, String> obsDetailColumn;

	private Config config;
	private String status;
	private ObsWsClient obsCfgClient;
	private final ObservableList<ObsControlCommand> commands = FXCollections.observableArrayList();
	private final List<String> scenes = new ArrayList<>();
	private final Map<String, List<String>> sceneSources = new HashMap<>();

	public static final String SCENE_NONE = "(No Change)";
	public static final String ACTION_NONE = "(Do Nothing)";

	public static final String TIMING_MAIN = "MAIN";
	public static final String TIMING_SUFFIX_START = "_START";
	public static final String TIMING_SUFFIX_END = "_END";
	public static final String ACTION_START_RECORD_LABEL = "Start Recording";
	public static final String ACTION_STOP_RECORD_LABEL = "Stop Recording";
	public static final String ACTION_SHOW_SOURCE_LABEL = "Show Source";
	public static final String ACTION_HIDE_SOURCE_LABEL = "Hide Source";
	public static final String ACTION_SET_SCENE_LABEL = "Switch Scene";

	private static final Map<String, String> ACTION_LABELS = new LinkedHashMap<>();

	static {
		ACTION_LABELS.put(ACTION_START_RECORD_LABEL, ObsWsClient.ACTION_START_RECORD);
		ACTION_LABELS.put(ACTION_STOP_RECORD_LABEL, ObsWsClient.ACTION_STOP_RECORD);
		ACTION_LABELS.put(ACTION_SHOW_SOURCE_LABEL, ObsWsClient.ACTION_SHOW_SOURCE);
		ACTION_LABELS.put(ACTION_HIDE_SOURCE_LABEL, ObsWsClient.ACTION_HIDE_SOURCE);
		ACTION_LABELS.put(ACTION_SET_SCENE_LABEL, ObsWsClient.ACTION_SET_SCENE);
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		obsWsRecMode.getItems().addAll(resources.getString("OBSWS_REC_DEFAULT"),
				resources.getString("OBSWS_REC_ONSCREENSHOT"),
				resources.getString("OBSWS_REC_ONREPLAY"));

		obsActionBox.getItems().addAll(ACTION_LABELS.keySet());
		obsTimingBox.getItems().add(TIMING_MAIN + TIMING_SUFFIX_START);
		obsTimingBox.getItems().add(TIMING_MAIN + TIMING_SUFFIX_END);
		for (final MainStateType state : MainStateType.values()) {
			obsTimingBox.getItems().add(state.name() + TIMING_SUFFIX_START);
			obsTimingBox.getItems().add(state.name() + TIMING_SUFFIX_END);
		}

		obsTimingColumn.setCellValueFactory(new PropertyValueFactory<>("timing"));
		obsActionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
		obsTargetColumn.setCellValueFactory(new PropertyValueFactory<>("targetDisplay"));
		obsDetailColumn.setCellValueFactory(new PropertyValueFactory<>("detailDisplay"));
		obsCommandTable.setItems(commands);

		obsActionBox.valueProperty().addListener((observable, oldValue, newValue) -> updateCommandInputState());
		obsTargetSceneBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshSourceItems(newValue));
		obsCommandTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
				obsRemoveCommandButton.setDisable(newValue == null));

		obsRemoveCommandButton.setDisable(true);
	}

	public void init(final PlayConfigurationView main) {
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
		loadSavedCommands();
		resetCommandInputs();
	}

	private void loadSavedCommands() {
		commands.clear();
		commands.addAll(config.getObsCommands());
		if (!commands.isEmpty()) {
			return;
		}

		for (final MainStateType state : MainStateType.values()) {
			addLegacyCommands(state.name());
		}
		addLegacyCommands("PLAY_ENDED");
	}

	private void addLegacyCommands(final String oldStateName) {
		final String timing = oldStateName.equals("PLAY_ENDED") ? "PLAY" + TIMING_SUFFIX_END
				: oldStateName + TIMING_SUFFIX_START;
		final String scene = config.getObsScene(oldStateName);
		if (scene != null && !scene.isEmpty()) {
			commands.add(new ObsControlCommand(timing, ObsWsClient.ACTION_SET_SCENE, "", "", scene));
		}
		final String action = config.getObsAction(oldStateName);
		if (action != null && !action.isEmpty()) {
			commands.add(new ObsControlCommand(timing, action, "", "", ""));
		}
	}

	public void commit() {
		config.setUseObsWs(obsWsEnabled.isSelected());
		config.setObsWsHost(obsWsHost.getText());
		config.setObsWsPort(obsWsPort.getValue());
		config.setObsWsPass(obsWsPass.getText());
		config.setObsWsRecStopWait(obsWsRecStopWait.getValue());
		config.setObsWsRecMode(obsWsRecMode.getSelectionModel().getSelectedIndex());
		config.setObsCommands(new ArrayList<>(commands));
		closeExistingConnection();
	}

	@FXML
	private void addCommand() {
		final String timing = obsTimingBox.getValue();
		final String action = ACTION_LABELS.get(obsActionBox.getValue());
		if (timing == null || action == null) {
			return;
		}

		final String targetScene = safeValue(obsTargetSceneBox);
		final String targetSource = safeValue(obsTargetSourceBox);
		final String transitionScene = safeValue(obsTransitionSceneBox);
		if (requiresTargetSource(action) && (targetScene.isEmpty() || targetSource.isEmpty())) {
			return;
		}
		if (action.equals(ObsWsClient.ACTION_SET_SCENE) && transitionScene.isEmpty()) {
			return;
		}

		commands.add(new ObsControlCommand(timing, action, targetScene, targetSource, transitionScene));
	}

	@FXML
	private void removeSelectedCommand() {
		final ObsControlCommand selected = obsCommandTable.getSelectionModel().getSelectedItem();
		if (selected != null) {
			commands.remove(selected);
		}
	}

	@FXML
	private void clearCommands() {
		commands.clear();
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
				obsCfgClient.setOnSceneSourcesReceived(this::handleSceneSourcesReceived);
				obsCfgClient.connect();

			} catch (final Exception ex) {
				handleObsError(ex);
			}
		}).start();
	}

	private void resetCommandInputs() {
		if (!obsActionBox.getItems().isEmpty()) {
			obsActionBox.getSelectionModel().selectFirst();
		}
		if (!obsTimingBox.getItems().isEmpty()) {
			obsTimingBox.getSelectionModel().selectFirst();
		}
		updateSceneItems();
		updateCommandInputState();
	}

	private void updateSceneItems() {
		obsTargetSceneBox.getItems().setAll(scenes);
		obsTransitionSceneBox.getItems().setAll(scenes);
		if (!scenes.isEmpty()) {
			obsTargetSceneBox.getSelectionModel().selectFirst();
			obsTransitionSceneBox.getSelectionModel().selectFirst();
		}
		refreshSourceItems(obsTargetSceneBox.getValue());
	}

	private void refreshSourceItems(final String sceneName) {
		final List<String> sources = sceneSources.getOrDefault(sceneName, List.of());
		obsTargetSourceBox.getItems().setAll(sources);
		if (!sources.isEmpty()) {
			obsTargetSourceBox.getSelectionModel().selectFirst();
		}
	}

	private void updateCommandInputState() {
		final String action = ACTION_LABELS.get(obsActionBox.getValue());
		final boolean sourceAction = requiresTargetSource(action);
		final boolean sceneAction = ObsWsClient.ACTION_SET_SCENE.equals(action);
		obsTargetSceneBox.setDisable(!sourceAction);
		obsTargetSourceBox.setDisable(!sourceAction);
		obsTransitionSceneBox.setDisable(!sceneAction);
	}

	private boolean requiresTargetSource(final String action) {
		return ObsWsClient.ACTION_SHOW_SOURCE.equals(action) || ObsWsClient.ACTION_HIDE_SOURCE.equals(action);
	}

	private String safeValue(final ComboBox<String> comboBox) {
		return comboBox.getValue() != null ? comboBox.getValue() : "";
	}

	private void closeExistingConnection() {
		if (obsCfgClient != null) {
			obsCfgClient.close();
			obsCfgClient = null;
		}
	}

	private void handleObsError(final Exception ex) {
		setConnectionStatus("connect_fail", "Failed to connect!");
	}

	private void handleObsClose() {
		if ("connecting".equals(status)) {
			setConnectionStatus("auth_fail", "Authentication failed!");
		}
	}

	private void handleVersionReceived(final ObsVersionInfo version) {
		setConnectionStatus("version_received", version.toString());
	}

	private void handleScenesReceived(final List<String> scenes) {
		Platform.runLater(() -> {
			this.scenes.clear();
			this.scenes.addAll(scenes);
			updateSceneItems();
		});
	}

	private void handleSceneSourcesReceived(final Map<String, List<String>> sceneSources) {
		Platform.runLater(() -> {
			this.sceneSources.clear();
			this.sceneSources.putAll(sceneSources);
			refreshSourceItems(obsTargetSceneBox.getValue());
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
