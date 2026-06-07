package bms.player.beatoraja.launcher;

import bms.player.beatoraja.AudioConfig;
import bms.player.beatoraja.AudioConfig.DriverType;
import bms.player.beatoraja.AudioConfig.FrequencyType;
import bms.player.beatoraja.audio.PortAudioDriver;
import com.portaudio.DeviceInfo;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AudioConfigurationView implements Initializable {
	private static final Logger logger = LoggerFactory.getLogger(AudioConfigurationView.class);

	@FXML
	private ComboBox<DriverType> audio;
	@FXML
	private ComboBox<String> audioname;
	@FXML
	private Spinner<Integer> audiobuffer;
	@FXML
	private Spinner<Integer> audiosim;
	@FXML
	private ComboBox<Integer> audiosamplerate;

	@FXML
	private Slider systemVolumeSlider;
	@FXML
	private Spinner<Double> systemVolumeSpinner;

	@FXML
	private Slider keyVolumeSlider;
	@FXML
	private Spinner<Double> keyVolumeSpinner;

	@FXML
	private Slider bgVolumeSlider;
	@FXML
	private Spinner<Double> bgVolumeSpinner;

	@FXML
	private CheckBox normalizeVolume;
	@FXML
	private ComboBox<FrequencyType> audioFreqOption;
	@FXML
	private ComboBox<FrequencyType> audioFastForward;
	@FXML
	private CheckBox loopResultSound;
	@FXML
	private CheckBox loopCourseResultSound;

	private AudioConfig config;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		audio.getItems().setAll(DriverType.OpenAL , DriverType.PortAudio);
		audiosamplerate.getItems().setAll(null, 44100, 48000);

		audioFreqOption.getItems().setAll(FrequencyType.UNPROCESSED , FrequencyType.FREQUENCY);
		audioFastForward.getItems().setAll(FrequencyType.UNPROCESSED , FrequencyType.FREQUENCY);

		bindSliderToSpinner(systemVolumeSlider, systemVolumeSpinner);
		bindSliderToSpinner(keyVolumeSlider, keyVolumeSpinner);
		bindSliderToSpinner(bgVolumeSlider, bgVolumeSpinner);
	}

	public void update(AudioConfig config) {
		this.config = config;

		audio.setValue(config.getDriver());
		audiobuffer.getValueFactory().setValue(config.getDeviceBufferSize());
		audiosim.getValueFactory().setValue(config.getDeviceSimultaneousSources());
		audiosamplerate.setValue(config.getSampleRate() > 0 ? config.getSampleRate() : null);
		audioFreqOption.setValue(config.getFreqOption());
		audioFastForward.setValue(config.getFastForward());
		systemVolumeSlider.setValue(config.getSystemvolume());
		keyVolumeSlider.setValue(config.getKeyvolume());
		bgVolumeSlider.setValue(config.getBgvolume());
		normalizeVolume.setSelected(config.isNormalizeVolume());
		loopResultSound.setSelected(config.isLoopResultSound());
		loopCourseResultSound.setSelected(config.isLoopCourseResultSound());

		updateAudioDriver();
		updateNormalizeVolume();
	}

	public void commit() {
		config.setDriver(audio.getValue());
		config.setDriverName(audioname.getValue());
		config.setDeviceBufferSize(audiobuffer.getValue());
		config.setDeviceSimultaneousSources(audiosim.getValue());
		config.setSampleRate(audiosamplerate.getValue() != null ? audiosamplerate.getValue() : 0);
		config.setFreqOption(audioFreqOption.getValue());
		config.setFastForward(audioFastForward.getValue());
		config.setSystemvolume((float) systemVolumeSlider.getValue());
		config.setKeyvolume((float) keyVolumeSlider.getValue());
		config.setBgvolume((float) bgVolumeSlider.getValue());
		config.setNormalizeVolume(normalizeVolume.isSelected());
		config.setLoopResultSound(loopResultSound.isSelected());
		config.setLoopCourseResultSound(loopCourseResultSound.isSelected());
	}

	@FXML
	public void updateNormalizeVolume() {
		boolean enabled = normalizeVolume.isSelected();
		keyVolumeSlider.setDisable(enabled);
		keyVolumeSpinner.setDisable(enabled);
		bgVolumeSlider.setDisable(enabled);
		bgVolumeSpinner.setDisable(enabled);
	}

    @FXML
	public void updateAudioDriver() {
		switch(audio.getValue()) {
		case OpenAL:
			audioname.setDisable(true);
			audioname.getItems().clear();
			audiobuffer.setDisable(false);
			audiosim.setDisable(false);
			break;
		case PortAudio:
			try {
				DeviceInfo[] devices = PortAudioDriver.getDevices();
				List<String> drivers = new ArrayList<String>(devices.length);
				for(int i = 0;i < devices.length;i++) {
					drivers.add(devices[i].name);
				}
				if(drivers.size() == 0) {
					throw new RuntimeException("ドライバが見つかりません");
				}
				audioname.getItems().setAll(drivers);
				if(drivers.contains(config.getDriverName())) {
					audioname.setValue(config.getDriverName());
				} else {
					audioname.setValue(drivers.get(0));
				}
				audioname.setDisable(false);
				audiobuffer.setDisable(false);
				audiosim.setDisable(false);
//				PortAudio.terminate();
			} catch(Throwable e) {
				logger.error("PortAudioは選択できません : {}", e.getMessage());
				audio.setValue(DriverType.OpenAL);
			}
			break;
		}
	}

	private void bindSliderToSpinner(Slider slider, Spinner<Double> spinner) {
		Bindings.bindBidirectional(
				slider.valueProperty().asObject(),
				spinner.getValueFactory().valueProperty()
		);
	}
}
