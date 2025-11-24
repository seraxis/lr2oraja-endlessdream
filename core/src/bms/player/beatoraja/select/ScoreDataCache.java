package bms.player.beatoraja.select;

import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.ScoreDatabaseAccessor.ScoreDataCollector;
import bms.player.beatoraja.song.SongData;
import com.badlogic.gdx.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * スコアデータのキャッシュ
 *
 * @implNote In an ideal future, we should replace the old query functions with the QueryScoreContext ones
 * @author exch
 */
public abstract class ScoreDataCache {

    // TODO ResourcePoolベースに移行する

    /**
     * スコアデータのキャッシュ
     */
    private ObjectMap<String, ScoreData>[] scorecache;
    /**
     * Modded scores' cache
     */
    private HashMap<QueryScoreContext, ObjectMap<String, ScoreData>> moddedScoreCache;

    public ScoreDataCache() {
        scorecache = new ObjectMap[4];
        for (int i = 0; i < scorecache.length; i++) {
            scorecache[i] = new ObjectMap(2000);
        }
        moddedScoreCache = new HashMap<>();
    }

    /**
     * 指定した楽曲データ、LN MODEに対するスコアデータを返す
     * @param song 楽曲データ
     * @param lnmode LN MODE
     * @return スコアデータ。存在しない場合はnull
     */
    public ScoreData readScoreData(SongData song, int lnmode) {
        final int cacheindex = song.hasUndefinedLongNote() ? lnmode : 3;
        if (scorecache[cacheindex].containsKey(song.getSha256())) {
            return scorecache[cacheindex].get(song.getSha256());
        }
        ScoreData score = readScoreDatasFromSource(song, lnmode);
        scorecache[cacheindex].put(song.getSha256(), score);
        return score;
    }

    /**
     * Query specified one song's best score
     */
    public ScoreData readScoreData(SongData song, QueryScoreContext ctx) {
        Optional<ScoreData> cachedScore = readFromModdedCache(song.getSha256(), ctx);
        if (cachedScore.isPresent()) {
            return cachedScore.get();
        }
        ScoreData score = readScoreDatasFromSource(song, ctx);
        moddedScoreCache.putIfAbsent(ctx, new ObjectMap<>());
        moddedScoreCache.get(ctx).put(song.getSha256(), score);
        return score;
    }

    /**
     *
     * @param collector
     * @param songs
     * @param lnmode
     */
    public void readScoreDatas(ScoreDataCollector collector, SongData[] songs, int lnmode) {
        // キャッシュからの抽出
        Array<SongData> noscore = null;
        for (SongData song : songs) {
            final int cacheindex = song.hasUndefinedLongNote() ? lnmode : 3;

            if (scorecache[cacheindex].containsKey(song.getSha256())) {
                collector.collect(song, scorecache[cacheindex].get(song.getSha256()));
            } else {
            	if(noscore == null) {
            		noscore = new Array<SongData>();
            	}
                noscore.add(song);
            }
        }

        if(noscore == null) {
            return;
        }
        // キャッシュに存在しなかったスコアデータをキャッシュに登録
        final SongData[] noscores = noscore.toArray(SongData.class);

        final ScoreDataCollector cachecollector = (song, score) -> {
            final int cacheindex = song.hasUndefinedLongNote() ? lnmode : 3;
            scorecache[cacheindex].put(song.getSha256(), score);
        	collector.collect(song, score);
        };
        readScoreDatasFromSource(cachecollector, noscores, lnmode);
    }

    public void readScoreDatas(ScoreDataCollector collector, SongData[] songs, QueryScoreContext ctx) {
        List<SongData> lost = new ArrayList<>();
        for (SongData song : songs) {
            Optional<ScoreData> optScoreData = readFromModdedCache(song.getSha256(), ctx);
            if (optScoreData.isPresent()) {
                collector.collect(song, optScoreData.get());
            } else {
                lost.add(song);
            }
        }
        if (lost.isEmpty()) {
            return ;
        }
        ScoreDataCollector cacheCollector = (song, score) -> {
            moddedScoreCache.putIfAbsent(ctx, new ObjectMap<>());
            moddedScoreCache.get(ctx).put(song.getSha256(), score);
            collector.collect(song, score);
        };
        readScoresDatasFromSource(cacheCollector, lost.toArray(SongData[]::new), ctx);
    }

    boolean existsScoreDataCache(SongData song, int lnmode) {
        final int cacheindex = song.hasUndefinedLongNote() ? lnmode : 3;
        return scorecache[cacheindex].containsKey(song.getSha256());
    }

    boolean existsScoreDataCache(SongData song, QueryScoreContext ctx) {
        if (!moddedScoreCache.containsKey(ctx)) {
            return false;
        }
        return moddedScoreCache.get(ctx).containsKey(song.getSha256());
    }

    public void clear() {
        for (ObjectMap<?, ?> cache : scorecache) {
            cache.clear();
        }
    }

    public void update(SongData song, int lnmode) {
        final int cacheindex = song.hasUndefinedLongNote() ? lnmode : 3;
        ScoreData score = readScoreDatasFromSource(song, lnmode);
        scorecache[cacheindex].put(song.getSha256(), score);
    }

    public void update(SongData song, QueryScoreContext ctx) {
        moddedScoreCache.putIfAbsent(ctx, new ObjectMap<>());
        ScoreData score = readScoreDatasFromSource(song, ctx);
        moddedScoreCache.get(ctx).put(song.getSha256(), score);
    }

    protected abstract ScoreData readScoreDatasFromSource(SongData songs, int lnmode);

    protected abstract void readScoreDatasFromSource(ScoreDataCollector collector, SongData[] songs, int lnmode);

    // NOTE: Below two functions are not abstract, and they're default to call the old functions
    // This is a compromised way to keep the compatibility with old apis
    protected ScoreData readScoreDatasFromSource(SongData song, QueryScoreContext ctx) {
        return readScoreDatasFromSource(song, ctx.lnMode());
    }

    protected void readScoresDatasFromSource(ScoreDataCollector collector, SongData[] songs, QueryScoreContext ctx) {
        readScoreDatasFromSource(collector, songs, ctx.lnMode());
    }

    private Optional<ScoreData> readFromModdedCache(String sha256, QueryScoreContext ctx) {
        if (!moddedScoreCache.containsKey(ctx)) {
            return Optional.empty();
        }
        return Optional.ofNullable(moddedScoreCache.get(ctx).get(sha256));
    }
}