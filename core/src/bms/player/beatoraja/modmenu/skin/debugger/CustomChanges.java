package bms.player.beatoraja.modmenu.skin.debugger;

import java.util.List;

public class CustomChanges {
	private String version;
	private List<PersistedEvent> events;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<PersistedEvent> getEvents() {
		return events;
	}

	public void setEvents(List<PersistedEvent> events) {
		this.events = events;
	}
}
