package bms.player.beatoraja.modmenu.skin.debugger;

public abstract class Event<T> {
	protected EventType type;
	protected T handle; // reference to the event object

	// NOTE: This is kinda naive, but it works...
	public enum EventType {
		// ChangeSingleFieldEvent
		CHANGE_X,
		CHANGE_Y,
		CHANGE_W,
		CHANGE_H,
		// ToggleVisibleEvent
		TOGGLE_VISIBLE;

		public static EventType from(String name) {
			for (EventType et : EventType.values()) {
				if (et.name().equalsIgnoreCase(name)) {
					return et;
				}
			}
			return null;
		}
	}

	public Event(EventType type, T handle) {
		this.type = type;
		this.handle = handle;
	}

	public abstract void redo();

	public abstract void undo();

	public abstract String getDescription();

	public abstract String getName();

	public abstract PersistedEvent persist();
}
