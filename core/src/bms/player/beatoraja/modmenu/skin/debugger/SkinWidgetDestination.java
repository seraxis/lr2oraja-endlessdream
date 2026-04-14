package bms.player.beatoraja.modmenu.skin.debugger;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.modmenu.skin.debugger.events.ChangeSingleFieldEvent;
import bms.player.beatoraja.skin.SkinObject;

import java.util.function.Consumer;

/**
 * A simple wrapper class of SkinObject.SkinObjectDestination
 *
 * @implNote setter functions must provide an extra argument to not trigger event system
 */
public class SkinWidgetDestination {
	private static final double eps = 1e-5;

	public final String name;
	public SkinObject.SkinObjectDestination beforeMove = null;
	/**
	 * <ul>
	 *     <li>0: haven't started moving</li>
	 *     <li>1: user enabled the move feature, but hasn't move around</li>
	 *     <li>2: user has moved the widget</li>
	 * </ul>
	 */
	public int movingState;

	private final SkinObject.SkinObjectDestination destination;
	private final Consumer<Event<?>> pushupEvent;

	public SkinWidgetDestination(String name, SkinObject.SkinObjectDestination destination, Consumer<Event<?>> pushupEvent) {
		this.name = name;
		this.destination = destination;
		this.movingState = 0;
		this.pushupEvent = pushupEvent;
	}

	public float getDstX() {
		return destination.region.x;
	}

	public float getDstY() {
		return destination.region.y;
	}

	public float getDstW() {
		return destination.region.width;
	}

	public float getDstH() {
		return destination.region.height;
	}

	public void setDstX(float x) {
		setDstX(x, true);
	}

	public void setDstX(float x, boolean createEvent) {
		float previous = this.getDstX();
		if (createEvent && Math.abs(x - previous) > eps) {
			pushupEvent.accept(new ChangeSingleFieldEvent(Event.EventType.CHANGE_X, this, previous, x));
		}
		destination.region.x = x;
	}

	public void setDstY(float y) {
		setDstY(y, true);
	}

	public void setDstY(float y, boolean createEvent) {
		float previous = this.getDstY();
		if (createEvent && Math.abs(y - previous) > eps) {
			pushupEvent.accept(new ChangeSingleFieldEvent(Event.EventType.CHANGE_Y, this, previous, y));
		}
		destination.region.y = y;
	}

	public void setDstW(float w) {
		setDstW(w, true);
	}

	public void setDstW(float w, boolean createEvent) {
		float previous = this.getDstW();
		if (createEvent && Math.abs(w - previous) > eps) {
			pushupEvent.accept(new ChangeSingleFieldEvent(Event.EventType.CHANGE_W, this, previous, w));
		}
		destination.region.width = w;
	}

	public void setDstH(float h) {
		setDstH(h, true);
	}

	public void setDstH(float h, boolean createEvent) {
		float previous = this.getDstH();
		if (createEvent && Math.abs(h - previous) > eps) {
			pushupEvent.accept(new ChangeSingleFieldEvent(Event.EventType.CHANGE_H, this, previous, h));
		}
		destination.region.height = h;
	}

	/**
	 * Submit the move result, producing the event
	 */
	public void submitMovement() {
		if (beforeMove == null) {
			ImGuiNotify.error("Cannot submit the move result because there's no original position");
			return;
		}
		float nextX = getDstX();
		float nextY = getDstY();
		float nextW = getDstW();
		float nextH = getDstH();
		// Reset the position, to mimic that we are never left the original position
		setDstX(beforeMove.region.x, false);
		setDstY(beforeMove.region.y, false);
		setDstW(beforeMove.region.width, false);
		setDstH(beforeMove.region.height, false);
		// Truly move to the target position
		setDstX(nextX);
		setDstY(nextY);
		setDstW(nextW);
		setDstH(nextH);
		beforeMove = null;
	}
}
