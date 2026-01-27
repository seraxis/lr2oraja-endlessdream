package bms.player.beatoraja.modmenu.setting.keybinding;

import com.badlogic.gdx.Input;

import static bms.player.beatoraja.input.KeyBoardInputProcesseor.*;

public class PlainKeyBinding extends KeyBinding {
	public PlainKeyBinding() {

	}

	public PlainKeyBinding(String name, int keyCode, int modifier, int mapping) {
		super(name, keyCode, modifier, mapping);
	}

	public PlainKeyBinding(String name, String description, int keyCode, int modifier, int mapping) {
		super(name, description, "", keyCode, modifier, mapping, false);
	}

	public PlainKeyBinding(String name, String description, String scene, int keyCode, int modifier, int mapping) {
		super(name, description, scene, keyCode, modifier, mapping, false);
	}

	@Override
	public String keyName() {
		StringBuilder sb = new StringBuilder();
		if (modifier() != 0) {
			if ((modifier() & MASK_CTRL) != 0) {
				sb.append("CTRL+");
			}
			if ((modifier() & MASK_ALT) != 0) {
				sb.append("ALT+");
			}
			if ((modifier() & MASK_SHIFT) != 0) {
				sb.append("SHIFT+");
			}
		}
		sb.append(Input.Keys.toString(keyCode()));
		return sb.toString();
	}

	public static class Builder {
		private String name;
		private String description = "";
		private String scene = "";
		private int keyCode;
		private int modifier = 0;
		private int mapping = -1;

		public Builder(String name, int keyCode) {
			this.name = name;
			this.keyCode = keyCode;
		}

		public Builder scene(String scene) {
			this.scene = scene;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder mapping(int mapping) {
			this.mapping = mapping;
			return this;
		}

		public Builder modifier(int modifier) {
			this.modifier = modifier;
			return this;
		}

		public PlainKeyBinding build() {
			return new PlainKeyBinding(name, description, scene, keyCode, modifier, mapping);
		}
	}
}
