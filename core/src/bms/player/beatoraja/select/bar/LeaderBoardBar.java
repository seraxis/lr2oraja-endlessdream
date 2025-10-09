package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.ir.IRChartData;
import bms.player.beatoraja.ir.IRResponse;
import bms.player.beatoraja.ir.IRScoreData;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;

public class LeaderBoardBar extends DirectoryBar {
	private final SongData songData;
	private final String title;

	public LeaderBoardBar(MusicSelector selector, SongData songData) {
		super(selector);
		this.songData = songData;
		this.title = songData.getFullTitle();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Bar[] getChildren() {
		MainController.IRStatus pir = selector.main.getIRStatus()[0];
		IRResponse<IRScoreData[]> response = pir.connection.getPlayData(pir.player, new IRChartData(songData));
		if (!response.isSucceeded()) {
			ImGuiNotify.error(String.format("Failed to load ir leaderboard: %s",response.getMessage()));
			return new Bar[0];
		}
		IRScoreData[] irScoreData = response.getData();
		return IRPlayerBar.fromIRScoreData(irScoreData);
	}
}
