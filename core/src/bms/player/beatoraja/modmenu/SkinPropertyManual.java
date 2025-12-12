package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.skin.SkinManualEntry;
import bms.player.beatoraja.skin.SkinProperty.PropertyType;
import bms.player.beatoraja.skin.property.*;
import com.github.therapi.runtimejavadoc.*;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;

import java.util.*;
import java.util.stream.Collectors;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class SkinPropertyManual {
	private static final Map<PropertyType, List<SkinManualEntry<?>>> manualEntries = new HashMap<>();

	/**
	 * Initialize the manual entries, must be called during game startup process
	 */
	public static void init() {
		manualEntries.put(
				PropertyType.Boolean,
				Arrays.stream(BooleanPropertyFactory.BooleanType.values())
						.map(BooleanPropertyFactory.BooleanType::intoManualEntry)
						.collect(Collectors.toList())
		);
		manualEntries.put(
				PropertyType.Event,
				Arrays.stream(EventFactory.EventType.values())
						.map(EventFactory.EventType::intoManualEntry)
						.collect(Collectors.toList())
		);
		manualEntries.put(
				PropertyType.Float,
				Arrays.stream(FloatPropertyFactory.FloatType.values())
						.map(FloatPropertyFactory.FloatType::intoManualEntry)
						.collect(Collectors.toList())
		);
		manualEntries.put(
				PropertyType.String,
				Arrays.stream(StringPropertyFactory.StringType.values())
						.map(StringPropertyFactory.StringType::intoManualEntry)
						.collect(Collectors.toList())
		);
	}

	public static void show(ImBoolean showSkinPropertyManual) {
		float relativeX = windowWidth * 0.455f;
		float relativeY = windowHeight * 0.04f;
		ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);
		float textLineHeight = ImGui.getTextLineHeight();
		ImGui.setNextWindowSizeConstraints(30f, 40f, 30 * textLineHeight, 30 * textLineHeight);

		if (ImGui.begin("Skin Property Manual", showSkinPropertyManual)) {
			for (PropertyType propertyType : PropertyType.values()) {
				if (!manualEntries.containsKey(propertyType)) {
					continue;
				}
				if (ImGui.treeNodeEx(propertyType.name())) {
					manualEntries.get(propertyType).forEach(entry -> {
						if (ImGui.treeNodeEx(entry.getFullName())) {
							ImGui.textWrapped(entry.getComment());
							ImGui.treePop();
						}
					});
					ImGui.treePop();
				}
			}
			ImGui.end();
		}
	}
}
