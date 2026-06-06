package bms.player.beatoraja.external;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import bms.model.Mode;
import bms.player.beatoraja.ClearType;
import bms.player.beatoraja.ReplayData;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.pattern.Random;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.stream.NamedPipeSender;

public final class OrajaHelperClient {
	private static final String PIPE_NAME = "oraja_helper";
	private static final NamedPipeSender SENDER = new NamedPipeSender(PIPE_NAME);
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "oraja-helper-pipe-sender");
		thread.setDaemon(true);
		return thread;
	});
	private static final Json JSON = new Json();

	static {
		JSON.setOutputType(OutputType.json);
	}

	private OrajaHelperClient() {
	}

	public static void sendSelect(SongData song) {
		Map<String, Object> payload = basePayload("song_select", "select", song);
		send(payload);
	}

	public static void sendPlay(SongData song, ReplayData replay, Mode mode) {
		Map<String, Object> payload = basePayload("song_play", "play", song);
		addOption(payload, replay, mode);
		send(payload);
	}

	public static void sendResult(SongData song, ReplayData replay, Mode mode, ScoreData score) {
		if (score == null) {
			return;
		}
		Map<String, Object> payload = basePayload("song_result", "result", song);
		addOption(payload, replay, mode);
		payload.put("score", score.getExscore());
		payload.put("scoreRate", scoreRate(score, song));
		payload.put("clearLamp", ClearType.getClearTypeByID(score.getClear()).name());
		payload.put("clearLampId", score.getClear());
		payload.put("missCount", score.getMinbp());
		payload.put("judges", judgePayload(score));
		send(payload);
	}

	private static Map<String, Object> basePayload(String event, String scene, SongData song) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("event", event);
		payload.put("scene", scene);
		payload.put("title", song != null ? song.getFullTitle() : "");
		payload.put("artist", song != null ? song.getFullArtist() : "");
		payload.put("sha256", song != null ? song.getSha256() : "");
		payload.put("md5", song != null ? song.getMd5() : "");
		return payload;
	}

	private static void addOption(Map<String, Object> payload, ReplayData replay, Mode mode) {
		if (replay == null || mode == null) {
			return;
		}
		Random option = Random.getRandom(replay.randomoption, mode);
		payload.put("option", optionName(option));
		payload.put("optionId", replay.randomoption);
		payload.put("randomSeed", replay.randomoptionseed);
		payload.put("randomPlacement", lanePlacement(replay.laneShufflePattern, 0, mode));
		if (mode.player == 2) {
			Random option2p = Random.getRandom(replay.randomoption2, mode);
			payload.put("option2P", optionName(option2p));
			payload.put("option2PId", replay.randomoption2);
			payload.put("randomSeed2P", replay.randomoption2seed);
			payload.put("randomPlacement2P", lanePlacement(replay.laneShufflePattern, 1, mode));
			payload.put("doubleOption", replay.doubleoption);
		}
	}

	private static Map<String, Object> judgePayload(ScoreData score) {
		Map<String, Object> judges = new LinkedHashMap<>();
		judges.put("epg", score.getEpg());
		judges.put("lpg", score.getLpg());
		judges.put("egr", score.getEgr());
		judges.put("lgr", score.getLgr());
		judges.put("egd", score.getEgd());
		judges.put("lgd", score.getLgd());
		judges.put("ebd", score.getEbd());
		judges.put("lbd", score.getLbd());
		judges.put("epr", score.getEpr());
		judges.put("lpr", score.getLpr());
		judges.put("ems", score.getEms());
		judges.put("lms", score.getLms());
		return judges;
	}

	private static float scoreRate(ScoreData score, SongData song) {
		int notes = song != null ? song.getNotes() : 0;
		return notes > 0 ? score.getExscore() * 100.0f / (notes * 2.0f) : 0.0f;
	}

	private static String lanePlacement(int[][] patterns, int player, Mode mode) {
		if (patterns == null || player < 0 || player >= patterns.length || patterns[player] == null) {
			return "";
		}
		int[] pattern = patterns[player];
		int length = pattern.length;
		if (mode.scratchKey.length > player && length > 0 && pattern[length - 1] == mode.scratchKey[player]) {
			length--;
		}
		StringBuilder placement = new StringBuilder();
		for (int i = 0; i < length; i++) {
			placement.append(pattern[i] + 1);
		}
		return placement.toString();
	}

	private static String optionName(Random option) {
		return switch (option) {
			case IDENTITY -> "正規";
			case ROTATE -> "R-RANDOM";
			case S_RANDOM -> "S-RANDOM";
			case H_RANDOM -> "H-RANDOM";
			case ALL_SCR -> "ALL-SCR";
			case MIRROR_EX -> "MIRROR-EX";
			case RANDOM_EX -> "RANDOM-EX";
			case ROTATE_EX -> "R-RANDOM-EX";
			case S_RANDOM_EX -> "S-RANDOM-EX";
			case S_RANDOM_NO_THRESHOLD -> "S-RANDOM-NO-THRESHOLD";
			case RANDOM_PLAYABLE -> "RANDOM-PLAYABLE";
			case S_RANDOM_PLAYABLE -> "S-RANDOM-PLAYABLE";
			default -> option.name().replace('_', '-');
		};
	}

	private static void send(Map<String, Object> payload) {
		String body = JSON.toJson(payload);
		EXECUTOR.execute(() -> SENDER.sendLine(body));
	}
}
