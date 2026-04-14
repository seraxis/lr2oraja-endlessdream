package bms.player.beatoraja.modmenu.skin.debugger.events;

import bms.player.beatoraja.modmenu.skin.debugger.Event;
import bms.player.beatoraja.modmenu.skin.debugger.PersistedEvent;
import bms.player.beatoraja.modmenu.skin.debugger.SkinWidget;

import java.util.HashMap;
import java.util.Map;

/**
 * Records the event when toggling the visibility of a widget
 */
public class ToggleVisibleEvent extends Event<SkinWidget> {
	private final boolean isVisibleBefore;

	public ToggleVisibleEvent(SkinWidget handle, boolean isVisibleBefore) {
		super(EventType.TOGGLE_VISIBLE, handle);
		this.isVisibleBefore = isVisibleBefore;
	}

	@Override
	public void redo() {
		handle.toggleVisible(false);
	}

	@Override
	public void undo() {
		handle.toggleVisible(false);
	}

	@Override
	public String getDescription() {
		return isVisibleBefore
				? String.format("Make %s widget invisible", handle.name)
				: String.format("Make %s widget visible", handle.name);
	}

	@Override
	public String getName() {
		return handle.name;
	}

	@Override
	public PersistedEvent persist() {
		Map<String, Object> payload = new HashMap<>();
		payload.put("isVisibleBefore", isVisibleBefore);
		return new PersistedEvent("1", "ToggleVisibleEvent", type.name(), handle.name, payload);
	}
}
