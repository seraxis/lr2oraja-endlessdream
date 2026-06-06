package bms.player.beatoraja.launcher;

import java.net.URL;
import java.util.ResourceBundle;

import bms.player.beatoraja.Config;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;

public class OrajaHelperConfigurationView implements Initializable {
	@FXML
	private CheckBox orajaHelperEnabled;
	@FXML
	private Spinner<Integer> orajaHelperPort;

	private Config config;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void init(PlayConfigurationView main) {
	}

	public void update(Config config) {
		this.config = config;
		orajaHelperEnabled.setSelected(config.isUseOrajaHelper());
		orajaHelperPort.getValueFactory().setValue(config.getOrajaHelperPort());
	}

	public void commit() {
		orajaHelperPort.getValueFactory()
				.setValue(orajaHelperPort.getValueFactory().getConverter().fromString(orajaHelperPort.getEditor().getText()));
		config.setUseOrajaHelper(orajaHelperEnabled.isSelected());
		config.setOrajaHelperPort(orajaHelperPort.getValue());
	}
}
