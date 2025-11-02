package bms.player.beatoraja.ir;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.ScoreDatabaseAccessor;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.skin.property.StringPropertyFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.net.URI;
import java.time.Duration;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.ToIntFunction;

/**
 * Original repo from https://github.com/SayakaIsBaka/lr2ir-read-only
 *
 * @author Sayaka, Catizard
 * @implNote This class is not a real IR connection, but the original repo is. It keeps the
 * original form to make things easier
 */
public class LR2IRConnection {
	private static final String IRUrl = "http://dream-pro.info/~lavalse/LR2IR/2";
	private static ScoreDatabaseAccessor scoreDatabaseAccessor;

    private static Map<String, LeaderboardEntry[]> lr2IRRankingCache = new HashMap<>();

	public static void setScoreDatabaseAccessor(ScoreDatabaseAccessor scoreDatabaseAccessor) {
		LR2IRConnection.scoreDatabaseAccessor = scoreDatabaseAccessor;
	}

	private static Object convertXMLToObject(String xml, Class c) {
		try {
			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			Object res = xmlMapper.readValue(xml, c);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String makePOSTRequest(String uri, String data) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(IRUrl + uri);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Connection", "close");
			conn.setDoOutput(true);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(data.getBytes());
			}

			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("HTTP error code: " + responseCode);
			}

			try (InputStream is = conn.getInputStream();
				 BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("Shift_JIS")))) {

				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
					response.append(System.lineSeparator());
				}

				return response.toString();
			}
		} catch (Exception e) {
			ImGuiNotify.error("Failed to send request to LR2IR: " + e.getMessage());
			return null;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * Get LR2IR scores and personal score
	 *
	 * @param chart requested chart
	 * @implNote Technically speaking, this class shouldn't have the access of local scores. But this makes the code
	 * easier to assemble.
	 * @return A pair, first is local score and second is scores from LR2IR. The first can be null.
	 */
	public static Pair<IRScoreData, LeaderboardEntry[]> getScoreData(IRChartData chart) {
		if (chart.md5 == null || chart.md5.isEmpty()) {
			return new Pair<>(null, new LeaderboardEntry[0]);
		}
		LR2IRSongData lr2IRSongData = new LR2IRSongData(chart.md5, "114328");
		try {
            String requestURL = lr2IRSongData.toUrlEncodedForm();
            LeaderboardEntry[] scoreData;
            if (lr2IRRankingCache.containsKey(requestURL)) {
                scoreData = lr2IRRankingCache.get(requestURL);
            }
            else {
                String res = makePOSTRequest("/getrankingxml.cgi", requestURL);
                Ranking ranking = (Ranking)convertXMLToObject(res.substring(1).replace("<lastupdate></lastupdate>", ""), Ranking.class);
                scoreData = ranking.toBeatorajaScoreData(chart);
                lr2IRRankingCache.put(requestURL, scoreData);
            }
			ScoreData localScore = scoreDatabaseAccessor.getScoreData(chart.sha256, chart.hasUndefinedLN ? chart.lntype : 0);
			if (localScore != null) {
				// This is intentional behaivor, see IRScoreData's player definition
				// and how we use this feature in LeaderBoardBar
				localScore.setPlayer("");
			}
			return new Pair<>(localScore == null ? null : new IRScoreData(localScore), scoreData);
		} catch (Exception e) {
			e.printStackTrace();
			ImGuiNotify.error("Failed to get score data from LR2IR: " + e.getMessage());
			return new Pair<>(null, new LeaderboardEntry[0]);
		}
	}

    public static LR2GhostData getGhostData(String MD5, long scoreId) {
        String api = "/getghost.cgi?songmd5=" + MD5 + "&mode=top&targetid=" + scoreId;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                                      .uri(URI.create(IRUrl + api))
                                      .timeout(Duration.ofSeconds(5))
                                      .GET()
                                      .build();
            HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if(status != HttpURLConnection.HTTP_OK){
                throw new RuntimeException("Unexpected http response code: " + status);
            }

            String body = response.body();
            return LR2GhostData.parse(body);
        }
        catch (Exception e) {
            e.printStackTrace();
            ImGuiNotify.error(String.format("Failed to load ghost data."));
            return null;
        }
    }

	public static class LR2IRSongData {
		public String md5;
		public String id;
		public String lastUpdate;

		LR2IRSongData(String md5, String id) {
			this.md5 = md5;
			this.id = id;
			this.lastUpdate = "";
		}

		public String toUrlEncodedForm() {
			return "songmd5=" + md5 + "&id=" + id + "&lastupdate=" + lastUpdate;
		}
	}

	public static class Ranking {
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Score> score = new ArrayList<>();

		public List<Score> getScore() {
			return score;
		}

		public void setScore(List<Score> score) {
			this.score = score;
		}

		public LeaderboardEntry[] toBeatorajaScoreData(IRChartData model) {
			List<Score> scores = getScore();
			List<LeaderboardEntry> res = new ArrayList<>();
			for (Score s : scores) {
				ScoreData tmp = new ScoreData(model.mode);
				tmp.setSha256(model.sha256);
				tmp.setPlayer(s.getName());
				tmp.setClear(s.getBeatorajaClear());
				tmp.setNotes(s.getNotes());
				tmp.setCombo(s.getCombo());
				tmp.setEpg(s.getPg());
				tmp.setEgr(s.getGr());
				tmp.setMinbp(s.getMinbp());
                res.add(LeaderboardEntry.newEntryLR2IR(new IRScoreData(tmp), s.getId()));
			}
        /*if (lastScoreData != null && lastChart != null && lastChart.sha256.equals(model.sha256)) {
            System.out.println(lastScoreData.player);
            ScoreData tmp2 = new ScoreData(model.mode);
            tmp2.setSha256(model.sha256);
            tmp2.setPlayer(null);
            tmp2.setClear(lastScoreData.clear.id);
            tmp2.setNotes(lastScoreData.notes);
            tmp2.setCombo(lastScoreData.maxcombo);
            tmp2.setEpg(lastScoreData.epg);
            tmp2.setLpg(lastScoreData.lpg);
            tmp2.setEgr(lastScoreData.egr);
            tmp2.setLgr(lastScoreData.lgr);
            tmp2.setMinbp(lastScoreData.minbp);
            res.add(new IRScoreData(tmp2));
            lastScoreData = null;
            lastChart = null;
        } else*/
            ToIntFunction<LeaderboardEntry> leaderboardScore =
                (entry -> entry.getIrScore().getExscore());
            return res.stream()
                .sorted(Comparator.comparingInt(leaderboardScore).reversed())
                .toArray(LeaderboardEntry[]::new);
        }
	}

	public static class Score {
		private String name;
		private int id;
		private int clear;
		private int notes;
		private int combo;
		private int pg;
		private int gr;
		private int minbp;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getClear() {
			return clear;
		}

		public int getBeatorajaClear() {
			switch (clear) {
				case 1: // Failed
					return 1;
				case 2: // Easy
					return 4;
				case 3: // Groove
					return 5;
				case 4: // Hard
					return 6;
				case 5: // FC
					if (pg + gr == notes) // Perfect
						return 9;
					else
						return 8;
				default:
					return 0;
			}
		}

		public void setClear(int clear) {
			this.clear = clear;
		}

		public int getNotes() {
			return notes;
		}

		public void setNotes(int notes) {
			this.notes = notes;
		}

		public int getCombo() {
			return combo;
		}

		public void setCombo(int combo) {
			this.combo = combo;
		}

		public int getPg() {
			return pg;
		}

		public void setPg(int pg) {
			this.pg = pg;
		}

		public int getGr() {
			return gr;
		}

		public void setGr(int gr) {
			this.gr = gr;
		}

		public int getMinbp() {
			return minbp;
		}

		public void setMinbp(int minbp) {
			this.minbp = minbp;
		}
	}
}

