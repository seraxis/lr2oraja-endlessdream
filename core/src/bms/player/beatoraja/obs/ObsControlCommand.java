package bms.player.beatoraja.obs;

public class ObsControlCommand {
	private String timing = "";
	private String action = "";
	private String targetScene = "";
	private String targetSource = "";
	private String transitionScene = "";

	public ObsControlCommand() {
	}

	public ObsControlCommand(String timing, String action, String targetScene, String targetSource,
			String transitionScene) {
		this.timing = timing;
		this.action = action;
		this.targetScene = targetScene;
		this.targetSource = targetSource;
		this.transitionScene = transitionScene;
	}

	public String getTiming() {
		return timing;
	}

	public void setTiming(String timing) {
		this.timing = timing != null ? timing : "";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action != null ? action : "";
	}

	public String getTargetScene() {
		return targetScene;
	}

	public void setTargetScene(String targetScene) {
		this.targetScene = targetScene != null ? targetScene : "";
	}

	public String getTargetSource() {
		return targetSource;
	}

	public void setTargetSource(String targetSource) {
		this.targetSource = targetSource != null ? targetSource : "";
	}

	public String getTransitionScene() {
		return transitionScene;
	}

	public void setTransitionScene(String transitionScene) {
		this.transitionScene = transitionScene != null ? transitionScene : "";
	}

	public String getTargetDisplay() {
		if (ObsWsClient.ACTION_SET_SCENE.equals(action)) {
			return transitionScene;
		}
		return targetScene;
	}

	public String getDetailDisplay() {
		if (ObsWsClient.ACTION_SHOW_SOURCE.equals(action) || ObsWsClient.ACTION_HIDE_SOURCE.equals(action)) {
			return targetSource;
		}
		return "";
	}
}
