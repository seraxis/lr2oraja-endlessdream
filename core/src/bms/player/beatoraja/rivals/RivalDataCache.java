package bms.player.beatoraja.rivals;

import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.ScoreDatabaseAccessor;
import bms.player.beatoraja.select.ScoreDataCache;
import bms.player.beatoraja.song.SongData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ScoreDataCache} intended for use with rivals. Adds methods to operate on underlying
 * database.
 */
public final class RivalDataCache extends ScoreDataCache {

	private static final Logger logger = LoggerFactory.getLogger(RivalDataCache.class);

    private final ScoreDatabaseAccessor scoreDb;

    public RivalDataCache(ScoreDatabaseAccessor scoreDb) {
        this.scoreDb = scoreDb;
    }

    @Override
    protected ScoreData readScoreDatasFromSource(SongData song, int lnmode) {
        return scoreDb.getScoreData(song.getSha256(), song.hasUndefinedLongNote() ? lnmode : 0);
    }

    @Override
    protected void readScoreDatasFromSource(
            ScoreDatabaseAccessor.ScoreDataCollector collector,
            SongData[] songs,
            int lnmode
    ) {
        logger.error("Unimplemented: this method is never called from rivals context");
    }

    public void updateScore(ScoreData scoreData, SongData songData, int lnMode) {
        scoreDb.setScoreData(scoreData);
        this.update(songData, lnMode);
    }

    public void updateAllScores(ScoreData[] scores) {
        scoreDb.setScoreData(scores);
        this.clear();
    }
}
