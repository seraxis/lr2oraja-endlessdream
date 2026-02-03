package bms.player.beatoraja.modmenu.setting.keybinding;

import com.badlogic.gdx.Input;

import java.util.Arrays;
import java.util.Optional;

import static bms.player.beatoraja.input.KeyBoardInputProcesseor.MASK_CTRL;
import static bms.player.beatoraja.input.KeyBoardInputProcesseor.MASK_SHIFT;

public enum MusicSelectKeyBindings {
	UPDATE_FOLDER(builder("Refresh folder/difficult table", Input.Keys.F2).build()),
	OPEN_CHART_DIRECTORY_IN_EXPLORER(builder("Open chart's directory in explorer", Input.Keys.F3).build()),
	FAVORITE_SONG(builder("Set song as favorite song", Input.Keys.F8).build()),
	FAVORITE_CHART(builder("Set chart as favorite chart", Input.Keys.F9).build()),
	AUTOPLAY_ALL_SONGS_IN_FOLDER(builder("Autoplay all songs in folder", Input.Keys.F10).build()),
	OPEN_CHART_PAGE_IN_PRIMARY_IR(builder("Open chart page in the primary IR", Input.Keys.F11).build()),
	OPEN_SKIN_SETTINGS(builder("Open skin settings", Input.Keys.F12).build()),
	FOCUS_SEARCH_TEXT(builder("Focus search text", Input.Keys.NUM_0).build()),
	SWITCH_PLAY_MODE(builder("Switch play mode", Input.Keys.NUM_1).build()),
	SWITCH_SORT_STRATEGY(builder("Switch sort strategy", Input.Keys.NUM_2).build()),
	SELECT_REPLAY_LOG(builder("Select replay log", Input.Keys.NUM_4).build()),
	OPTIONS_MENU_3(builder("Options menu 3", Input.Keys.NUM_5).build()),
	OPEN_KEY_CONFIGURATION(builder("Open key configuration", Input.Keys.NUM_6).build()),
	CYCLE_THROUGH_RIVALS(builder("Cycle through rivals", Input.Keys.NUM_7).build()),
	SHOW_SONGS_IN_SAME_FOLDER(builder("Show songs in same folder", Input.Keys.NUM_8).build()),
	DISPLAY_SONG_TEXT_FILE(builder("Display song text file", Input.Keys.NUM_9).build()),
	COPY_SONG_MD5_HASH(builder("Copy song md5 hash", Input.Keys.F3).modifier(MASK_CTRL).build()),
	COPY_SONG_SHA256_HASH(builder("Copy song sha256 hash", Input.Keys.F3).modifier(MASK_SHIFT).build()),
	COPY_HIGHLIGHTED_MENU_TEXT(builder("Copy highlighted menu text", Input.Keys.C).modifier(MASK_CTRL).build());

	private final KeyBinding keyBinding;

	MusicSelectKeyBindings(KeyBinding keyBinding) {
		this.keyBinding = keyBinding;
	}

	private static PlainKeyBinding.Builder builder(String name, int keyCode) {
		return new PlainKeyBinding.Builder(name, keyCode)
				.scene("MusicSelect");
	}

	public int keyCode() {
		return keyBinding.keyCode();
	}

	public void setKeyCode(int keyCode) {
		keyBinding.setKeyCode(keyCode);
	}

	public int modifier() {
		return keyBinding.modifier();
	}

	public boolean disabled() {
		return keyBinding.disabled();
	}

	public KeyBinding keyBinding() {
		return keyBinding;
	}

	public void setDisabled(boolean disabled) {
		this.keyBinding.setDisabled(disabled);
	}

	public void setModifier(int modifier) {
		this.keyBinding.setModifier(modifier);
	}


	public static Optional<MusicSelectKeyBindings> fromName(String name) {
		return Arrays.stream(MusicSelectKeyBindings.values()).filter(bind -> bind.keyBinding.name().equals(name)).findAny();
	}
}
