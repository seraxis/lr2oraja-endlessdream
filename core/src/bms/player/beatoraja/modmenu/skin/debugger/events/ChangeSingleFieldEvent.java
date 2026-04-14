package bms.player.beatoraja.modmenu.skin.debugger.events;

import bms.player.beatoraja.modmenu.skin.debugger.Event;
import bms.player.beatoraja.modmenu.skin.debugger.PersistedEvent;
import bms.player.beatoraja.modmenu.skin.debugger.SkinWidgetDestination;

import java.util.HashMap;
import java.util.Map;

/**
 * Records the event when changing a single field from a widget
 */
public class ChangeSingleFieldEvent extends Event<SkinWidgetDestination> {
	private float previous;
	private float current;

	public ChangeSingleFieldEvent(EventType type, SkinWidgetDestination dst, float previous, float current) {
		super(type, dst);
		this.previous = previous;
		this.current = current;
	}

	@Override
	public void redo() {
		switch (type) {
			case CHANGE_X -> handle.setDstX(current, false);
			case CHANGE_Y -> handle.setDstY(current, false);
			case CHANGE_W -> handle.setDstW(current, false);
			case CHANGE_H -> handle.setDstH(current, false);
			default -> { /* Intentionally do nothing */ }
		}
	}

	@Override
	public void undo() {
		switch (type) {
			case CHANGE_X -> handle.setDstX(previous, false);
			case CHANGE_Y -> handle.setDstY(previous, false);
			case CHANGE_W -> handle.setDstW(previous, false);
			case CHANGE_H -> handle.setDstH(previous, false);
			default -> { /* Intentionally do nothing */ }
		}
	}

	@Override
	public String getDescription() {
		String fieldName = switch (type) {
			case CHANGE_X -> "x";
			case CHANGE_Y -> "y";
			case CHANGE_W -> "width";
			case CHANGE_H -> "height";
			default -> "[ERROR] Not a ChangeSingleFieldEvent";
		};
		return String.format("Changed %s's %s from %.4f to %.4f", handle.name, fieldName, previous, current);
	}

	@Override
	public String getName() {
		return handle.name;
	}

	@Override
	public PersistedEvent persist() {
		Map<String, Object> payload = new HashMap<>();
		payload.put("previous", previous);
		payload.put("current", current);
		return new PersistedEvent("1", "ChangeSingleFieldEvent", type.name(), handle.name, payload);
	}
}