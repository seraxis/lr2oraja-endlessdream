package bms.player.beatoraja.modmenu.skinwidget;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.modmenu.FontAwesomeIcons;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.skin.SkinProperty;
import bms.player.beatoraja.skin.property.BooleanPropertyFactory;
import bms.player.beatoraja.skin.property.FloatPropertyFactory;
import bms.player.beatoraja.skin.property.IntegerPropertyFactory;
import bms.player.beatoraja.skin.property.StringPropertyFactory;
import bms.tool.utils.Either;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Skin property watcher
 */
public class SkinPropertyWatcher {
	private static final List<Watcher> watchers = new ArrayList<>();
	private static MainController main;

	private static String addingWatcherType = SkinProperty.PropertyType.Boolean.name();
	private static ImString addingWatcherIdOrName = new ImString(64);
	private static boolean autoRefresh = false;

	public static void init(MainController main) {
		SkinPropertyWatcher.main = main;
	}

	public static void render() {
		if (ImGui.button("Add##SkinPropertyWatcher")) {
			// NOTE: We don't reset watcher's type here because users are more likely to add
			//  same type of watchers sequentially
			addingWatcherIdOrName.set("");
			ImGui.openPopup("Add Skin Property Watcher");
		}
		ImGui.sameLine();
		if (ImGui.button("Refresh All##SkinPropertyWatcher")) {
			watchers.forEach(watcher -> watcher.evaluate(main.getCurrentState()));
		}
		ImGui.sameLine();
		if (ImGui.checkbox("Auto Refresh##SkinPropertyWatcher", autoRefresh)) {
			// TODO: Implement me!
			autoRefresh = !autoRefresh;
		}
		if (ImGui.beginPopup("Add Skin Property Watcher", ImGuiWindowFlags.AlwaysAutoResize)) {
			if (ImGui.beginCombo("type##SkinPropertyWatcher", addingWatcherType, ImGuiComboFlags.HeightLarge)) {
				for (SkinProperty.PropertyType propertyType : SkinProperty.PropertyType.values()) {
					ImGui.pushID(propertyType.name());
					if (ImGui.selectable(propertyType.name())) {
						addingWatcherType = propertyType.name();
					}
					ImGui.popID();
				}
				ImGui.endCombo();
			}
			ImGui.inputText("ID/Name", addingWatcherIdOrName);
			if (ImGui.button("Submit##SkinPropertyWatcher")) {
				Optional<Watcher> watcher = Watcher.create(
						SkinProperty.PropertyType.valueOf(addingWatcherType),
						addingWatcherIdOrName.get(),
						main.getCurrentState()
				);
				if (watcher.isPresent()) {
					watchers.add(watcher.get());
				} else {
					ImGuiNotify.error("No such property");
				}
				ImGui.closeCurrentPopup();
			}
			ImGui.endPopup();
		}
		if (ImGui.beginTable("Watchers", 4, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
			ImGui.tableSetupScrollFreeze(0, 1);
			ImGui.tableSetupColumn("Type");
			ImGui.tableSetupColumn("Symbol");
			ImGui.tableSetupColumn("Value");
			ImGui.tableSetupColumn("Op");
			ImGui.tableHeadersRow();
			ImGuiListClipper.forEach(watchers.size(), new ImListClipperCallback() {
				@Override
				public void accept(int row) {
					ImGui.pushID(row);

					ImGui.tableNextRow();
					Watcher watcher = watchers.get(row);

					ImGui.tableNextColumn();
					ImGui.text(watcher.type.name());

					ImGui.tableNextColumn();
					ImGui.text(String.valueOf(watcher.getSymbol()));

					ImGui.tableNextColumn();
					ImGui.text(watcher.value);

					ImGui.tableNextColumn();
					if (ImGui.button(FontAwesomeIcons.Redo)) {
						watcher.evaluate(main.getCurrentState());
					}
					ImGui.sameLine();
					// TODO: Is unsafe to remove the element inside an iteration
					if (ImGui.button(FontAwesomeIcons.WindowClose)) {
						watchers.remove(watcher);
					}

					ImGui.popID();
				}
			});
			ImGui.endTable();
		}
	}

	private static class Watcher {
		private final SkinProperty.PropertyType type;
		private final Either<Integer, String> idOrName;
		private String value;

		/**
		 * Create a watcher, which initial value is evaluated by current 'state'
		 *
		 * @param type  the watching property's type
		 * @param input skin property's id or name
		 * @param state current game state
		 * @return empty when property doesn't exist
		 */
		public static Optional<Watcher> create(SkinProperty.PropertyType type, String input, MainState state) {
			Either<Integer, String> idOrName = Either.parseInteger(input);
			Optional<String> evaluated = Either.unwrap(idOrName.apply(
					id -> evaluate(id, type, state),
					name -> evaluate(name, type, state)
			));
			return evaluated.map(value -> new Watcher(type, idOrName, value));
		}

		/**
		 * Evaluate the watching property
		 *
		 * @implNote If a watcher is ever created, it's not possible that it doesn't be connected
		 * to a skin property, so we should be safe to unwrap the optional returned by 'evaluate'
		 */
		public void evaluate(MainState state) {
			this.value = Either.unwrap(idOrName.apply(
					id -> evaluate(id, type, state),
					name -> evaluate(name, type, state)
			)).orElse("[!] Failed to evaluate");
		}

		public String getSymbol() {
			return idOrName.isLeft() ? String.valueOf(idOrName.getLeft()) : idOrName.getRight();
		}

		private Watcher(SkinProperty.PropertyType type, Either<Integer, String> idOrName, String value) {
			this.type = type;
			this.idOrName = idOrName;
			this.value = value;
		}

		private static Optional<String> evaluate(int id, SkinProperty.PropertyType type, MainState state) {
			return switch (type) {
				case Boolean -> Optional.ofNullable(BooleanPropertyFactory.getBooleanProperty(id))
						.map(booleanProperty -> String.valueOf(booleanProperty.get(state)));
				case Float -> Optional.ofNullable(FloatPropertyFactory.getFloatProperty(id))
						.map(floatProperty -> String.valueOf(floatProperty.get(state)));
				case Integer -> Optional.ofNullable(IntegerPropertyFactory.getIntegerProperty(id))
						.map(integerProperty -> String.valueOf(integerProperty.get(state)));
				case String -> Optional.ofNullable(StringPropertyFactory.getStringProperty(id))
						.map(stringProperty -> String.valueOf(stringProperty.get(state)));
				default -> Optional.empty();
			};
		}

		private static Optional<String> evaluate(String name, SkinProperty.PropertyType type, MainState state) {
			return switch (type) {
				case Float -> Optional.ofNullable(FloatPropertyFactory.getFloatProperty(name))
						.map(floatProperty -> String.valueOf(floatProperty.get(state)));
				case Integer -> Optional.ofNullable(IntegerPropertyFactory.getIntegerProperty(name))
						.map(integerProperty -> String.valueOf(integerProperty.get(state)));
				case String -> Optional.ofNullable(StringPropertyFactory.getStringProperty(name))
						.map(stringProperty -> String.valueOf(stringProperty.get(state)));
				default -> Optional.empty();
			};
		}
	}
}
