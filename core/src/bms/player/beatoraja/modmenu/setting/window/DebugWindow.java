package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.Version;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.tool.util.Pair;
import imgui.ImGui;
import imgui.type.ImString;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugWindow extends BaseSettingWindow {
	private final Logger logger = LoggerFactory.getLogger(DebugWindow.class);
	private static final List<Pair<String, String>> systemInfo = new ArrayList<>();
	static {
		systemInfo.add(Pair.of("Commit Hash", Version.getGitCommitHash()));
		systemInfo.add(Pair.of("Java Version", System.getProperty("java.version")));
		systemInfo.add(Pair.of("Java Vendor", System.getProperty("java.vendor")));
		systemInfo.add(Pair.of("Build Time", Version.getBuildDate().toString()));
		systemInfo.add(Pair.of("GLFW Version", GLFW.glfwGetVersionString()));
	}

	private final ImString issueTitle = new ImString(60);
	private final ImString issueReport = new ImString(1000);

	public DebugWindow(Config config, PlayerConfig playerConfig) {
		super(config, playerConfig);
	}

	@Override
	public String getName() {
		return "Debug";
	}

	@Override
	public void render() {
		systemInfo.forEach(p -> ImGui.text(p.getFirst() + ": " + p.getSecond()));
		ImGui.inputText("Title##Issue Report", issueTitle);
		ImGui.inputTextMultiline("##Issue Report Body", issueReport);
		ImGui.beginDisabled(!Desktop.isDesktopSupported());
		try {
			ImGui.beginDisabled(issueTitle.isEmpty() || issueReport.isEmpty());
			if (ImGui.button("Report a Bug")) {
				StringBuilder sb = new StringBuilder();
				systemInfo.forEach(p -> sb.append("**").append(p.getFirst()).append(": ").append(p.getSecond()).append("**\n"));
				sb.append("\n").append(issueReport.get());
				String encodedTitle = URLEncoder.encode(issueTitle.get(), StandardCharsets.UTF_8);
				String encodedBody = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
				Desktop.getDesktop().browse(new URI(String.format("https://github.com/seraxis/lr2oraja-endlessdream/issues/new?title=%s&body=%s", encodedTitle, encodedBody)));
			}
			ImGui.endDisabled();
		} catch (Exception e) {
			logger.error("Failed to open browser to github: ", e);
			ImGuiNotify.error("Failed to open browser, please try to submit your report manually at github");
		}

		ImGui.endDisabled();
	}

	@Override
	public void refresh() {

	}

}
