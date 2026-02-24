package bms.player.beatoraja;

import java.sql.*;
import java.util.*;

import bms.player.beatoraja.select.QueryScoreContext;
import bms.player.beatoraja.song.SongData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.SynchronousMode;
import org.sqlite.SQLiteDataSource;

import javax.management.Query;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * スコアデータログデータベースアクセサ
 * 
 * @author omi
 */
public class ScoreDataLogDatabaseAccessor extends SQLiteDatabaseAccessor {
	private static final Logger logger = LoggerFactory.getLogger(ScoreDataLogDatabaseAccessor.class);

	private SQLiteDataSource ds;
	private final ResultSetHandler<List<ScoreData>> scoreHandler = new BeanListHandler<ScoreData>(ScoreData.class);

	private final QueryRunner qr;
	private static final int LOAD_CHUNK_SIZE = 1000;

	public ScoreDataLogDatabaseAccessor(String path) throws ClassNotFoundException {
		super( new Table("scoredatalog",
						new Column("sha256", "TEXT", 1, 1),
						new Column("mode", "INTEGER",0,1),
						new Column("clear", "INTEGER"),
						new Column("epg", "INTEGER"),
						new Column("lpg", "INTEGER"),
						new Column("egr", "INTEGER"),
						new Column("lgr", "INTEGER"),
						new Column("egd", "INTEGER"),
						new Column("lgd", "INTEGER"),
						new Column("ebd", "INTEGER"),
						new Column("lbd", "INTEGER"),
						new Column("epr", "INTEGER"),
						new Column("lpr", "INTEGER"),
						new Column("ems", "INTEGER"),
						new Column("lms", "INTEGER"),
						new Column("notes", "INTEGER"),
						new Column("combo", "INTEGER"),
						new Column("minbp", "INTEGER"),
						new Column("avgjudge", "INTEGER", 1, 0, String.valueOf(Integer.MAX_VALUE)),
						new Column("playcount", "INTEGER"),
						new Column("clearcount", "INTEGER"),
						new Column("trophy", "TEXT"),
						new Column("ghost", "TEXT"),
						new Column("option", "INTEGER"),
						new Column("seed", "INTEGER"),
						new Column("random", "INTEGER"),
						new Column("date", "INTEGER"),
						new Column("state", "INTEGER"),
						new Column("scorehash", "TEXT")
				),
				new Table( "eddatalog",
						new Column("sha256", "TEXT", 1, 0),
						new Column("mode", "INTEGER"),
						new Column("clear", "INTEGER"),
						new Column("epg", "INTEGER"),
						new Column("lpg", "INTEGER"),
						new Column("egr", "INTEGER"),
						new Column("lgr", "INTEGER"),
						new Column("egd", "INTEGER"),
						new Column("lgd", "INTEGER"),
						new Column("ebd", "INTEGER"),
						new Column("lbd", "INTEGER"),
						new Column("epr", "INTEGER"),
						new Column("lpr", "INTEGER"),
						new Column("ems", "INTEGER"),
						new Column("lms", "INTEGER"),
						new Column("notes", "INTEGER"),
						new Column("combo", "INTEGER"),
						new Column("minbp", "INTEGER"),
						new Column("avgjudge", "INTEGER", 1, 0, String.valueOf(Integer.MAX_VALUE)),
						new Column("playcount", "INTEGER"),
						new Column("clearcount", "INTEGER"),
						new Column("trophy", "TEXT"),
						new Column("ghost", "TEXT"),
						new Column("option", "INTEGER"),
						new Column("seed", "INTEGER"),
						new Column("random", "INTEGER"),
						new Column("date", "INTEGER"),
						new Column("state", "INTEGER"),
						new Column("scorehash", "TEXT"),
						new Column("rate", "INTEGER"),
						new Column("overridejudge", "INTEGER")
				));

		Class.forName("org.sqlite.JDBC");
		SQLiteConfig conf = new SQLiteConfig();
		conf.setSharedCache(true);
		conf.setSynchronous(SynchronousMode.OFF);
		// conf.setJournalMode(JournalMode.MEMORY);
		ds = new SQLiteDataSource(conf);
		ds.setUrl("jdbc:sqlite:" + path);
		qr = new QueryRunner(ds);
		
		try {
			this.validate(qr);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setScoreDataLog(ScoreData score) {
		setScoreDataLog(new ScoreData[] { score });
	}

	public void setScoreDataLog(ScoreData[] scores) {
		try (Connection con = qr.getDataSource().getConnection()) {
			con.setAutoCommit(false);
			for (ScoreData score : scores) {
				this.insert(qr, con, "scoredatalog", score);
				this.insert(qr, con, "eddatalog", score);
			}
			con.commit();
		} catch (Exception e) {
			logger.error("スコア更新時の例外:{}", e.getMessage());
		}
	}

	public List<ScoreData> getScoreDataLog(String sha256) {
		List<ScoreData> result = null;
		try {
			// TODO: One day we shall use prepared statement instead
			result = Validatable.removeInvalidElements(qr.query(String.format("SELECT * FROM eddatalog WHERE sha256 = '%s'", sha256), scoreHandler));
		} catch (Exception e) {
			logger.error("Failed to query table eddatalog: {}", e.getMessage());
		}
		return result;
	}

	public List<ScoreData> getScoreDataLog(String sha256, QueryScoreContext ctx) {
		List<ScoreData> result = null;
		try (Connection con = qr.getDataSource().getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM eddatalog WHERE sha256 = ? AND rate = ? AND overridejudge = ?");
			ps.setString(1, sha256);
			ps.setInt(2, ctx.freqValue() != null ? ctx.freqValue() : 0);
			ps.setInt(3, ctx.overrideJudge() != null ? ctx.overrideJudge() : -1);
			result = Validatable.removeInvalidElements(scoreHandler.handle(ps.executeQuery()));
		} catch (Exception e) {
			logger.error("Failed to query table eddatalog: {}", e.getMessage());
		}
		return result;
	}

	/**
	 * TODO: Maybe we should make the definition of "best" programmable?
	 */
	public ScoreData getBestScoreDataLog(String sha256, QueryScoreContext ctx) {
		List<ScoreData> rawLogs = getScoreDataLog(sha256, ctx);
		if (rawLogs.isEmpty()) {
			return null;
		}
		ScoreData result = rawLogs.get(0);
		for (ScoreData score : rawLogs) {
			if (score.getClear() > result.getClear()) {
				result = score;
			} else if (score.getClear() == result.getClear() && score.getExscore() > result.getExscore()) {
				result = score;
			}
		}
		return result;
	}

	public void getBestScoreDataLogs(ScoreDatabaseAccessor.ScoreDataCollector collector, SongData[] songs, QueryScoreContext ctx) {
		StringBuilder str = new StringBuilder(songs.length * 68);
		getBestScoreDataLogs(collector, songs, ctx.lnMode(), str, true, ctx);
		str.setLength(0);
		getBestScoreDataLogs(collector, songs, 0, str, false, ctx);
	}

	public void getBestScoreDataLogs(ScoreDatabaseAccessor.ScoreDataCollector collector, SongData[] songs, int mode, StringBuilder str, boolean hasLN, QueryScoreContext ctx) {
		try (Connection con = qr.getDataSource().getConnection()) {
			int songLength = songs.length;
			int chunkLength = (songLength + LOAD_CHUNK_SIZE - 1) / LOAD_CHUNK_SIZE;
			List<ScoreData> scores = new ArrayList<>();
			for (int i = 0; i < chunkLength;++i) {
				// [i * CHUNK_SIZE, min(length, (i + 1) * CHUNK_SIZE)
				final int chunkStart = i * LOAD_CHUNK_SIZE;
				final int chunkEnd = Math.min(songLength, (i + 1) * LOAD_CHUNK_SIZE);
				for (int j = chunkStart; j < chunkEnd; ++j) {
					SongData song = songs[j];
					if((hasLN && song.hasUndefinedLongNote()) || (!hasLN && !song.hasUndefinedLongNote())) {
						if (str.length() > 0) {
							str.append(',');
						}
						str.append('\'').append(song.getSha256()).append('\'');
					}
				}

				PreparedStatement ps = con.prepareStatement("SELECT * FROM eddatalog WHERE sha256 in (?) AND mode = ? AND rate = ? AND overridejudge = ?");
				ps.setString(1, str.toString());
				ps.setInt(2, mode);
				ps.setInt(3, ctx.freqValue() != null ? ctx.freqValue() : 0);
				ps.setInt(4, ctx.overrideJudge() != null ? ctx.overrideJudge() : -1);

				List<ScoreData> subScores = Validatable.removeInvalidElements(scoreHandler.handle(ps.executeQuery()));
				str.setLength(0);
				scores.addAll(subScores);
			}
			scores.sort(Comparator.comparing(ScoreData::getSha256));
			// For every chart, we calculate it's best score
			List<ScoreData> bestScores = new ArrayList<>();
			for (int i = 0; i <scores.size(); ++i) {
				int j = i;
				ScoreData bestScore = scores.get(i);
				while (j + 1 < scores.size() && scores.get(j + 1).getSha256().equals(bestScore.getSha256())) {
					ScoreData next = scores.get(j + 1);
					if (next.getClear() > bestScore.getClear()) {
						bestScore = next;
					} else if (next.getClear() == bestScore.getClear() && next.getExscore() > bestScore.getExscore()) {
						bestScore = next;
					}
					j++;
				}
				bestScores.add(bestScore);
				i = j;
			}
			for(SongData song : songs) {
				if((hasLN && song.hasUndefinedLongNote()) || (!hasLN && !song.hasUndefinedLongNote())) {
					boolean b = true;
					for (ScoreData score : bestScores) {
						if(song.getSha256().equals(score.getSha256())) {
							collector.collect(song, score);
							b = false;
							break;
						}
					}
					if(b) {
						collector.collect(song, null);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Failed to query table eddatalog: {}", e.getMessage());
		}
	}
}
