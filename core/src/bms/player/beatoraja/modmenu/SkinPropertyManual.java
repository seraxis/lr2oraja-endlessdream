package bms.player.beatoraja.modmenu;

import bms.player.beatoraja.skin.SkinManualEntry;
import bms.player.beatoraja.skin.SkinProperty.PropertyType;
import bms.player.beatoraja.skin.lr2.LR2ButtonDef;
import bms.player.beatoraja.skin.lr2.LR2DestinationOptions;
import bms.player.beatoraja.skin.lr2.LR2NumberDef;
import bms.player.beatoraja.skin.lr2.LR2TextDef;
import bms.player.beatoraja.skin.property.BooleanPropertyFactory;
import bms.player.beatoraja.skin.property.EventFactory;
import bms.player.beatoraja.skin.property.FloatPropertyFactory;
import bms.player.beatoraja.skin.property.StringPropertyFactory;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class SkinPropertyManual {
	private static final Map<PropertyType, List<SkinManualEntry<?>>> rajaManualEntries = new HashMap<>();
	private static final Map<PropertyType, List<SkinManualEntry<?>>> lr2ManualEntries = new HashMap<>();

	/**
	 * Initialize the manual entries, must be called during game startup process
	 */
	public static void init() {
		rajaManualEntries.put(
				PropertyType.Boolean,
				Arrays.stream(BooleanPropertyFactory.BooleanType.values())
						.map(BooleanPropertyFactory.BooleanType::intoManualEntry)
						.collect(Collectors.toList())
		);
		rajaManualEntries.put(
				PropertyType.Event,
				Arrays.stream(EventFactory.EventType.values())
						.map(EventFactory.EventType::intoManualEntry)
						.collect(Collectors.toList())
		);
		rajaManualEntries.put(
				PropertyType.Float,
				Arrays.stream(FloatPropertyFactory.FloatType.values())
						.map(FloatPropertyFactory.FloatType::intoManualEntry)
						.collect(Collectors.toList())
		);
		rajaManualEntries.put(
				PropertyType.String,
				Arrays.stream(StringPropertyFactory.StringType.values())
						.map(StringPropertyFactory.StringType::intoManualEntry)
						.collect(Collectors.toList())
		);
		lr2ManualEntries.put(
				PropertyType.Boolean,
				Arrays.stream(LR2DestinationOptions.values())
						.map(LR2DestinationOptions::intoManualEntry)
						.collect(Collectors.toList())
		);
		lr2ManualEntries.put(
				PropertyType.String,
				Arrays.stream(LR2TextDef.values())
						.map(LR2TextDef::intoManualEntry)
						.collect(Collectors.toList())
		);
		lr2ManualEntries.put(
				PropertyType.Number,
				Arrays.stream(LR2NumberDef.values())
						.map(LR2NumberDef::intoManualEntry)
						.collect(Collectors.toList())
		);
		lr2ManualEntries.put(
				PropertyType.Button,
				Arrays.stream(LR2ButtonDef.values())
						.map(LR2ButtonDef::intoManualEntry)
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
			if (ImGui.beginTabBar("##TabBar##SkinPropertyManual")) {
				if (ImGui.beginTabItem("Endless Dream")) {
					renderManualEntries(rajaManualEntries);
					ImGui.endTabItem();
				}
				if (ImGui.beginTabItem("LR2")) {
					renderManualEntries(lr2ManualEntries);
					ImGui.endTabItem();
				}
				ImGui.endTabBar();
			}

			ImGui.end();
		}
	}

	private static void renderManualEntries(Map<PropertyType, List<SkinManualEntry<?>>> entries) {
		for (PropertyType propertyType : PropertyType.values()) {
			if (!entries.containsKey(propertyType)) {
				continue;
			}
			if (ImGui.treeNodeEx(propertyType.name())) {
				entries.get(propertyType).forEach(entry -> {
					if (ImGui.treeNodeEx(entry.getFullName())) {
						ImGui.textWrapped(entry.getComment());
						ImGui.treePop();
					}
				});
				ImGui.treePop();
			}
		}
	}
}
