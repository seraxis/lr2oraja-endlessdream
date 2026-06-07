package bms.player.beatoraja.rivals;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.PlayerInformation;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.ScoreDatabaseAccessor;
import bms.player.beatoraja.external.ScoreDataImporter;
import bms.player.beatoraja.ir.IRPlayerData;
import bms.player.beatoraja.ir.IRResponse;
import bms.player.beatoraja.ir.IRScoreData;
import bms.player.beatoraja.song.SongData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Single source of truth about all rival data. All operations such as setting and retrieving rivals, as well as their
 * scores should go through this accessor.
 */
public final class RivalDataAccessor {
    private static final Logger logger = LoggerFactory.getLogger(RivalDataAccessor.class);

    /**
     * ライバル情報とスコアデータキャッシュ
     */
    private final List<Rival> rivals = new ArrayList<>();

    /**
     * Currently selected rival.
     */
    private Rival currentRival = null;

    /**
     * ライバル情報を取得する
     *
     * @param index インデックス
     * @return ライバル情報
     */
    @Nullable
    public PlayerInformation getRivalInformation(int index) {
        return index >= 0 && index < rivals.size() ? rivals.get(index).rivalInformation : null;
    }

    /**
     * ライバル数を取得する
     *
     * @return ライバル数
     */
    public int getRivalCount() {
        return rivals.size();
    }

    /**
     * Switches to next rival and returns relevant {@link PlayerInformation}, or null if current was the last one.
     *
     * @return next rival's {@link PlayerInformation} or null
     */
    @Nullable
    public PlayerInformation nextRival() {
        if (rivals.isEmpty()) {
            return null;
        }

        if (currentRival == null) {
            currentRival = rivals.get(0);
            return currentRival.rivalInformation;
        } else {
            var index = rivals.indexOf(currentRival);
            var lastIndex = rivals.size() - 1;
            if (index >= lastIndex) {
                currentRival = null;
                return null;
            } else {
                currentRival = rivals.get(index + 1);
                return currentRival.rivalInformation;
            }
        }
    }

    /**
     * @return true if any rival is currently selected
     */
    public boolean isRivalSelected() {
        return currentRival != null;
    }

    /**
     * Retrieves {@link ScoreData} for currently selected rival.
     *
     * @param songData
     * @param lnMode
     * @return {@link ScoreData} or null if no rival is selected
     */
    @Nullable
    public ScoreData getCurrentRivalScore(SongData songData, int lnMode) {
        if (currentRival != null) {
            return currentRival.getScore(songData, lnMode);
        } else {
            return null;
        }
    }

    /**
     * Retrieves {@link ScoreData} for a rival by his index in rivals list.
     *
     * @param index  rival's index
     * @param song
     * @param lnMode
     * @return {@link ScoreData} or null if no score/rival found
     */
    @Nullable
    public ScoreData getRivalScore(int index, SongData song, int lnMode) {
        return getRival(index).map(rival -> rival.getScore(song, lnMode)).orElse(null);
    }

    /**
     * Retrieves all rivals' scores for a given chart.
     *
     * @param song
     * @param lnMode
     * @return {@link List} of rivals' scores
     */
    public List<ScoreData> getAllRivalsScores(SongData song, int lnMode) {
        return rivals.stream()
                .map(rival -> rival.getScore(song, lnMode))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Updates all rivals' scores for a given chart.
     *
     * @param scores
     * @param songData
     * @param lnMode
     */
    public void updateAllRivalsScores(IRScoreData[] scores, SongData songData, int lnMode) {
        for (var rival : rivals) {
            Arrays.stream(scores)
                    .filter(score -> score.player.equals(rival.rivalInformation.getName()))
                    .findFirst()
                    .map(IRScoreData::convertToScoreData)
                    .ifPresent(score -> rival.rivalDataCache.updateScore(score, songData, lnMode));
        }
    }

    public void update(MainController main) {
        if (main.getIRStatus().length > 0) {
            // TODO 別のクラスに移動
            if (main.getIRStatus()[0].config.isImportscore()) {
                main.getIRStatus()[0].config.setImportscore(false);
                try {
                    IRResponse<IRScoreData[]> scores = main.getIRStatus()[0].connection.getPlayData(main.getIRStatus()[0].player, null);
                    if (scores.isSucceeded()) {
                        ScoreDataImporter scoreimport = new ScoreDataImporter(new ScoreDatabaseAccessor(main.getConfig().getPlayerpath() + File.separatorChar + main.getConfig().getPlayername() + File.separatorChar + "score.db"));
                        scoreimport.importScores(convert(scores.getData()), main.getIRStatus()[0].config.getIrname());

                        logger.info("IRからのスコアインポート完了");
                    } else {
                        logger.warn("IRからのスコアインポート失敗 : {}", scores.getMessage());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            IRResponse<IRPlayerData[]> response = main.getIRStatus()[0].connection.getRivals();
            if (response.isSucceeded()) {
                try {
                    // ライバルスコアデータベース作成
                    var rivalsPath = Paths.get("rival");
                    if (!Files.exists(rivalsPath)) {
                        Files.createDirectory(rivalsPath);
                    }

                    var irName = main.getIRStatus()[0].config.getIrname();
                    var irRivalDatabases = createIrRivalDatabases(rivalsPath, response.getData(), irName);
                    rivals.addAll(irRivalDatabases);

                    var existingRivalIds = rivals.stream().map(rival -> rival.rivalInformation().getId()).toList();
                    var localRivalDatabases = loadRemainingLocalDatabases(rivalsPath, existingRivalIds, irName);
                    rivals.addAll(localRivalDatabases);

                    if (main.getIRStatus()[0].config.isImportrival()) {
                        for (IRPlayerData irPlayer : response.getData()) {
                            new Thread(() -> {
                                var rivalInformation = irPlayer.toPlayerInformation();
                                IRResponse<IRScoreData[]> scores = main.getIRStatus()[0].connection.getPlayData(irPlayer, null);
                                if (scores.isSucceeded()) {
                                    rivals.stream().filter(rival -> rival.rivalInformation.equals(rivalInformation))
                                            .findFirst()
                                            .ifPresent(rival ->
                                                    rival.rivalDataCache().updateAllScores(convert(scores.getData())));
                                    logger.info("IRからのライバルスコア取得完了 : {}", rivalInformation.getName());
                                } else {
                                    logger.warn("IRからのライバルスコア取得失敗 : {}", scores.getMessage());
                                }
                            }).start();
                        }
                    }

//					Array<String> targets = new Array<String>(TargetProperty.getTargets());
//					for(int i = 0;i < this.rivals.length;i++) {
//						targets.add("RIVAL_" + (i + 1));
//					}
//					TargetProperty.setTargets(targets.toArray(String.class));

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                logger.warn("IRからのライバル取得失敗 : {}", response.getMessage());
            }
        }
    }

    private List<Rival> createIrRivalDatabases(Path rivalsPath, IRPlayerData[] rivalsData, String irName) {
        List<Rival> irRivals = new ArrayList<>();
        for (var irPlayer : rivalsData) {
            var rivalInformation = irPlayer.toPlayerInformation();
            try {
                var scoreDb = new ScoreDatabaseAccessor(
                        rivalsPath.toString() + File.separatorChar + irName + rivalInformation.getId() + ".db"
                );
                scoreDb.createTable();
                scoreDb.setInformation(rivalInformation);
                var rivalCache = new RivalDataCache(scoreDb);
                irRivals.add(new Rival(rivalInformation, rivalCache));
                logger.info("Created rival score DB for rival from IR: {}", rivalInformation.getName());
            } catch (ClassNotFoundException e) {
                logger.error("SQLite JDBC driver not found", e);
            }
        }
        return irRivals;
    }

    private List<Rival> loadRemainingLocalDatabases(
            Path rivalsPath,
            Collection<String> existingRivalIds,
            String irName
    ) {
        List<Rival> localRivals = new ArrayList<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(rivalsPath)) {
            for (Path p : paths) {
                var exists = existingRivalIds.stream()
                        .anyMatch(rivalId -> p.getFileName().toString().equals(irName + rivalId + ".db"));
                if (exists) {
                    continue;
                }

                if (p.toString().endsWith(".db")) {
                    try {
                        var scoreDb = new ScoreDatabaseAccessor(p.toString());
                        var rivalInformation = scoreDb.getInformation();
                        if (rivalInformation != null) {
                            var rivalCache = new RivalDataCache(scoreDb);
                            localRivals.add(new Rival(rivalInformation, rivalCache));
                            logger.info("Loaded local rival score DB: {}", rivalInformation.getName());
                        }
                    } catch (ClassNotFoundException e) {
                        logger.error("SQLite JDBC driver not found", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("IO error while loading local rival scores", e);
        }
        return localRivals;
    }

    private ScoreData[] convert(IRScoreData[] irscores) {
        return Stream.of(irscores).map(IRScoreData::convertToScoreData).toArray(ScoreData[]::new);
    }

    private Optional<Rival> getRival(int index) {
        return index >= 0 && index < rivals.size() ? Optional.of(rivals.get(index)) : Optional.empty();
    }

    private record Rival(PlayerInformation rivalInformation, RivalDataCache rivalDataCache) {

        private ScoreData getScore(SongData song, int lnMode) {
            return rivalDataCache.readScoreData(song, lnMode);
        }
    }
}
