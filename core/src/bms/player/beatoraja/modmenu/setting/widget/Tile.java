package bms.player.beatoraja.modmenu.setting.widget;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;

/**
 * Tile is a container widget, which composes a 'name' and an 'action'. It's designed to display a configurable option,
 *  its illustration looks like:
 *
 * <pre>
 * +-------------------------------------+
 * |              |             |        |
 * |              |             |        |
 * | (Icon)Name(?)|             |action  |
 * |              |             |        |
 * |              |             |        |
 * +-------------------------------------+
 * </pre>
 *
 * Icon is an optional part that used to highlight this option are different. For example, if enabling an option would
 *  cause the play be marked as unqualified or assist clear, this option should be prefixed with an icon.
 * The tile container would render a blank space to ensure the alignment even when no icon is provided.
 * <br>
 * The (?) is a hover-tooltip that shows a detailed explanation about this option. Its content is 'description' field.
 *  And it won't be rendered if description is not provided.
 * <br>
 * The action is a 'SizedWidget', which is used to handle the user inputs like flagging the option or input some value.
 *  The reason that action is not a 'Widget' is because 'Tile' will try to align the action widget to the right.
 *  Therefore, we need to know the width of the action widget to calculate how many space we need to fill in the middle.
 * <br>
 * However, Tile itself doesn't serve any capability of handling the value that action widget renders and controls:
 *  it's * only a container widget that serves for rendering. This is also the reason why 'Action' is a SizedWidget
 *  rather than ActionWidget: Tile is only a container class that supports rendering.
 * <br>
 *
 * Additionally, you can attach some labels around the 'name' to help you display some hints for the option.
 * <br>
 * TODO: Description is not implemented yet....
 */
public class Tile implements Widget {
	private Label iconLabel;
	private final String name;
	private Label descriptionLabel;
	private final SizedWidget action;

	public Tile(String name) {
		this(name, null);
	}

	public Tile(String name, SizedWidget action) {
		this.name = name;
		this.action = action;
	}

	public void addIcon(Label iconLabel) {
		this.iconLabel = iconLabel;
	}

	public void addDescription(String description) {
		if (description == null || description.isEmpty()) {
			this.descriptionLabel = null;
			return ;
		}
		this.descriptionLabel = new Label.Builder("(?)").hint(description).build();
	}

	@Override
	public void render() {
		ImGui.beginGroup();
		ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.2f, 0.2f, 0.2f, 0.8f);

		if (ImGui.beginChild(this.name + "##Container", 0, 30, false)) {
			float textHeight = ImGui.calcTextSizeY(this.name);
			float windowHeight = ImGui.getWindowHeight();
			float textY = (windowHeight - textHeight) / 2;
			ImGui.setCursorPosY(textY);
			ImGui.setCursorPosX(ImGui.getCursorPosX() + 8F);
			if (this.iconLabel != null) {
				this.iconLabel.render();
				ImGui.sameLine();
			} else {
				ImGuiStyle style = ImGui.getStyle();
				ImGui.setCursorPosX(ImGui.getCursorPosX() + Label.IconLabelWidth + style.getFramePaddingX());
			}
			ImGui.text(this.name);
			if (descriptionLabel != null) {
				ImGui.sameLine();
				descriptionLabel.render();
			}

			if (this.action != null) {
				ImGui.sameLine();
				float spacing = ImGui.getContentRegionAvailX() - action.getWidth() - 16F;
				if (spacing > 0) {
					ImGui.dummy(new ImVec2(spacing, 0F));
					ImGui.sameLine();
				}

				this.action.render();
			}
		}

		ImGui.popStyleColor();

		ImGui.endChild();
		ImGui.endGroup();
	}
}
