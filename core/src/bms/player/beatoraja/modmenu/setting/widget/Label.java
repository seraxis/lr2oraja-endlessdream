package bms.player.beatoraja.modmenu.setting.widget;

import bms.player.beatoraja.modmenu.FontAwesomeIcons;
import bms.player.beatoraja.modmenu.ImGuiRenderer;
import imgui.ImColor;
import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

/**
 * Label is a simple text widget
 *
 * @implNote Imgui doesn't support dynamic font until 1.92, so it's not easy to change the font size dynamically
 *  here. Instead, we hardcoded the font data in ImguiRenderer :/
 */
public class Label implements SizedWidget {
	public static float IconLabelWidth = 16F;

	private String hint;
	private float hintWidth = DefaultHintWidth;
	public static final float DefaultHintWidth = 160F;
	private final String name;
	private int color = DefaultColor;
	public static final int DefaultColor = ImColor.rgb(0, 0, 0);
	private ImFont font;

	private Label(String name) {
		this.name = name;
	}

	public static Label categoryLabel(String name) {
		return Builder.categoryLabelBuilder(name).build();
	}

	@Override
	public float getWidth() {
		return ImGui.calcTextSizeX(this.name);
	}

	@Override
	public void render() {
		if (color != DefaultColor) {
			ImGui.pushStyleColor(ImGuiCol.Text, color);
		}
		if (font != null) {
			ImGui.pushFont(font);
		}
		ImGui.text(this.name);
		if (this.hint != null) {
			ImGui.setNextWindowSize(hintWidth, 0F);
			if (ImGui.beginItemTooltip()) {
				ImGui.textWrapped(this.hint);
				ImGui.endTooltip();
			}
		}
		if (font != null) {
			ImGui.popFont();
		}
		if (color != DefaultColor) {
			ImGui.popStyleColor();
		}
	}

	// Factory methods

	public static Label defaultAssistIconLabel() {
		return assistIconLabel("Your play would be restricted to assist clear if this option is flagged");
	}

	public static Label assistIconLabel(String hint) {
		return Builder.assistIconLabelBuilder(hint, DefaultHintWidth).build();
	}

	public static Label assistIconLabel(String hint, float hintWidth) {
		return Builder.assistIconLabelBuilder(hint, hintWidth).build();
	}

	public static Label restartIconLabel() {
		return new Builder(FontAwesomeIcons.PowerOff)
				.colorHex("#49E670")
				.hint("Need restart to make it take effect")
				.build();
	}

	public static Label warningIconLabel(String hint) {
		return new Builder(FontAwesomeIcons.ExclamationTriangle)
				.colorHex("#CC5C76")
				.hint(hint)
				.build();
	}

	public static class Builder {
		private final Label label;

		public Builder(String name) {
			label = new Label(name);
		}

		// Special builders
		public static Builder categoryLabelBuilder(String name) {
			return new Builder(name)
					.font(ImGuiRenderer.font24)
					.color(ImColor.rgb(0, 128, 128));
		}

		public static Builder assistIconLabelBuilder(String hint, float hintWidth) {
			return new Builder(FontAwesomeIcons.Child)
					.colorHex("#FF9FF9")
					.hint(hint)
					.hintWidth(hintWidth);
		}

		public Builder hint(String hint) {
			label.hint = hint;
			return this;
		}

		public Builder hintWidth(float hintWidth) {
			label.hintWidth = hintWidth;
			return this;
		}

		public Builder font(ImFont font) {
			label.font = font;
			return this;
		}

		public Builder color(int color) {
			label.color = color;
			return this;
		}

		public Builder colorHex(String hex) {
			return color(ImColor.rgb(hex));
		}

		public Label build() {
			return label;
		}
	}
}
