package bms.player.beatoraja.select.bar;

import java.util.Arrays;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.ir.IRChartData;
import bms.player.beatoraja.ir.IRResponse;
import bms.player.beatoraja.ir.IRScoreData;
import bms.player.beatoraja.ir.LeaderboardEntry;
import bms.player.beatoraja.ir.LR2IRConnection;
import bms.player.beatoraja.ir.LR2GhostData;
import bms.player.beatoraja.play.GhostBattlePlay;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.skin.property.StringPropertyFactory;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.BMSPlayerMode;
import bms.model.Mode;
import javafx.util.Pair;

import static bms.player.beatoraja.select.bar.FunctionBar.STYLE_COURSE;
import static bms.player.beatoraja.select.bar.FunctionBar.STYLE_TABLE;

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
            LeaderboardEntry[] leaderboard = Arrays.stream(irScoreData)
                                                 .map(LeaderboardEntry::newEntryPrimaryIR)
                                                 .toArray(LeaderboardEntry[] ::new);
            return fromIRScoreData(leaderboard);
		} else {
			Pair<IRScoreData, LeaderboardEntry[]> scores = LR2IRConnection.getScoreData(new IRChartData(songData));
			IRScoreData localScore = scores.getKey();
			LeaderboardEntry[] scoreData = scores.getValue();
			if (localScore != null) {
				return fromIRScoreData(localScore, scoreData);
			}
			return fromIRScoreData(scoreData);
		}
	}

	/**
	 * Convert some ir scores to bars
	 *
	 * @param irScoreData ir scores, should be ordered by exscore. More specifically, the score has larger exscore
	 *                    should be positioned before a smaller one
	 * @implNote IRScoreData's player field would be an empty string when it represents the player's own score
	 * @return bars
	 */
	public FunctionBar[] fromIRScoreData(LeaderboardEntry[] irScoreData) {
		FunctionBar[] bars = new FunctionBar[irScoreData.length];
		for (int i = 0; i < irScoreData.length; i++) {
			bars[i] = createFunctionBar(i + 1, irScoreData[i], irScoreData[i].getIrScore().player.isEmpty());
		}
		return bars;
	}

	/**
	 * Convert some ir scores to bars and insert the local score into. Used for
	 *
	 * @param localScore  local score, would be inserted into scores
	 * @param leaderboard ir scores, should be ordered by exscore. More specifically, the score has larger exscore
	 *                    should be positioned before a smaller one
	 * @return bars
	 * @implNote This function is the reason why ir scores need to be sorted before the function call, because we need
	 * to insert the local score into it. An alternative idea is marking the local score so we can mix them and sort,
	 * and tell which one is the local score to give it a specific bar type. However, this needs to change the original
	 * class fields to achieve. (But actually, this isn't impossible if we have a good pair type implementation, we can
	 * attach a boolean with score and sort by score's exscore)
	 */
	public FunctionBar[] fromIRScoreData(IRScoreData localScore, LeaderboardEntry[] leaderboard) {
		FunctionBar[] bars = new FunctionBar[leaderboard.length + 1];
		int id = 0;
		boolean inserted = false;
        if (leaderboard.length == 0 || localScore.getExscore() > leaderboard[0].getIrScore().getExscore()) {
            id++;
			bars[0] = createFunctionBar(id, LeaderboardEntry.newEntryPrimaryIR(localScore), true);
			inserted = true;
		}
        for (int i = 0; i < leaderboard.length; i++) {
			LeaderboardEntry entry = leaderboard[i];
			IRScoreData score = entry.getIrScore();
			bars[id] = createFunctionBar(id + 1, entry, false);
			id++;
            if (!inserted && score.getExscore() > localScore.getExscore() &&
                (i == leaderboard.length - 1 || leaderboard[i + 1].getIrScore().getExscore() <= localScore.getExscore())) {
                bars[id] = createFunctionBar(id + 1, LeaderboardEntry.newEntryPrimaryIR(localScore), true);
				id++;
				inserted = true;
			}
		}
		if (!inserted) {
			bars[id] = createFunctionBar(id, LeaderboardEntry.newEntryPrimaryIR(localScore), true);
		}
		return bars;
	}

	/**
	 * Create a single function bar
	 *
	 * @param rank        score's place, started from 1
	 * @param entry       leaderboard entry containing the score
	 * @param isSelfScore whether 'score' is from local or not, a local score would be rendered differently
	 * @return a function bar, see below comments
	 */
	private FunctionBar createFunctionBar(int rank, LeaderboardEntry entry, boolean isSelfScore) {
        IRScoreData scoreData = entry.getIrScore();
		FunctionBar irScoreBar = new FunctionBar((selector, self) -> {
            if (!entry.isLR2IR()) { return; }

            if (songData.getBMSModel().getMode() != Mode.BEAT_7K) {
                ImGuiNotify.warning("LR2IR Ghost battle is currently only supported for 7K.");
            }

            LR2GhostData ghost = LR2IRConnection.getGhostData(songData.getMd5(), entry.getLR2Id());
            // ghost might be null in case of issues with fetching or parsing
            // the ghost data; whatever caused the error should have already
            // been reported by this point
            if (ghost == null) { return; }

            // the LR2 ghost data may be too short in case of quitting out
            // we throw in extra poors here to account for this
            // (beatoraja will refuse to replay a pacemaker ghost if it
            //  isn't exactly the right length)
            int expectedNotes = songData.getNotes();
            int[] judgements = ghost.getJudgements();
            if (expectedNotes > judgements.length) {
                int[] padded = new int[expectedNotes];
                for (int i = 0; i < expectedNotes; ++i) {
                    padded[i] = (i < judgements.length) ? judgements[i] : 4;
                }
                judgements = padded;
            }
            else if (expectedNotes < ghost.getJudgements().length) {
                // haven't seen this happen yet
                ImGuiNotify.error("Malformed LR2IR ghost data received.");
                return;
            }

            var target = new ScoreData();
            target.setPlayer(scoreData.player);
            // lazy way to ensure EX score matches
            // would doing this properly matter at all?
            // (todo: check if skins can display this)
            target.setEpg(ghost.getPgreat());
            target.setEgr(ghost.getGreat());
            target.setEgd(ghost.getGood());
            target.setEbd(ghost.getBad());
            target.setEpr(ghost.getPoor());
            // the ScoreData object needs to know how many note there are
            // to properly decode the ghost later
            target.setNotes(judgements.length);
            target.encodeGhost(judgements);
            var play = new SongBar(songData);
            play.setRivalScore(target);

            GhostBattlePlay.setup(ghost.getRandom(), ghost.getLaneOrder());

            selector.selectSong(BMSPlayerMode.PLAY);
            selector.readChart(songData, play);
		},
            String.format("%d. %s", rank, isSelfScore ? getCurrentPlayerName() : scoreData.player),
            isSelfScore ? STYLE_COURSE : STYLE_TABLE
		);

        irScoreBar.setScore(scoreData.convertToScoreData());
        irScoreBar.setLamp(scoreData.clear.id);
        return irScoreBar;
    }

	private String getCurrentPlayerName() {
		return StringPropertyFactory.getStringProperty(StringPropertyFactory.StringType.player.name())
				.get(super.selector.main.getCurrentState());
	}
}
