package bms.player.beatoraja;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

import bms.player.beatoraja.exceptions.PlayerConfigException;
import bms.player.beatoraja.ir.IRConnectionManager;
import bms.player.beatoraja.pattern.*;
import bms.player.beatoraja.play.GrooveGauge;
import bms.player.beatoraja.select.BarSorter;
import bms.player.beatoraja.skin.SkinType;

import bms.model.Mode;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;
import lombok.Getter;
import lombok.Setter;

/**
 * プレイヤー毎の設定項目
 *
 * @author exch
 */
@Getter
@Setter
public final class PlayerConfig {

	/**
	 * 旧コンフィグパス。そのうち削除
	 */
	static final Path configpath_old = Paths.get("config.json");
	/**
	 * コンフィグパス(UTF-8)
	 */
	static final Path configpath = Paths.get("config_player.json");	

	private String id;
    /**
     * プレイヤーネーム
     */
    private String name = "NO NAME";

	/**
	 * ゲージの種類
	 */
	private int gauge = 0;
	/**
	 * 譜面オプション
	 */
	private int random;
	/**
	 * 譜面オプション(2P)
	 */
	private int random2;

	/**
	 * DP用オプション
	 */
	private int doubleoption;
	
	private String chartReplicationMode = "RIVALCHART";

	/**
	 * スコアターゲット
	 */
	private String targetid = "MAX";
	
	private String[] targetlist = new String[] {"RATE_A-","RATE_A", "RATE_A+","RATE_AA-","RATE_AA", "RATE_AA+", "RATE_AAA-", "RATE_AAA", "RATE_AAA+", "RATE_MAX-", "MAX"
			,"RANK_NEXT", "IR_NEXT_1", "IR_NEXT_2", "IR_NEXT_3", "IR_NEXT_4", "IR_NEXT_5", "IR_NEXT_10"
			, "IR_RANK_1", "IR_RANK_5", "IR_RANK_10", "IR_RANK_20", "IR_RANK_30", "IR_RANK_40", "IR_RANK_50"
			, "IR_RANKRATE_5", "IR_RANKRATE_10", "IR_RANKRATE_15", "IR_RANKRATE_20", "IR_RANKRATE_25", "IR_RANKRATE_30", "IR_RANKRATE_35", "IR_RANKRATE_40", "IR_RANKRATE_45","IR_RANKRATE_50"
			,"RIVAL_RANK_1","RIVAL_RANK_2","RIVAL_RANK_3","RIVAL_NEXT_1","RIVAL_NEXT_2","RIVAL_NEXT_3"};
	/**
	 * 判定タイミング
	 */
	private int judgetiming = 0;
	
	public static final int JUDGETIMING_MAX = 500;
	public static final int JUDGETIMING_MIN = -500;
	
	/**
	 * ディスプレイ表示タイミング自動調整
	 */
	private boolean notesDisplayTimingAutoAdjust = false;

    /**
     * 選曲時のモードフィルター
     */
	private Mode mode = null;
	/**
	 * 指定がない場合のミスレイヤー表示時間(ms)
	 */
	private int misslayerDuration = 500;

	/**
	 * LNモード
	 */
	private int lnmode = 0;
	/**
	 * スクロール追加/削除モード
	 */
    private int scrollMode = 0;
    private int scrollSection = 4;
    private double scrollRate = 0.5;
	/**
	 * ロングノート追加/削除モード
	 */
    private int longnoteMode = 0;
    private double longnoteRate = 1.0;
	/**
	 * アシストオプション:カスタムジャッジ
	 */
	private boolean customJudge = false;
	private int keyJudgeWindowRatePerfectGreat = 400;
	private int keyJudgeWindowRateGreat = 400;
	private int keyJudgeWindowRateGood = 100;
	private int scratchJudgeWindowRatePerfectGreat = 400;
	private int scratchJudgeWindowRateGreat = 400;
	private int scratchJudgeWindowRateGood = 100;

	/**
	 * 地雷モード
	 */
	private int mineMode = 0;
	/**
	 * アシストオプション:BPMガイド
	 */
	private boolean bpmguide = false;

	private int extranoteType = 0;
	private int extranoteDepth = 0;
	private boolean extranoteScratch = false;

	private boolean showjudgearea = false;

	private boolean markprocessednote = false;

	/**
	 * H-RANDOM連打しきい値BPM
	 */
	private int hranThresholdBPM = 120;
	/**
	 * プレイ中のゲージ切替
	 */
	private int gaugeAutoShift = GAUGEAUTOSHIFT_NONE;
	/**
	 * GASで遷移可能なゲージの下限  ASSIST EASY, EASY, NORMALから選択
	 */
	private int bottomShiftableGauge = GrooveGauge.ASSISTEASY;

	public static final int GAUGEAUTOSHIFT_NONE = 0;
	public static final int GAUGEAUTOSHIFT_CONTINUE = 1;
	public static final int GAUGEAUTOSHIFT_SURVIVAL_TO_GROOVE = 2;
	public static final int GAUGEAUTOSHIFT_BESTCLEAR = 3;
	public static final int GAUGEAUTOSHIFT_SELECT_TO_UNDER = 4;

	private int autoSaveReplay[];

	/**
	 * 7to9 スクラッチ鍵盤位置関係 0:OFF 1:SC1KEY2~8 2:SC1KEY3~9 3:SC2KEY3~9 4:SC8KEY1~7 5:SC9KEY1~7 6:SC9KEY2~8
	 */
	private int sevenToNinePattern = 0;

	/**
	 * 7to9 スクラッチ処理タイプ 0:そのまま 1:連打回避 2:交互
	 */
	private int sevenToNineType = 0;

	/**
	 * START+SELECTを押すと終了するまでの時間
	 */
	private int exitPressDuration = 1000;

	/**
	 * Guide SE
	 */
	private boolean guideSE = false;

	/**
	 * Window Hold
	 */
	private boolean windowHold = false;
	
	/**
	 * Enable folder random select bar
	 */
	private boolean randomSelect = false;

	private SkinConfig[] skin = new SkinConfig[SkinType.getMaxSkinTypeID() + 1];
	private SkinConfig[] skinHistory;

	private PlayModeConfig mode5 = new PlayModeConfig(Mode.BEAT_5K);

	private PlayModeConfig mode7 = new PlayModeConfig(Mode.BEAT_7K);

	private PlayModeConfig mode10 = new PlayModeConfig(Mode.BEAT_10K);

	private PlayModeConfig mode14 = new PlayModeConfig(Mode.BEAT_14K);

	private PlayModeConfig mode9 = new PlayModeConfig(Mode.POPN_9K);

	private PlayModeConfig mode24 = new PlayModeConfig(Mode.KEYBOARD_24K);

	private PlayModeConfig mode24double = new PlayModeConfig(Mode.KEYBOARD_24K_DOUBLE);
	/**
	 * HIDDENノートを表示するかどうか
	 */
	private boolean showhiddennote = false;
	/**
	 * 通過ノートを表示するかどうか
	 */
	private boolean showpastnote = false;
	
	/**
	 * チャートプレビューを使用するかどうか
	 */
	private boolean chartPreview = true;
	
	/**
	 * 選択中の選曲時ソート
	 */
	private int sort;
	/**
	 * 選択中の選曲時ソート
	 */
	private String sortid;

	/**
	 * 選曲時でのキー入力方式
	 */
	private int musicselectinput = 0;

	private IRConfig[] irconfig;
	
	private String twitterConsumerKey;

	private String twitterConsumerSecret;

	private String twitterAccessToken;

	private String twitterAccessTokenSecret;

	// -- Stream
	private boolean requestEnabled = false;
	private boolean requestNotify = false;
	private int maxRequestCount = 30;

	private boolean eventMode = false;

	public PlayerConfig() {
		validate();
	}

	public PlayModeConfig getPlayConfig(Mode modeId) {
        return switch (modeId != null ? modeId : Mode.BEAT_7K) {
            case BEAT_5K -> getMode5();
            case BEAT_7K -> getMode7();
            case BEAT_10K -> getMode10();
            case BEAT_14K -> getMode14();
            case POPN_9K -> getMode9();
            case KEYBOARD_24K -> getMode24();
            case KEYBOARD_24K_DOUBLE -> getMode24double();
            default -> getMode7();
        };
	}

	public PlayModeConfig getPlayConfig(int modeId) {
        return switch (modeId) {
            case 5 -> getMode5();
			case 7 -> getMode7();
			case 10 -> getMode10();
			case 14 -> getMode14();
			case 9 -> getMode9();
			case 25 -> getMode24();
            case 50 -> getMode24double();
            default -> getMode7();
        };
	}

	public PlayModeConfig getMode10() {
		if(mode10 == null || mode10.getController().length < 2) {
			mode10 = new PlayModeConfig(Mode.BEAT_10K);
			Logger.getGlobal().warning("mode10のPlayConfigを再構成");
		}
		return mode10;
	}

	public PlayModeConfig getMode14() {
		if(mode14 == null || mode14.getController().length < 2) {
			mode14 = new PlayModeConfig(Mode.BEAT_14K);
			Logger.getGlobal().warning("mode14のPlayConfigを再構成");
		}
		return mode14;
	}

	public PlayModeConfig getMode24double() {
		if(mode24double == null || mode24double.getController().length < 2) {
			mode24double = new PlayModeConfig(Mode.KEYBOARD_24K_DOUBLE);
			Logger.getGlobal().warning("mode24doubleのPlayConfigを再構成");
		}
		return mode24double;
	}

	public SkinConfig[] getSkin() {
		if(skin.length <= SkinType.getMaxSkinTypeID()) {
			skin = Arrays.copyOf(skin, SkinType.getMaxSkinTypeID() + 1);
			Logger.getGlobal().warning("skinを再構成");
		}
		return skin;
	}

	public int getMisslayerDuration() {
		return Math.max(misslayerDuration, 0);
	}

	public void validate() {
		if(skin == null) {
			skin = new SkinConfig[SkinType.getMaxSkinTypeID() + 1];
		}
		if(skinHistory == null) {
			skinHistory = new SkinConfig[0];
		}
		if(skin.length != SkinType.getMaxSkinTypeID() + 1) {
			skin = Arrays.copyOf(skin, SkinType.getMaxSkinTypeID() + 1);
		}
		for(int i = 0;i < skin.length;i++) {
			if(skin[i] == null) {
				skin[i] = SkinConfig.getDefault(i);
			}
			skin[i].validate();
		}

		if(mode5 == null) {
			mode5 = new PlayModeConfig(Mode.BEAT_5K);
		}

		if(mode7 == null) {
			mode7 = new PlayModeConfig(Mode.BEAT_7K);
		}
		if(mode14 == null) {
			mode14 = new PlayModeConfig(Mode.BEAT_14K);
		}
		if(mode10 == null) {
			mode10 = new PlayModeConfig(Mode.BEAT_10K);
		}
		if(mode9 == null) {
			mode9 = new PlayModeConfig(Mode.POPN_9K);
		}
		if(mode24 == null) {
			mode24 = new PlayModeConfig(Mode.KEYBOARD_24K);
		}
		if(mode24double == null) {
			mode24double = new PlayModeConfig(Mode.KEYBOARD_24K_DOUBLE);
		}
		mode5.validate(7);
		mode7.validate(9);
		mode10.validate(14);
		mode14.validate(18);
		mode9.validate(9);
		mode24.validate(26);
		mode24double.validate(52);

		sort = MathUtils.clamp(sort, 0 , BarSorter.defaultSorter.length - 1);
		if(sortid == null) {
			sortid = BarSorter.defaultSorter[sort].name();
		}

		gauge = MathUtils.clamp(gauge, 0, 5);
		random = MathUtils.clamp(random, 0, 9);
		random2 = MathUtils.clamp(random2, 0, 9);
		doubleoption = MathUtils.clamp(doubleoption, 0, 3);
		chartReplicationMode = chartReplicationMode != null ? chartReplicationMode : "NONE";
		targetid = targetid!= null ? targetid : "MAX";
		targetlist = targetlist != null ? targetlist : new String[0];
		judgetiming = MathUtils.clamp(judgetiming, JUDGETIMING_MIN, JUDGETIMING_MAX);
		misslayerDuration = MathUtils.clamp(misslayerDuration, 0, 5000);
		lnmode = MathUtils.clamp(lnmode, 0, 2);
		keyJudgeWindowRatePerfectGreat = MathUtils.clamp(keyJudgeWindowRatePerfectGreat, 25, 400);
		keyJudgeWindowRateGreat = MathUtils.clamp(keyJudgeWindowRateGreat, 0, 400);
		keyJudgeWindowRateGood = MathUtils.clamp(keyJudgeWindowRateGood, 0, 400);
		scratchJudgeWindowRatePerfectGreat = MathUtils.clamp(scratchJudgeWindowRatePerfectGreat, 25, 400);
		scratchJudgeWindowRateGreat = MathUtils.clamp(scratchJudgeWindowRateGreat, 0, 400);
		scratchJudgeWindowRateGood = MathUtils.clamp(scratchJudgeWindowRateGood, 0, 400);
		hranThresholdBPM = MathUtils.clamp(hranThresholdBPM, 1, 1000);
		
		if(autoSaveReplay == null) {
			autoSaveReplay = new int[4];
		}
		if(autoSaveReplay.length != 4) {
			autoSaveReplay = Arrays.copyOf(autoSaveReplay, 4);
		}
		sevenToNinePattern = MathUtils.clamp(sevenToNinePattern, 0, 6);
		sevenToNineType = MathUtils.clamp(sevenToNineType, 0, 2);
		exitPressDuration = MathUtils.clamp(exitPressDuration, 0, 100000);

		scrollMode = MathUtils.clamp(scrollMode, 0, ScrollSpeedModifier.Mode.values().length);
		scrollSection = MathUtils.clamp(scrollSection, 1, 1024);
		scrollRate = MathUtils.clamp(scrollRate, 0, 1.0);
		longnoteMode = MathUtils.clamp(longnoteMode, 0, LongNoteModifier.Mode.values().length);
		longnoteRate = MathUtils.clamp(longnoteRate, 0.0, 1.0);
		mineMode = MathUtils.clamp(mineMode, 0, MineNoteModifier.Mode.values().length);
		extranoteDepth = MathUtils.clamp(extranoteDepth, 0, 100);

		if(irconfig == null || irconfig.length == 0) {
			String[] irnames = IRConnectionManager.getAllAvailableIRConnectionName();
			irconfig = new IRConfig[irnames.length];
			for(int i = 0;i < irnames.length;i++) {
				irconfig[i] = new IRConfig();
				irconfig[i].setIrname(irnames[i]);
			}
		}
		
		for(int i = 0;i < irconfig.length;i++) {
			if(irconfig[i] == null || irconfig[i].getIrname() == null) {
				continue;
			}
			for(int j = i + 1;j < irconfig.length;j++) {
				if(irconfig[j] != null && irconfig[i].getIrname().equals(irconfig[j].getIrname())) {
					irconfig[j].setIrname(null);
				}				
			}
		}
		irconfig = Validatable.removeInvalidElements(irconfig);

		// --Stream
		maxRequestCount = MathUtils.clamp(maxRequestCount, 0, 100);
	}

	public static void init(Config config) throws PlayerConfigException {
		// TODO プレイヤーアカウント検証
		if(!Files.exists(Paths.get(config.getPlayerpath()))) {
			createDirectory(Paths.get(config.getPlayerpath()));
		}

		if(readAllPlayerID(config.getPlayerpath()).length == 0) {
			create(config.getPlayerpath(), "player1");

			// スコアデータコピー
			Path parentPlayerScoreDBPath = Paths.get("playerscore.db");
			if(Files.exists(parentPlayerScoreDBPath)) {
                try {
                    Files.copy(parentPlayerScoreDBPath, Paths.get(config.getPlayerpath() + "/player1/score.db"));
                } catch (IOException e) {
					Logger.getGlobal().severe(String.format("Failed to copy playerscore.db to %s: %s", config.getPlayerpath(), e.getLocalizedMessage()));
                }
            }

			// リプレイデータコピー
			copyReplays(config);

			config.setPlayername("player1");
		} else {
			readPlayerConfig(config.getPlayerpath(), config.getPlayername());
		}
	}

	private static void createDirectory(Path path) {
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            Logger.getGlobal().severe(String.format("Failed to create directory at %s: %s", path, e.getLocalizedMessage()));
        }
    }

	private static void copyReplays(Config config) {
		Path player1ReplayDir = Paths.get(config.getPlayerpath() + "/player1/replay");
		Path parentReplayDir = Paths.get("replay");

		createDirectory(player1ReplayDir);
		if(!Files.exists(parentReplayDir)) {
			// nothing to copy
			return;
		}

		try (DirectoryStream<Path> paths = Files.newDirectoryStream(parentReplayDir)) {
			for (Path p : paths) {
				Files.copy(p, player1ReplayDir.resolve(p.getFileName()));
			}
		} catch(Throwable e) {
			Logger.getGlobal().warning("Error while copying replays: " + e.getLocalizedMessage());
		}
	}

	public static void create(String playerpath, String playerid) {
		try {
			Path p = Paths.get(playerpath + "/" + playerid);
			if(Files.exists(p)) {
				return;
			}
			Files.createDirectory(p);
			PlayerConfig player = new PlayerConfig();
			player.setId(playerid);
			write(playerpath, player);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String[] readAllPlayerID(String playerpath) {
		List<String> l = new ArrayList<>();
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(playerpath))) {
			for (Path p : paths) {
				if(Files.isDirectory(p)) {
					l.add(p.getFileName().toString());
				}
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return l.toArray(new String[l.size()]);
	}

	public static PlayerConfig readPlayerConfig(String playerpath, String playerid) throws PlayerConfigException {
		PlayerConfig player = new PlayerConfig();
		final Path path = Paths.get(playerpath + "/" + playerid + "/" + configpath);
		final Path path_old = Paths.get(playerpath + "/" + playerid + "/" + configpath_old);

		if (Files.exists(path)) {
			player = loadPlayerConfig(playerpath, playerid, path);
		} else if(Files.exists(path_old)) {
			// 旧コンフィグ読み込み。そのうち削除
			player = loadPlayerConfigFromOldPath(path_old);
		}

		return validatePlayerConfig(playerid, player);
	}

	public static PlayerConfig validatePlayerConfig(String playerid, PlayerConfig player) {
		player.setId(playerid);
		player.validate();
		return player;
	}

	private static PlayerConfig loadPlayerConfig(String playerpath, String playerid, Path path) throws PlayerConfigException {
		PlayerConfig player;
		try (Reader reader = new InputStreamReader(Files.newInputStream(path.toFile().toPath()), StandardCharsets.UTF_8)) {
			Json json = new Json();
			json.setIgnoreUnknownFields(true);
			player = json.fromJson(PlayerConfig.class, reader);
		} catch (SerializationException e) {
			writeBackupConfigFile(playerpath, playerid, path);
			throw new PlayerConfigException("PlayerConfigの読み込み失敗 - Path : " + path + " , Log : " + e.getLocalizedMessage());
		} catch (IOException e) {
			throw new PlayerConfigException("Failed to load player config file: " + e.getLocalizedMessage());
		}
		return player;
	}

	private static PlayerConfig loadPlayerConfigFromOldPath(Path path_old) throws PlayerConfigException {
		PlayerConfig player;
		try (FileReader reader = new FileReader(path_old.toFile())) {
			Json json = new Json();
			json.setIgnoreUnknownFields(true);
			player = json.fromJson(PlayerConfig.class, reader);
		} catch (Throwable e) {
			throw new PlayerConfigException("Failed to load player config file: " + e.getLocalizedMessage());
		}
		return player;
	}

	private static void writeBackupConfigFile(String playerpath, String playerid, Path path) {
		try {
			Path configBackupPath = Paths.get(playerpath + "/" + playerid + "/config_backup.json");
			Files.copy(path, configBackupPath, StandardCopyOption.REPLACE_EXISTING);
			Logger.getGlobal().info("Backup config written to " + configBackupPath);
		} catch (IOException e) {
			Logger.getGlobal().severe("Failed to write backup config file: " + e.getLocalizedMessage());
		}
	}

	public static void write(String playerpath, PlayerConfig player) {
		try (Writer writer = new OutputStreamWriter(
				new FileOutputStream(Paths.get(playerpath + "/" + player.getId() + "/" + configpath).toFile()), StandardCharsets.UTF_8)) {
			Json json = new Json();
			json.setOutputType(JsonWriter.OutputType.json);
			json.setUsePrototypes(false);
			writer.write(json.prettyPrint(player));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
