package bms.player.beatoraja.modmenu.setting.widget;

import java.util.function.Supplier;

public class TiledOption<V> implements Widget {
	private final Supplier<V> getter;
	// We're holding a reference to action here it's because the type inside Tile is a super-class(SizedWidget)
	//  and Tile doesn't expose the action field out ¯\_(ツ)_/¯
	private final ActionWidget<V> action;
	private final Tile tile;

	public TiledOption(String optionName, Supplier<V> getter, ActionWidget<V> action) {
		this.getter = getter;
		this.action = action;
		action.update(getter.get());
		this.tile = new Tile(optionName, action);
	}

	public TiledOption<V> addIcon(Label icon) {
		this.tile.addIcon(icon);
		return this;
	}

	public TiledOption<V> addDescription(String description) {
		this.tile.addDescription(description);
		return this;
	}

	@Override
	public void render() {
		this.tile.render();
	}

	public void refresh() {
		action.update(getter.get());
	}
}
