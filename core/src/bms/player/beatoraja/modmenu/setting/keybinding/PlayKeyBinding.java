package bms.player.beatoraja.modmenu.setting.keybinding;

import bms.model.Mode;
import bms.player.beatoraja.config.KeyConfiguration;
import com.badlogic.gdx.Input;

public class PlayKeyBinding extends KeyBinding {
	private boolean holdModifier = false;
	private Mode playMode;
	private int keyIndex;

	public PlayKeyBinding(String name, int keyIndex, int keyCode, int mapping, Mode playMode) {
		this(name, "", keyIndex, keyCode, mapping, playMode);
	}

	public PlayKeyBinding(String name, String description, int keyIndex, int keyCode, int mapping, Mode playMode) {
		super(name, description, "", keyCode, 0, mapping, false);
		this.keyIndex = keyIndex;
		this.playMode = playMode;
	}

	public PlayKeyBinding requiringHold() {
		this.holdModifier = true;
		return this;
	}

	public Mode getPlayMode() {
		return playMode;
	}

	public void setPlayMode(Mode playMode) {
		this.playMode = playMode;
	}

	@Override
	public String keyName() {
		StringBuilder sb = new StringBuilder();
		switch (keyIndex) {
			case -1 -> sb.append("SELECT");
			case -2 -> sb.append("START");
			default -> sb.append(KeyConfiguration.getKeyNames(playMode)[keyIndex]);
		}
		sb.append('(').append(Input.Keys.toString(keyCode())).append(')');
		if (holdModifier) {
			sb.append(" (hold)");
		}
		return sb.toString();
	}
}
