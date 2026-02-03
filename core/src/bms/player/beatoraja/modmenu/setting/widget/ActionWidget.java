package bms.player.beatoraja.modmenu.setting.widget;

import java.util.function.Consumer;

public abstract class ActionWidget<T> implements SizedWidget {
	protected Consumer<T> setter;

	public ActionWidget(Consumer<T> setter) {
		this.setter = setter;
	}

	protected abstract void update(T value);
}
