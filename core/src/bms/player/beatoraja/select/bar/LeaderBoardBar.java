package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.ir.IRChartData;
import bms.player.beatoraja.ir.IRResponse;
import bms.player.beatoraja.ir.IRScoreData;
import bms.player.beatoraja.ir.LR2IRConnection;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;
import javafx.util.Pair;

public class LeaderBoardBar extends DirectoryBar {
	private final SongData songData;
	private final String title;
	private final boolean fromLR2IR;

	public LeaderBoardBar(MusicSelector selector, SongData songData, boolean fromLR2IR) {
		super(selector);
		this.songData = songData;
		this.title = songData.getFullTitle();
		this.fromLR2IR = fromLR2IR;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Bar[] getChildren() {
		// NOTE: For further devs, the leaderboard's children is sorted by 'exscore', if you want to implement a
		// different sort strategy, you need to change two 'fromIRScoreData' implementation
		if (!fromLR2IR) {
			MainController.IRStatus pir = selector.main.getIRStatus()[0];
			IRResponse<IRScoreData[]> response = pir.connection.getPlayData(pir.player, new IRChartData(songData));
			if (!response.isSucceeded()) {
				ImGuiNotify.error(String.format("Failed to load ir leaderboard: %s",response.getMessage()));
				return new Bar[0];
			}
			IRScoreData[] irScoreData = response.getData();
			return IRPlayerBar.fromIRScoreData(irScoreData);
		} else {
			Pair<IRScoreData, IRScoreData[]> scores = LR2IRConnection.getScoreData(new IRChartData(songData));
			IRScoreData localScore = scores.getKey();
			IRScoreData[] scoreData = scores.getValue();
			if (localScore != null) {
				return IRPlayerBar.fromIRScoreData(localScore, scoreData);
			}
			return IRPlayerBar.fromIRScoreData(scoreData);
		}
	}
}
