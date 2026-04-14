package bms.player.beatoraja.modmenu.skin.debugger;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.modmenu.skin.debugger.events.ChangeSingleFieldEvent;
import bms.player.beatoraja.modmenu.skin.debugger.events.ToggleVisibleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PersistedEvent {
	private static final Logger logger = LoggerFactory.getLogger(PersistedEvent.class);
	private String version;
	private String clazz;
	private String type;
	private String handleName;
	private Map<String, Object> payload;

	public PersistedEvent() {

	}

	public PersistedEvent(String version, String clazz, String type, String handleName, Map<String, Object> payload) {
		this.version = version;
		this.clazz = clazz;
		this.type = type;
		this.handleName = handleName;
		this.payload = payload;
	}

	public Event<?> load() {
		Event.EventType eventType = Event.EventType.from(type);
		if (eventType == null) {
			return null;
		}

		try {
			return switch (clazz) {
				case "ChangeSingleFieldEvent" -> new ChangeSingleFieldEvent(eventType, new SkinWidgetDestination(handleName, null, null), getFloat("previous"), getFloat("current"));
				case "ToggleVisibleEvent" ->  new ToggleVisibleEvent(new SkinWidget(handleName, null, null, null), getBoolean("isVisibleBefore"));
				default -> null;
			};
		} catch (Exception e) {
			logger.error("Failed to load a persisted event: ", e);
			ImGuiNotify.error(String.format("Failed to load a persisted event: %s", e.getMessage()));
		}

		return null;
	}

	private float getFloat(String key) {
		Number v = (Number) payload.get(key);
		return v.floatValue();
	}

	private boolean getBoolean(String key) {
		return ((boolean) payload.get(key));
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	public String getHandleName() {
		return handleName;
	}

	public void setHandleName(String handleName) {
		this.handleName = handleName;
	}
}
