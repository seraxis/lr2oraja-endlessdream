package bms.player.beatoraja.modmenu.setting.window;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.setting.widget.Label;
import bms.player.beatoraja.modmenu.setting.widget.TiledOption;
import bms.tool.util.Pair;

import java.util.List;

public abstract class TiledOptionBasedWindow extends BaseSettingWindow {
	private final String name;

	public TiledOptionBasedWindow(String name, Config config, PlayerConfig playerConfig) {
		super(config, playerConfig);
		this.name = name;
	}

	abstract protected List<Pair<String, List<TiledOption<?>>>> getOptions();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void render() {
		getOptions().forEach(category -> {
			Label.categoryLabel(category.getFirst()).render();
			category.getSecond().forEach(TiledOption::render);
		});
	}

	@Override
	public void refresh() {
		getOptions().forEach(category -> category.getSecond().forEach(TiledOption::refresh));
	}
}
