package bms.player.beatoraja.modmenu.skin.debugger;

import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import bms.player.beatoraja.skin.lr2.LR2DestinationOptions;
import bms.player.beatoraja.skin.lr2.LR2NumberDef;
import bms.player.beatoraja.skin.lr2.LR2TextDef;
import bms.tool.util.Pair;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiTableFlags;

import java.util.ArrayList;
import java.util.List;

/**
 * Show current skin's customized options, used internal options and the options that are missing implementation
 */
public class SkinOptions {
	private static final List<Pair<Integer, Integer>> skinOptions = new ArrayList<>();
	private static final List<Integer> missingOps = new ArrayList<>();
	private static final List<Integer> missingTextDefinitions = new ArrayList<>();
	private static final List<Integer> missingNumberDefinitions = new ArrayList<>();

	static void removeCurrentOptions() {
		skinOptions.clear();
	}

	static void changeSkin(Skin skin) {
		SkinObject[] skinObjects = skin.getAllSkinObjects();
		for (SkinObject skinObject : skinObjects) {
			int[] dstOp = skinObject.getOption();
			for (int op : dstOp) {
				op = Math.abs(op);
				if (op >= 900 && op <= 999) {
					continue;
				}
				registerMissingOp(op);
			}
		}

		skin.getOption().forEach(entry -> skinOptions.add(Pair.of(entry.key, entry.value)));
		skinOptions.sort(Pair.DEFAULT_COMPARATOR());

		missingOps.sort(Integer::compareTo);
		missingNumberDefinitions.sort(Integer::compareTo);
		missingTextDefinitions.sort(Integer::compareTo);
	}

	public static void registerMissingOp(int value) {
		if (!missingOps.contains(value)) {
			missingOps.add(value);
		}
	}

	public static void registerMissingTextDefinition(int value) {
		if (!missingTextDefinitions.contains(value)) {
			missingTextDefinitions.add(value);
		}
	}

	public static void registerMissingNumberDefinition(int value) {
		if (!missingNumberDefinitions.contains(value)) {
			missingNumberDefinitions.add(value);
		}
	}

	public static void render() {
		if (ImGui.beginTabBar("##TabBar##Skin Options")) {
			if (ImGui.beginTabItem("Options##TabItem##Skin Options")) {
				renderSkinOptions();
				ImGui.endTabItem();
			}
			if (ImGui.beginTabItem("Unimplemented##TabItem##Skin Options")) {
				renderUnimplementedOptions();
				ImGui.endTabItem();
			}
			ImGui.endTabBar();
		}
	}

	private static void renderUnimplementedOptions() {
		if (ImGui.treeNodeEx("DstOp##MissingNo")) {
			if (ImGui.beginTable("DstOp##MissingNo##Table", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
				ImGui.tableSetupScrollFreeze(0, 1);
				ImGui.tableSetupColumn("Name");
				ImGui.tableSetupColumn("Value");
				ImGui.tableHeadersRow();
				ImGuiListClipper.forEach(missingOps.size(), new ImListClipperCallback() {
					@Override
					public void accept(int row) {
						ImGui.pushID(row);
						ImGui.tableNextRow();

						Integer value = missingOps.get(row);
						ImGui.tableSetColumnIndex(0);
						LR2DestinationOptions opDef = LR2DestinationOptions.valueOf(value);
						ImGui.text(opDef != null ? opDef.name() : "ERROR");

						ImGui.tableSetColumnIndex(1);
						ImGui.text(value.toString());
						ImGui.popID();
					}
				});
				ImGui.endTable();
			}
			ImGui.treePop();
		}
		if (ImGui.treeNodeEx("Text##MissingNo")) {
			if (ImGui.beginTable("Text##MissingNo##Table", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
				ImGui.tableSetupScrollFreeze(0, 1);
				ImGui.tableSetupColumn("Name");
				ImGui.tableSetupColumn("Value");
				ImGui.tableHeadersRow();
				ImGuiListClipper.forEach(missingTextDefinitions.size(), new ImListClipperCallback() {
					@Override
					public void accept(int row) {
						ImGui.pushID(row);
						ImGui.tableNextRow();

						Integer value = missingTextDefinitions.get(row);
						ImGui.tableSetColumnIndex(0);
						LR2TextDef textDef = LR2TextDef.valueOf(value);
						ImGui.text(textDef != null ? textDef.name() : "ERROR");

						ImGui.tableSetColumnIndex(1);
						ImGui.text(value.toString());
						ImGui.popID();
					}
				});
				ImGui.endTable();
			}
			ImGui.treePop();
		}
		if (ImGui.treeNodeEx("Number##MissingNo")) {
			if (ImGui.beginTable("Number##MissingNo##Table", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
				ImGui.tableSetupScrollFreeze(0, 1);
				ImGui.tableSetupColumn("Name");
				ImGui.tableSetupColumn("Value");
				ImGui.tableHeadersRow();
				ImGuiListClipper.forEach(missingNumberDefinitions.size(), new ImListClipperCallback() {
					@Override
					public void accept(int row) {
						ImGui.pushID(row);
						ImGui.tableNextRow();

						Integer value = missingNumberDefinitions.get(row);
						ImGui.tableSetColumnIndex(0);
						LR2NumberDef numberDef = LR2NumberDef.valueOf(value);
						ImGui.text(numberDef != null ? numberDef.name() : "ERROR");

						ImGui.tableSetColumnIndex(1);
						ImGui.text(value.toString());
						ImGui.popID();
					}
				});
				ImGui.endTable();
			}
			ImGui.treePop();
		}
	}

	private static void renderSkinOptions() {
		if (ImGui.beginTable("Options##SkinOptions", 3, ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollY, 0, ImGui.getTextLineHeight() * 20)) {
			ImGui.tableSetupScrollFreeze(0, 1);
			ImGui.tableSetupColumn("Name##SkinOptions");
			ImGui.tableSetupColumn("Op##SkinOptions");
			ImGui.tableSetupColumn("Value##SkinOptions");
			ImGui.tableHeadersRow();
			ImGuiListClipper.forEach(skinOptions.size(), new ImListClipperCallback() {
				@Override
				public void accept(int row) {
					ImGui.pushID(row);

					Pair<Integer, Integer> p = skinOptions.get(row);
					ImGui.tableNextRow();

					ImGui.tableSetColumnIndex(0);
					Integer op = p.getFirst();
					if (op >= 900 && op <= 999) {
						ImGui.text("-");
					} else {
						LR2DestinationOptions dstDef = LR2DestinationOptions.valueOf(op);
						ImGui.text(dstDef != null ? dstDef.getName() : "ERROR");
					}

					ImGui.tableSetColumnIndex(1);
					ImGui.text(op.toString());

					ImGui.tableSetColumnIndex(2);
					ImGui.text(p.getSecond().toString());

					ImGui.popID();
				}
			});
			ImGui.endTable();
		}
	}
}
