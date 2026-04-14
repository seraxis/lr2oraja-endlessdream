package bms.player.beatoraja.modmenu.skin.debugger;

import bms.player.beatoraja.modmenu.skin.debugger.events.ToggleVisibleEvent;
import bms.player.beatoraja.skin.SkinObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * A simple wrapper class of SkinObject
 *
 * @implNote setter functions must provide an extra argument to not trigger event system
 */
public class SkinWidget {
	public final String name;
	public final List<SkinWidgetDestination> destinations;

	private final SkinObject skinObject;
	private final Consumer<Event<?>> pushupEvent;

	public SkinWidget(String name, SkinObject skinObject, List<SkinWidgetDestination> destinations,  Consumer<Event<?>> pushupEvent) {
		this.name = name;
		this.skinObject = skinObject;
		this.destinations = destinations;
		this.pushupEvent = pushupEvent;
	}

	public boolean isDrawingOnScreen() {
		return skinObject.draw && skinObject.visible;
	}

	public void toggleVisible() {
		toggleVisible(true);
	}

	public void toggleVisible(boolean createEvent) {
		boolean isVisibleBefore = skinObject.visible;
		if (createEvent) {
			pushupEvent.accept(new ToggleVisibleEvent(this, isVisibleBefore));
		}
		skinObject.visible = !isVisibleBefore;
	}
}
