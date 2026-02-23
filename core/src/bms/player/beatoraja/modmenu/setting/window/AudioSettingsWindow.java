package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.AudioConfig;
import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.widget.*;
import bms.tool.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class AudioSettingsWindow extends TiledOptionBasedWindow {
	public AudioSettingsWindow(Config config, PlayerConfig playerConfig) {
		super("Audio", config, playerConfig);
	}

	private final Supplier<Integer> getMasterVolume = () -> convertPercentToInt(config.getAudioConfig().getSystemvolume());
	private final Supplier<Integer> getKeyVolume = () -> convertPercentToInt(config.getAudioConfig().getKeyvolume());
	private final Supplier<Integer> getBGVolume = () -> convertPercentToInt(config.getAudioConfig().getBgvolume());
	private final SliderWidget masterVolume = new SliderWidget("##Master Volume", (newValue) -> config.getAudioConfig().setSystemvolume(newValue / 100.0F));
	private final SliderWidget keyVolume = new SliderWidget("##Key Volume", (newValue) -> config.getAudioConfig().setSystemvolume(newValue / 100.0F));
	private final SliderWidget bgVolume = new SliderWidget("##BG Volume", (newValue) -> config.getAudioConfig().setSystemvolume(newValue / 100.0F));
	private final CheckboxWidget normalizeVolume = new CheckboxWidget("##Normalize Volume", (newValue) -> config.getAudioConfig().setNormalizeVolume(newValue));

	private final CheckboxWidget loopResultMusic = new CheckboxWidget("##LoopResultMusic", (newValue) -> config.getAudioConfig().setLoopResultSound(newValue));
	private final CheckboxWidget loopCourseResult = new CheckboxWidget("##LoopCourseResult", (newValue) -> config.getAudioConfig().setLoopCourseResultSound(newValue));

	private final EnumComboWidget<AudioConfig.FrequencyType> frequencyOption = new EnumComboWidget<>("##Frequency Option", AudioConfig.FrequencyType.class, (newValue) -> config.getAudioConfig().setFreqOption(newValue));
	private final EnumComboWidget<AudioConfig.FrequencyType> fastForward = new EnumComboWidget<>("##Fast-forward", AudioConfig.FrequencyType.class, (newValue) -> config.getAudioConfig().setFastForward(newValue));

	private final List<Pair<String, List<TiledOption<?>>>> options = Arrays.asList(
			Pair.of("Volume", Arrays.asList(
					new TiledOption<>("Master Volume", getMasterVolume, masterVolume),
					new TiledOption<>("Key Volume", getKeyVolume, keyVolume),
					new TiledOption<>("BG Volume", getBGVolume, bgVolume),
					new TiledOption<>("Normalize Volume", config.getAudioConfig()::isNormalizeVolume, normalizeVolume)
			)),
			Pair.of("Loop", Arrays.asList(
					new TiledOption<>("Looping the sound of the Result", config.getAudioConfig()::isLoopResultSound, loopResultMusic),
					new TiledOption<>("Looping the sound of the Course Result", config.getAudioConfig()::isLoopCourseResultSound, loopCourseResult)
			)),
			Pair.of("Freq", Arrays.asList(
					new TiledOption<>("Frequency Option", config.getAudioConfig()::getFreqOption, frequencyOption),
					new TiledOption<>("Fast-forward", config.getAudioConfig()::getFastForward, fastForward)
			))
	);

	@Override
	public List<Pair<String, List<TiledOption<?>>>> getOptions() {
		return options;
	}

	private int convertPercentToInt(float value) {
		return (int) (value * 100.0);
	}
}
