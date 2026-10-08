package bms.player.beatoraja;

import bms.model.*;
import bms.player.beatoraja.CourseData.CourseDataConstraint;
import bms.player.beatoraja.TableData.TableFolder;
import bms.player.beatoraja.audio.AudioDriver;
import bms.player.beatoraja.audio.BMSLoudnessAnalyzer;
import bms.player.beatoraja.ir.RankingData;
import bms.player.beatoraja.play.BMSPlayerRule;
import bms.player.beatoraja.play.GrooveGauge;
import bms.player.beatoraja.play.bga.BGAProcessor;
import bms.player.beatoraja.song.SongData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Future;

/**
 * プレイヤーのコンポーネント間でデータをやり取りするためのクラス
 *
 * @author exch
 */
public final class PlayerResource {
	private static final Logger logger = LoggerFactory.getLogger(PlayerResource.class);
	
	/**
	 * 選曲中のBMS
	 */
	private BMSModel model;
	
	private long marginTime;
	/**
	 * 選択中のBMSの情報
	 */
	private SongData songdata;
	/**
	 * BMSModelの元々のモード
	 */
	private bms.model.Mode orgmode;

	private PlayerData playerdata = new PlayerData();

	private Config config;
	private PlayerConfig pconfig;
	/**
	 * プレイモード
	 */
	private BMSPlayerMode mode;
	
	private BMSResource bmsresource;

	/**
	 * スコア
	 */
	private ScoreData score;
	/**
	 * ライバルスコア
	 */
	private ScoreData rscore;
	/**
	 * ターゲットスコア
	 */
	private ScoreData tscore;
	
	private RankingData ranking;
	/**
	 * スコア更新するかどうか
	 */
	private boolean updateScore = true;
	private boolean updateCourseScore = true;
	private GrooveGauge grooveGauge;
	/**
	 * ゲージの遷移ログ
	 */
	private FloatArray[] gauge;

	private ReplayData replay;
	
	private ReplayData chartOption;

	private Path[] bmsPaths;
	private SongData[] autoPlaySongs;
	private boolean loop;
	
	/**
	 * コース
	 */
	private CourseData coursedata;
	/**
	 * 読み込み済みのコース
	 */
	private LoadedCourse course;
	/**
	 * コース何曲目
	 */
	private int courseindex;
	/**
	 * コースゲージ履歴
	 */
	private Array<FloatArray[]> coursegauge = new Array<FloatArray[]>();

	private Array<ReplayData> courseReplay = new Array<ReplayData>();
	/**
	 * コーススコア
	 */
	private ScoreData cscore;
	/**
	 * コンボ数。コースプレイ時の引継ぎに使用
	 */
	private int combo;
	/**
	 * 最大コンボ数。コースプレイ時の引継ぎに使用
	 */
	private int maxcombo;
	/**
	 * 元々のゲージオプション
	 */
	private int orgGaugeOption = 0;

	private int assist = 0;
	/**
	 * 現在プレイしている楽曲を含む難易度表とレベル
	 */
	private String tablename = "";
	private String tablelevel = "";
	private String tablefull;
	private boolean freqOn;
	private String freqString;
	private boolean forceNoIRSend;
	// Full list of difficult tables that contains current song
	private List<String> reverseLookup = new ArrayList<>();

	private final BMSLoudnessAnalyzer loudnessAnalyzer;
	private Future<BMSLoudnessAnalyzer.AnalysisResult> analysisTask;

	public PlayerResource(AudioDriver audio, Config config, PlayerConfig pconfig, BMSLoudnessAnalyzer loudnessAnalyzer) {
		this.config = config;
		this.pconfig = pconfig;
		this.bmsresource = new BMSResource(audio, config, pconfig);
		this.orgGaugeOption = pconfig.getGauge();
		this.loudnessAnalyzer = loudnessAnalyzer;
	}

	public void clear() {
		course = null;
		courseindex = 0;
		cscore = null;
		score = null;
//		rscore = null;
		tscore = null;
		gauge = null;
		courseReplay.clear();
		coursegauge.clear();
		combo = 0;
		maxcombo = 0;
		bmsPaths = null;
		autoPlaySongs = null;
		setTablename("");
		setTablelevel("");
	}

	public boolean setBMSFile(final Path f, BMSPlayerMode mode) {
		SongData song = new SongData();
		song.setPath(f.toString());
		return setBMSFile(song, mode);
	}

	public boolean setBMSFile(SongData song, BMSPlayerMode mode) {
		this.songdata = song;
		if(song == null) return false;
		// TODO play mode, リプレイデータでの読み込み分岐をここで行う
		this.mode = mode;
		replay = new ReplayData();
		model = loadBMSModel(song, pconfig.getLnmode(), null);
		if (model == null) {
			logger.warn("楽曲が存在しないか、解析時にエラーが発生しました:{}", song.chartFilename());
			return false;
		}
		if (model.getAllTimeLines().length == 0) {
			return false;
		}

		orgmode = model.getMode();
		songdata.setBMSModel(model);
		bmsresource.setBMSFile(model, songdata, config, mode);
		// TODO 選曲の時点で表名、フォルダ名を補完しておきたい
		if(tablename.length() == 0 || courseindex != 0){
			setTableinfo();
		}
		if (songdata.filesystemPath().isPresent() && config.getAudioConfig().isNormalizeVolume()
				&& loudnessAnalyzer != null && loudnessAnalyzer.isAvailable()) {
			analysisTask = loudnessAnalyzer.analyzeAsync(model);
		} else {
			analysisTask = null;
		}
		return true;
	}

	public BMSModel loadBMSModel(Path f, int lnmode) {
		return loadBMSModel(new ChartInformation(f, lnmode, null));
	}

	public BMSModel loadBMSModel(SongData song, int lnmode) {
		return loadBMSModel(song, lnmode, null);
	}

	public BMSModel loadBMSModel(SongData song, int lnmode, int[] selectedRandom) {
		if(song == null) return null;
		Path path = song.filesystemPath().orElse(null);
		if(path != null) return loadBMSModel(new ChartInformation(path, lnmode, selectedRandom));
		String filename = song.chartFilename();
		if(filename == null) return null;
		ChartDecoder decoder = ChartDecoder.getDecoder(Path.of(filename));
		if (decoder == null) return null;
		try {
			byte[] data = song.chartData().orElse(null);
			if (data == null) return null;
			BMSModel model;
			if (decoder instanceof BMSDecoder bms) {
				model = bms.decode(data, filename.toLowerCase().endsWith(".pms"), selectedRandom);
			} else if (decoder instanceof BMSONDecoder bmson) {
				model = bmson.decode(data, selectedRandom);
			} else if (decoder instanceof OSUDecoder osu) {
				model = osu.decode(data, selectedRandom);
			} else {
				return null;
			}
			return prepareModel(model, decoder);
		} catch (Exception error) {
			logger.warn("Failed to decode {}: {}", filename, error.getMessage());
			return null;
		}
	}

	public BMSModel loadBMSModel(int[] selectedRandom) {
		return loadBMSModel(songdata, pconfig.getLnmode(), selectedRandom);
	}

	public BMSModel loadBMSModel(ChartInformation info) {
		ChartDecoder decoder = ChartDecoder.getDecoder(info.path);
		if(decoder == null) {
			return null;
		}
		BMSModel model = decoder.decode(info);
		return prepareModel(model, decoder);
	}

	private BMSModel prepareModel(BMSModel model, ChartDecoder decoder) {
		if (model == null) {
			return null;
		}
		if (decoder instanceof OSUDecoder) {
			model.setFromOSU(true);
		}

		marginTime = BMSModelUtils.setStartNoteTime(model, 1000);
		BMSPlayerRule.validate(model);

		// 地雷ノートに爆発音が定義されていない場合、デフォルト爆発音をセットする
		final int lanes = model.getMode().key;
		final int wavcount = model.getWavList().length;
		for (TimeLine tl : model.getAllTimeLines()) {
			for (int i = 0; i < lanes; i++) {
				final Note n = tl.getNote(i);
				if (n != null) {
					if (n instanceof MineNote && n.getWav() < 0) {
						n.setWav(wavcount);
					}
				}
			}
		}

		return model;
	}

	public BMSModel getBMSModel() {
		return model;
	}
	
	public long getMarginTime() {
		return marginTime;
	}

	public BMSPlayerMode getPlayMode() {
		return mode;
	}

	public void setPlayMode(BMSPlayerMode mode) {
		this.mode = mode;
	}

	public Config getConfig() {
		return config;
	}

	public PlayerConfig getPlayerConfig() {
		return pconfig;
	}

	public BGAProcessor getBGAManager() {
		return bmsresource.getBGAProcessor();
	}

	public boolean mediaLoadFinished() {
		return bmsresource.mediaLoadFinished();
	}

	public ScoreData getScoreData() {
		return score;
	}

	public void setScoreData(ScoreData score) {
		this.score = score;
	}

	public ScoreData getRivalScoreData() {
		return rscore;
	}

	public void setRivalScoreData(ScoreData rscore) {
		this.rscore = rscore;
	}
	
	public ScoreData getTargetScoreData() {
		return tscore;
	}

	public void setTargetScoreData(ScoreData tscore) {
		this.tscore = tscore;
	}
	
	public RankingData getRankingData() {
		return ranking;
	}
	
	public void setRankingData(RankingData ranking) {
		this.ranking = ranking;
	}
	
	public boolean setCourseBMSFiles(Path[] files) {
		SongData[] songs = new SongData[files.length];
		for (int i = 0; i < files.length; i++) {
			songs[i] = new SongData();
			songs[i].setPath(files[i].toString());
		}
		return setCourseBMSFiles(songs);
	}

	public boolean setCourseBMSFiles(SongData[] songs) {
		CourseEntry[] entries = new CourseEntry[songs.length];
		for (int i = 0; i < songs.length; i++) {
			BMSModel courseModel = loadBMSModel(songs[i], pconfig.getLnmode(), null);
			if (courseModel == null) return false;
			entries[i] = new CourseEntry(songs[i], courseModel);
		}
		course = new LoadedCourse(entries);
		updateCourseScore = true;
		return true;
	}

	public BMSModel[] getCourseBMSModels() {
		return course != null ? course.models : null;
	}

	public void setAutoPlaySongs(Path[] paths, boolean loop) {
		this.bmsPaths = paths;
		this.autoPlaySongs = null;
		this.loop = loop;
	}

	public void setAutoPlaySongs(SongData[] songs, boolean loop) {
		this.autoPlaySongs = songs.clone();
		this.bmsPaths = null;
		this.loop = loop;
	}
	
	public boolean nextSong() {
		if(bmsPaths == null && autoPlaySongs == null) {
			return false;
		}
		int songCount = autoPlaySongs != null ? autoPlaySongs.length : bmsPaths.length;
		final int orgindex = courseindex;
		do {
			if(courseindex == songCount) {
				if(loop) {
					courseindex = 0;
				} else {
					return false;
				}
			}
			SongData next = autoPlaySongs != null ? autoPlaySongs[courseindex] : null;
			songdata = next;
			boolean loaded = next != null
					? setBMSFile(next, BMSPlayerMode.AUTOPLAY)
					: setBMSFile(bmsPaths[courseindex], BMSPlayerMode.AUTOPLAY);
			courseindex++;
			if(loaded) {
				return true;
			};
		} while(orgindex != courseindex);
		return false;
	}
	
	public boolean nextCourse() {
		courseindex++;
		if (courseindex == course.entries.length) {
			return false;
		} else {
			setBMSFile(course.entries[courseindex].song(), mode);
			return true;
		}
	}

	public int getCourseIndex() {
		return courseindex;
	}

	public void reloadBMSFile() {
		if (model != null) model = loadBMSModel(model.getRandom());
		final String name = tablename;
		final String lev = tablelevel;
		clear();
		tablename = name;
		tablelevel = lev;
	}

	public FloatArray[] getGauge() {
		return gauge;
	}

	public void setGauge(FloatArray[] gauge) {
		this.gauge = gauge;
	}

	public GrooveGauge getGrooveGauge() {
		return grooveGauge;
	}

	public void setGrooveGauge(GrooveGauge grooveGauge) {
		this.grooveGauge = grooveGauge;
	}

	public ReplayData getReplayData() {
		return replay;
	}

	public void setReplayData(ReplayData replay) {
		this.replay = replay;
	}

	public ScoreData getCourseScoreData() {
		return cscore;
	}

	public void setCourseScoreData(ScoreData cscore) {
		this.cscore = cscore;
	}

	public boolean isUpdateScore() {
		return updateScore;
	}

	public void setUpdateScore(boolean b) {
		this.updateScore = b;
	}

	public boolean isUpdateCourseScore() {
		return updateCourseScore;
	}

	public void setUpdateCourseScore(boolean updateCourseScore) {
		this.updateCourseScore = updateCourseScore;
	}

	public CourseData getCourseData() {
		return coursedata;
	}

	public void setCourseData(CourseData coursedata) {
		this.coursedata = coursedata;
	}
	
	public String getCoursetitle() {
		return coursedata != null ? coursedata.getName() : null;
	}
	
	public CourseDataConstraint[] getConstraint() {
		return coursedata != null ? coursedata.getConstraint() : new CourseDataConstraint[0];
	}

	public ReplayData[] getCourseReplay() {
		return courseReplay.toArray(ReplayData.class);
	}

	public void addCourseReplay(ReplayData rd) {
		courseReplay.add(rd);
	}

	public Array<FloatArray[]> getCourseGauge() {
		return coursegauge;
	}

	public void addCourseGauge(FloatArray[] gauge) {
		coursegauge.add(gauge);
	}

	public int getCombo() {
		return combo;
	}

	public void setCombo(int combo) {
		this.combo = combo;
	}

	public int getMaxcombo() {
		return maxcombo;
	}

	public void setMaxcombo(int maxcombo) {
		this.maxcombo = maxcombo;
	}

	public void dispose() {
		if(bmsresource != null) {
			bmsresource.dispose();
			bmsresource = null;
		}
	}

	public SongData getSongdata() {
		return songdata;
	}

	public void setSongdata(SongData songdata) {
		this.songdata = songdata;
	}

	public BMSResource getBMSResource() {
		return bmsresource;
	}
	
	public int getOrgGaugeOption() {
		return orgGaugeOption;
	}

	public void setOrgGaugeOption(int orgGaugeOption) {
		this.orgGaugeOption = orgGaugeOption;
	}

	private record CourseEntry(SongData song, BMSModel model) {}

	private static final class LoadedCourse {
		private final CourseEntry[] entries;
		private final BMSModel[] models;

		private LoadedCourse(CourseEntry[] entries) {
			this.entries = entries;
			this.models = new BMSModel[entries.length];
			for (int i = 0; i < entries.length; i++) models[i] = entries[i].model();
		}
	}

	public int getAssist() {
		return assist;
	}

	public void setAssist(int assist) {
		this.assist = assist;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
		this.tablefull = null;
	}

	public String getTablelevel() {
		return tablelevel;
	}

	public void setTablelevel(String tablelevel) {
		this.tablelevel = tablelevel;
		this.tablefull = null;
	}

	public String getTableFullname() {
		if(tablefull == null) {
			tablefull = tablelevel + tablename;
		}
		return tablefull;
	}


	public PlayerData getPlayerData() {
		return playerdata;
	}

	public void setPlayerData(PlayerData playerdata) {
		this.playerdata = playerdata;
	}

	private void setTableinfo(){
		final String[] urls = this.getConfig().getTableURL();
		final TableDataAccessor tdaccessor = new TableDataAccessor(config.getTablepath());
		final TableData[] tds = tdaccessor.readAll();
		for(String url: urls){
			for(TableData td: tds){
				if(td.getUrl().equals(url)){
					final TableFolder[] tfs = td.getFolder();
					for(TableFolder tf: tfs){
						final SongData[] tss = tf.getSong();
						for(SongData ts: tss){
							if((ts.getMd5().length() != 0 && this.getSongdata().getMd5().length() != 0 &&
									ts.getMd5().equals(this.getSongdata().getMd5()))||
									(ts.getMd5().length() != 0 && this.getSongdata().getMd5().length() != 0 &&
									ts.getSha256().equals(this.getSongdata().getSha256()))){
								setTablename(td.getName());
								setTablelevel(tf.getName());
								return;
							}
						}
					}
				}
			}
		}
		setTablename("");
		setTablelevel("");
	}

	public List<String> getReverseLookupData() {
		Set<String> urlSet = new HashSet<>(List.of(this.getConfig().getTableURL()));
		TableDataAccessor tdaccessor = new TableDataAccessor(config.getTablepath());
		TableData[] tds = tdaccessor.readAll();
		List<String> reverseLookup = new ArrayList<>();
		for (TableData td : tds) {
			if (!urlSet.contains(td.getUrl())) {
				continue;
			}
			TableFolder[] tfs = td.getFolder();
			boolean found = false;
			for (TableFolder tf : tfs) {
				SongData[] tss = tf.getSong();
				for (SongData ts : tss) {
					boolean matchOnMd5 = !ts.getMd5().isEmpty() && ts.getMd5().equals(this.getSongdata().getMd5());
					boolean matchOnSha256 = !ts.getSha256().isEmpty() && ts.getSha256().equals(this.getSongdata().getSha256());
					if (matchOnMd5 || matchOnSha256) {
						found = true;
						break;
					}
				}
				if (found) {
					reverseLookup.add(td.getName() + " " + tf.getName());
					break;
				}
			}
		}
		return reverseLookup;
	}

    public List<String> getReverseLookupLevels() {
        Set<String> urlSet = new HashSet<>(List.of(this.getConfig().getTableURL()));
        TableDataAccessor tdaccessor = new TableDataAccessor(config.getTablepath());
        TableData[] tds = tdaccessor.readAll();
        List<String> reverseLookup = new ArrayList<>();
        for (TableData td : tds) {
            if (!urlSet.contains(td.getUrl())) {
                continue;
            }
            TableFolder[] tfs = td.getFolder();
            boolean found = false;
            for (TableFolder tf : tfs) {
                SongData[] tss = tf.getSong();
                for (SongData ts : tss) {
                    boolean matchOnMd5 = !ts.getMd5().isEmpty() && ts.getMd5().equals(this.getSongdata().getMd5());
                    boolean matchOnSha256 = !ts.getSha256().isEmpty() && ts.getSha256().equals(this.getSongdata().getSha256());
                    if (matchOnMd5 || matchOnSha256) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    reverseLookup.add(tf.getName());
                    break;
                }
            }
        }
        return reverseLookup;
    }

	public ReplayData getChartOption() {
		return chartOption;
	}

	public void setChartOption(ReplayData chartOption) {
		this.chartOption = chartOption;
	}

	public bms.model.Mode getOriginalMode() {
		return orgmode;
	}

	public void setOriginalMode(bms.model.Mode orgmode) {
		this.orgmode = orgmode;
	}

	public boolean isFreqOn() {
		return freqOn;
	}

	public void setFreqOn(boolean freqOn) {
		this.freqOn = freqOn;
	}

	public String getFreqString() {
		return freqString;
	}

	public void setFreqString(String freqString) {
		this.freqString = freqString;
	}

	public boolean isForceNoIRSend() {
		return forceNoIRSend;
	}

	public void setForceNoIRSend(boolean forceNoIRSend) {
		this.forceNoIRSend = forceNoIRSend;
	}

	public Future<BMSLoudnessAnalyzer.AnalysisResult> getAnalysisTask() {
		return analysisTask;
	}
}
