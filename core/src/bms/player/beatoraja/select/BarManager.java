package bms.player.beatoraja.select;

import static bms.player.beatoraja.SystemSoundManager.SoundType.FOLDER_CLOSE;
import static bms.player.beatoraja.SystemSoundManager.SoundType.OPTION_CHANGE;

import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import bms.player.beatoraja.modmenu.SongManagerMenu;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.StringBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bms.model.Mode;
import bms.player.beatoraja.*;
import bms.player.beatoraja.CourseData.CourseDataConstraint;
import bms.player.beatoraja.CourseData.TrophyData;
import bms.player.beatoraja.external.BMSSearchAccessor;
import bms.player.beatoraja.ir.IRResponse;
import bms.player.beatoraja.ir.IRTableData;
import bms.player.beatoraja.select.bar.*;
import bms.player.beatoraja.skin.property.EventFactory.EventType;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.song.SongInformationAccessor;

/**
 * 楽曲バー管理用クラス
 *
 * @author exch
 */
public class BarManager {
	private static final Logger logger = LoggerFactory.getLogger(BarManager.class);
	
	private final MusicSelector select;
	/**
	 * 難易度表バー一覧
	 */
	private TableBar[] tables = new TableBar[0];

	private Bar[] commands;
	
	private TableBar courses;

	private HashBar[] favorites = new HashBar[0];

	/**
	 * 現在のフォルダ階層
	 */
	private final Queue<DirectoryBar> dir = new Queue<>();
	private String dirString = "";
	/**
	 * 現在表示中のバー一覧
	 */
	Bar[] currentsongs;
	/**
	 * 選択中のバーのインデックス
	 */
	int selectedindex;

	/**
	 * 各階層のフォルダを開く元となったバー
	 */
	private final Queue<Bar> sourcebars = new Queue<>();

	// jsonで定義したrandom bar (folder)
	private List<RandomFolder> randomFolderList;

	// システム側で挿入されるルートフォルダ
	private final HashMap<String, Bar> appendFolders = new HashMap<String, Bar>();
	/**
	 * 検索結果バー一覧
	 */
	private final Array<SearchWordBar> search = new Array<SearchWordBar>();
	/**
	 * ランダムコース結果バー一覧
	 */
	private final Array<RandomCourseResult> randomCourseResult = new Array<>();

	BarContentsLoaderThread loader;

	public BarManager(MusicSelector select) {
		this.select = select;
	}
	
	void init() {
		TableDataAccessor tdaccessor = new TableDataAccessor(select.resource.getConfig().getTablepath());

        TableData[] unsortedtables;
        try (var perf = PerformanceMetrics.get().Event("Saved table load")) {
            unsortedtables = tdaccessor.readAll();
        }

		final List<TableData> sortedtables = new ArrayList<TableData>(unsortedtables.length);
		
		for(String url : select.resource.getConfig().getTableURL()) {
			for(int i = 0;i < unsortedtables.length;i++) {
				final TableData td = unsortedtables[i];
				if(td != null && url.equals(td.getUrl())) {
					sortedtables.add(td);
					unsortedtables[i] = null;
					break;
				}
			}
		}

		Arrays.stream(unsortedtables).filter(Objects::nonNull).forEach(td -> sortedtables.add(td));

		BMSSearchAccessor bmssearcha = new BMSSearchAccessor(select.resource.getConfig().getTablepath());

		Array<TableBar> table = new Array<TableBar>();

		sortedtables.stream().map(td -> {
			if (td.getName().equals("BMS Search")) {
				return new TableBar(select, td, bmssearcha);
			} else {
				return new TableBar(select, td,
						new TableDataAccessor.DifficultyTableAccessor(select.resource.getConfig().getTablepath(), td.getUrl()));
			}			
		}).forEach(table::add);;

		if(select.main.getIRStatus().length > 0) {
			IRResponse<IRTableData[]> response = select.main.getIRStatus()[0].connection.getTableDatas();
			if(response.isSucceeded()) {
				for(IRTableData irtd : response.getData()) {
					TableData td = new TableData();
					td.setName(irtd.name);
					td.setFolder(Stream.of(irtd.folders).map(folder -> {
						TableData.TableFolder tf = new TableData.TableFolder();
						tf.setName(folder.name);
						tf.setSong(Stream.of(folder.charts).map(chart -> {
							SongData song = new SongData();
							song.setSha256(chart.sha256);
							song.setMd5(chart.md5);
							song.setTitle(chart.title);
							song.setArtist(chart.artist);
							song.setGenre(chart.genre);
							song.setUrl(chart.url);
							song.setAppendurl(chart.appendurl);
							if(chart.mode != null) {
								song.setMode(chart.mode.id);								
							}
							return song;
						}).toArray(SongData[]::new));
						return tf;
					}).toArray(TableData.TableFolder[]::new));
					
					td.setCourse(Stream.of(irtd.courses).map(course -> {
						CourseData cd = new CourseData();
						cd.setName(course.name);
						cd.setSong(Stream.of(course.charts).map(chart -> {
							SongData song = new SongData();
							song.setSha256(chart.sha256);
							song.setMd5(chart.md5);
							song.setTitle(chart.title);
							song.setArtist(chart.artist);
							song.setGenre(chart.genre);
							song.setUrl(chart.url);
							song.setAppendurl(chart.appendurl);
							if(chart.mode != null) {
								song.setMode(chart.mode.id);								
							}
							return song;
						}).toArray(SongData[]::new));
						
						cd.setConstraint(course.constraint);
						cd.setTrophy(Stream.of(course.trophy).map(t -> {
						    TrophyData trophyData = new TrophyData();
						    trophyData.setName(t.name);
						    trophyData.setMissrate(t.smissrate);
						    trophyData.setScorerate(t.scorerate);
							return trophyData;
						}).toArray(TrophyData[]::new));
						
						cd.setRelease(true);
						return cd;
					}).toArray(CourseData[]::new));
					
					if(td.validate()) {
						table.add(new TableBar(select, td, new TableDataAccessor.DifficultyTableAccessor(select.resource.getConfig().getTablepath(), td.getUrl())));						
					}
				}
			} else {
				logger.warn("IRからのテーブル取得失敗 : {}", response.getMessage());
			}
		}

		new Thread(() -> {
			TableData td = bmssearcha.read();
			if (td != null) {
				tdaccessor.write(td);
			}
		}).start();

		this.tables = table.toArray(TableBar.class);


		TableDataAccessor.TableAccessor courseReader = new TableDataAccessor.TableAccessor("course") {
			@Override
			public TableData read() {
				TableData td = new TableData();
				td.setName("COURSE");
				td.setCourse(new CourseDataAccessor("course").readAll());
				return td;
			}

			@Override
			public void write(TableData td) {
			}
		};
		courses = new TableBar(select, courseReader.read(), courseReader);

		CourseData[] cds = new CourseDataAccessor("favorite").readAll();
//		if(cds.length == 0) {
//			cds = new CourseData[1];
//			cds[0] = new CourseData();
//			cds[0].setName("FAVORITE");
//		}
		
		favorites = Stream.of(cds).map(cd -> new HashBar(select, cd.getName(), cd.getSong())).toArray(HashBar[]::new);

		Array<Bar> l = new Array<Bar>();

		Array<Bar> lampupdate = new Array<Bar>();
		Array<Bar> scoreupdate = new Array<Bar>();
		for(int i = 0;i < 30;i++) {
			String s = i == 0 ? "TODAY" : i + "DAYS AGO";
			long t = ((System.currentTimeMillis() / 86400000) - i) * 86400;
			lampupdate.add(new CommandBar(select,  s, "scorelog.clear > scorelog.oldclear AND scorelog.date >= "  + t + " AND scorelog.date < " + (t + 86400)));
			scoreupdate.add(new CommandBar(select,  s,  "scorelog.score > scorelog.oldscore AND scorelog.date >= "  + t + " AND scorelog.date < " + (t + 86400)));
		}
		l.add(new ContainerBar("LAMP UPDATE", lampupdate.toArray(Bar.class)));
		l.add(new ContainerBar("SCORE UPDATE", scoreupdate.toArray(Bar.class)));
		try {
			Json json = new Json();
			CommandFolder[] cf = json.fromJson(CommandFolder[].class,
					new BufferedInputStream(Files.newInputStream(Paths.get("folder/default.json"))));
			Stream.of(cf).forEach(folder -> l.add(createCommandBar(select, folder)));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			randomFolderList = objectMapper.readValue(
					new BufferedInputStream(Files.newInputStream(Paths.get("random/default.json"))),
					new TypeReference<List<RandomFolder>>() {
					});
		} catch (Throwable e) {
			randomFolderList = new ArrayList<RandomFolder>();
			RandomFolder randomFolder = new RandomFolder();
			randomFolder.setName("RANDOM SELECT");
			randomFolderList.add(randomFolder);
			e.printStackTrace();
		}

		commands = l.toArray(Bar.class);
	}
	
	public boolean updateBar() {
		if (dir.size > 0) {
			return updateBar(dir.last());
		}
		return updateBar(null);
	}

	public boolean updateBar(Bar bar) {
		Bar prevbar = currentsongs != null ? currentsongs[selectedindex] : null;
		int prevdirsize = dir.size;
		Bar sourcebar = null;
		Array<Bar> l = new Array<Bar>();
		boolean showInvisibleCharts = false;
		boolean isSortable = true;

		// -------------------------------------------------------------
		// リスト生成処理 (元のBarManagerのロジックに準拠)
		// -------------------------------------------------------------
		if (MainLoader.getIllegalSongCount() > 0) {
			l.addAll(SongBar.toSongBarArray(select.getSongDatabase().getSongDatas(MainLoader.getIllegalSongs())));
		} else if (bar == null) {
			// root bar
			if (dir.size > 0) {
				prevbar = dir.first();
			}
			dir.clear();
			sourcebars.clear();
			l.addAll(new FolderBar(select, null, "e2977170").getChildren());
			l.add(courses);
			l.addAll(favorites);
			appendFolders.keySet().forEach((key) -> {
				l.add(appendFolders.get(key));
			});
			l.addAll(tables);
			l.addAll(commands);
			l.addAll(search);
		} else if (bar instanceof DirectoryBar) {
			// 【修正】 ここで不可視譜面の設定を取得
			showInvisibleCharts = ((DirectoryBar)bar).isShowInvisibleChart();
			if(dir.indexOf((DirectoryBar) bar, true) != -1) {
				while(dir.last() != bar) {
					prevbar = dir.removeLast();
					sourcebar = sourcebars.removeLast();
				}
				dir.removeLast();
			}
			l.addAll(((DirectoryBar) bar).getChildren());
			isSortable = ((DirectoryBar) bar).isSortable();

			if (bar instanceof ContainerBar && randomCourseResult.size > 0) {
				StringBuilder str = new StringBuilder();
				for (Bar b : dir) {
					str.append(b.getTitle()).append(" > ");
				}
				str.append(bar.getTitle()).append(" > ");
				final String ds = str.toString();
				for (RandomCourseResult r : randomCourseResult) {
					if (r.dirString.equals(ds)) {
						l.add(r.course);
					}
				}
			}
		}

		if(!select.resource.getConfig().isShowNoSongExistingBar()) {
			Array<Bar> remove = new Array<Bar>();
			for (Bar b : l) {
				if ((b instanceof SongBar && !((SongBar) b).existsSong())
						|| b instanceof GradeBar && !((GradeBar) b).existsAllSongs()) {
					remove.add(b);
				}
			}
			l.removeAll(remove, true);
		}

		if (l.size > 0) {
			final PlayerConfig config = select.resource.getPlayerConfig();

			// 【修正1】 フリーズ対策：リストのバックアップを作成
			final Array<Bar> originalList = new Array<>(l);

			int modeIndex = 0;
			for(;modeIndex < MusicSelector.MODE.length && MusicSelector.MODE[modeIndex] != config.getMode();modeIndex++);
			for(int trialCount = 0; trialCount < MusicSelector.MODE.length; trialCount++, modeIndex++) {
				// 【修正1の続き】 試行ごとにリストを初期状態にリセット
				l.clear();
				l.addAll(originalList);

				final Mode mode = MusicSelector.MODE[modeIndex % MusicSelector.MODE.length];
				config.setMode(mode);
				Array<Bar> remove = new Array<Bar>();

				// -------------------------------------------------------------
				// 1. 基本的な除外処理
				// -------------------------------------------------------------
				for (Bar b : l) {
					if(b instanceof SongBar && ((SongBar) b).getSongData() != null) {
						final SongData song = ((SongBar) b).getSongData();
						if((!showInvisibleCharts && (song.getFavorite() & (SongData.INVISIBLE_SONG | SongData.INVISIBLE_CHART)) != 0)
								|| (mode != null && song.getMode() != 0 && song.getMode() != mode.id)) {
							remove.add(b);
						}
					}
				}
				l.removeAll(remove, true);

				// -------------------------------------------------------------
				// 2. 難易度フィルタリング (親フォルダ基準)
				// -------------------------------------------------------------
				if (config.getDifficultyFilter() != 0 && l.size > 0) {
					remove.clear();
					java.util.HashMap<String, java.util.ArrayList<SongBar>> songGroups = new java.util.HashMap<>();

					for (Bar b : l) {
						if (b instanceof SongBar) {
							SongBar sb = (SongBar) b;
							SongData sd = sb.getSongData();
							if (sd != null && sd.getPath() != null) {
								String key;
								try {
									String path = sd.getPath();
									java.io.File file = new java.io.File(path);
									String parentPath = file.getParent();
									key = (parentPath != null) ? parentPath : path;
								} catch (Exception e) {
									key = sd.getTitle() + "_" + sd.getArtist() + "_" + sb.hashCode();
								}
								if (!songGroups.containsKey(key)) {
									songGroups.put(key, new java.util.ArrayList<>());
								}
								songGroups.get(key).add(sb);
							}
						}
					}

					// B. 各グループから表示対象を選出
					for (java.util.ArrayList<SongBar> group : songGroups.values()) {
						java.util.ArrayList<SongBar> matches = new java.util.ArrayList<>();

						// 最高難易度を探すための変数（初期値はあり得ない低い値）
						int maxDiff = -1;

						// 1. フィルタ一致の抽出 & フォルダ内の最高難易度を特定
						for (SongBar sb : group) {
							int d = sb.getSongData().getDifficulty();

							// フィルタ設定と一致するものをリストアップ
							if (d == config.getDifficultyFilter()) {
								matches.add(sb);
							}

							// フォルダ内の最高難易度を更新
							if (d > maxDiff) {
								maxDiff = d;
							}
						}

						// 2. 削除対象の決定
						if (!matches.isEmpty()) {
							// フィルタ一致譜面がある場合: リストに含まれないものを全て削除
							for (SongBar sb : group) {
								if (!matches.contains(sb)) {
									remove.add(sb);
								}
							}
						} else {
							// フィルタ一致譜面がない場合: 特定した「最高難易度(maxDiff)」と一致しないものを削除
							// これにより、同じ最高難易度の譜面が複数あっても全て残ります
							for (SongBar sb : group) {
								if (sb.getSongData().getDifficulty() != maxDiff) {
									remove.add(sb);
								}
							}
						}
					}
					if (l.size != remove.size) {
						l.removeAll(remove, true);
					}
				}

				// 曲が見つかった場合
				if(l.size > 0) {
					break;
				} else {
					// 【修正2】 曲が見つからず、最後の試行だった場合はリストを空にする
					if (trialCount == MusicSelector.MODE.length - 1) {
						l.clear();
					}
				}
			}

			if (bar != null) {
				dir.addLast((DirectoryBar) bar);
				if (dir.size > prevdirsize) {
					sourcebars.addLast(prevbar);
				}
			}

			Bar[] newcurrentsongs = l.toArray(Bar.class);
			for (Bar b : newcurrentsongs) {
				if (b instanceof SongBar) {
					SongData sd = ((SongBar) b).getSongData();
					if (sd != null && select.getScoreDataCache().existsScoreDataCache(sd, config.getLnmode())) {
						b.setScore(select.getScoreDataCache().readScoreData(sd, config.getLnmode()));
					}
				}
			}

			if(isSortable) {
				final BarSorter sorter = BarSorter.valueOf(select.main.getPlayerConfig().getSortid());
				Sort.instance().sort(newcurrentsongs, sorter != null ? sorter.sorter : BarSorter.TITLE.sorter);
			}

			Array<Bar> bars = new Array<Bar>();
			if (select.main.getPlayerConfig().isRandomSelect()) {
				try {
					for (RandomFolder randomFolder : randomFolderList) {
						SongData[] randomTargets = Stream.of(newcurrentsongs).filter(
										songBar -> songBar instanceof SongBar && ((SongBar) songBar).getSongData().getPath() != null)
								.map(songBar -> ((SongBar) songBar).getSongData()).toArray(SongData[]::new);
						if (randomFolder.getFilter() != null) {
							Set<String> filterKey = randomFolder.getFilter().keySet();
							randomTargets = Stream.of(randomTargets).filter(r -> {
								ScoreData scoreData = select.getScoreDataCache().readScoreData(r, config.getLnmode());
								for (String key : filterKey) {
									String getterMethodName = "get" + key.substring(0, 1).toUpperCase()
											+ key.substring(1);
									try {
										Object value = randomFolder.getFilter().get(key);
										if (scoreData == null) {
											if (value instanceof String && !"".equals((String) value)) {
												return false;
											}
											if (value instanceof Integer && 0 != (Integer) value) {
												return false;
											}
										} else {
											Method getterMethod = ScoreData.class.getMethod(getterMethodName);
											Object propertyValue = getterMethod.invoke(scoreData);
											if (!propertyValue.equals(value)) {
												return false;
											}
										}
									} catch (Throwable e) {
										e.printStackTrace();
										return false;
									}
								}
								return true;
							}).toArray(SongData[]::new);
						}
						if ((randomFolder.getFilter() != null && randomTargets.length >= 1)
								|| (randomFolder.getFilter() == null && randomTargets.length >= 2)) {
							Bar randomBar = new ExecutableBar(randomTargets, select.main.getCurrentState(),
									randomFolder.getName());
							bars.add(randomBar);
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			bars.addAll(newcurrentsongs);

			currentsongs = bars.toArray(Bar.class);

			// 【修正3】 クラッシュ対策: 空ならnullにする
			if (currentsongs.length == 0) {
				currentsongs = null;
			}

			select.getBarRender().updateBarText();

			selectedindex = 0;

			// -------------------------------------------------------------
			// カーソル位置復元 (親フォルダ追従機能付き)
			// -------------------------------------------------------------
			if (sourcebar != null) {
				prevbar = sourcebar;
			}
			if (prevbar != null) {
				if (prevbar instanceof SongBar && ((SongBar) prevbar).existsSong()) {
					final SongBar prevsong = (SongBar) prevbar;
					boolean found = false;

					if (currentsongs != null) {
						for (int i = 0; i < currentsongs.length; i++) {
							if (currentsongs[i] instanceof SongBar && ((SongBar) currentsongs[i]).existsSong() &&
									((SongBar) currentsongs[i]).getSongData().getSha256()
											.equals(prevsong.getSongData().getSha256())) {
								selectedindex = i;
								found = true;
								break;
							}
						}
					}

					if (!found && prevsong.getSongData() != null && prevsong.getSongData().getPath() != null && currentsongs != null) {
						String prevParent = null;
						try {
							prevParent = new java.io.File(prevsong.getSongData().getPath()).getParent();
						} catch (Exception e) {}

						if (prevParent != null) {
							for (int i = 0; i < currentsongs.length; i++) {
								if (currentsongs[i] instanceof SongBar && ((SongBar) currentsongs[i]).existsSong()) {
									SongData sd = ((SongBar) currentsongs[i]).getSongData();
									if (sd != null && sd.getPath() != null) {
										try {
											String currParent = new java.io.File(sd.getPath()).getParent();
											if (prevParent.equals(currParent)) {
												selectedindex = i;
												break;
											}
										} catch (Exception e) {}
									}
								}
							}
						}
					}

				} else {
					if (currentsongs != null) {
						for (int i = 0; i < currentsongs.length; i++) {
							if (currentsongs[i].getClass() == prevbar.getClass() && currentsongs[i].getTitle().equals(prevbar.getTitle())) {
								selectedindex = i;
								break;
							}
						}
					}
				}
			}

			if (currentsongs != null && currentsongs.length > selectedindex) {
				select.getScoreDataProperty().update(currentsongs[selectedindex].getScore(),
						currentsongs[selectedindex].getRivalScore());
			}

			if (loader != null) {
				loader.stopRunning();
			}
			loader = new BarContentsLoaderThread(select, currentsongs);
			loader.start();

			// ガード追加
			if (currentsongs != null && currentsongs.length > selectedindex) {
				select.getScoreDataProperty().update(currentsongs[selectedindex].getScore(),
						currentsongs[selectedindex].getRivalScore());
			}

			StringBuilder str = new StringBuilder();
			for (Bar b : dir) {
				str.append(b.getTitle()).append(" > ");
			}
			dirString = str.toString();

			select.selectedBarMoved();

			return true;
		}

		// -------------------------------------------------------------
		// 【修正4】 無限ループ・フリーズ回避のためのフォールバック処理
		// -------------------------------------------------------------
		if (dir.size > 0) {
			return updateBar(dir.last());
		} else if (bar != null) {
			return updateBar(null);
		} else {
			currentsongs = null;
			selectedindex = 0;
			logger.warn("楽曲がありません");
			return false;
		}
	}

	public void close() {
		if(dir.size == 0) {
            SongManagerMenu.forceDisableLastPlayedSort();
			select.executeEvent(EventType.sort);
			return;
		}

		final DirectoryBar current = dir.removeLast();
		final DirectoryBar parent = dir.size > 0 ? dir.last() : null;
		dir.addLast(current);
		updateBar(parent);
		select.play(FOLDER_CLOSE);
	}

	public Queue<DirectoryBar> getDirectory() {
		return dir;
	}

	public String getDirectoryString() {
		return dirString;
	}

	public Bar getSelected() {
		return currentsongs != null ? currentsongs[selectedindex] : null;
	}

	public void setSelected(Bar bar) {
		for (int i = 0; i < currentsongs.length; i++) {
			if (currentsongs[i].getTitle().equals(bar.getTitle())) {
				selectedindex = i;
				select.getScoreDataProperty().update(currentsongs[selectedindex].getScore(),
						currentsongs[selectedindex].getRivalScore());
				break;
			}
		}
	}

	public float getSelectedPosition() {
		return ((float) selectedindex) / currentsongs.length;
	}

    public TableBar[] getTables() { return tables.clone(); }

	public void setSelectedPosition(float value) {
		if (value >= 0 && value < 1) {
			selectedindex = (int) (currentsongs.length * value);
		}
		select.getScoreDataProperty().update(currentsongs[selectedindex].getScore(),
				currentsongs[selectedindex].getRivalScore());
	}

	public void move(boolean inclease) {
		if (inclease) {
			selectedindex++;
		} else {
			selectedindex += currentsongs.length - 1;
		}
		selectedindex = selectedindex % currentsongs.length;
		select.getScoreDataProperty().update(currentsongs[selectedindex].getScore(),
				currentsongs[selectedindex].getRivalScore());
	}

	private Bar createCommandBar(MusicSelector select, CommandFolder folder) {
		return (folder.getFolder() != null && folder.getFolder().length > 0 || folder.getRandomCourse() != null && folder.getRandomCourse().length > 0) ?
			new ContainerBar(folder.getName(), Stream.concat(
					Stream.of(folder.getFolder()).map(child -> createCommandBar(select, child))
					,Stream.of(folder.getRandomCourse()).map(RandomCourseBar::new)).toArray(Bar[]::new)) : 
			new CommandBar(select, folder.getName(), folder.getSql(), folder.isShowall());
	}

	public void addSearch(SearchWordBar bar) {
		for (SearchWordBar s : search) {
			if (s.getTitle().equals(bar.getTitle())) {
				search.removeValue(s, true);
				break;
			}
		}
		if (search.size >= select.resource.getConfig().getMaxSearchBarCount()) {
			search.removeIndex(0);
		}
		search.add(bar);
	}

	public void addRandomCourse(GradeBar bar, String dirString) {
		if (randomCourseResult.size >= 100) {
			randomCourseResult.removeIndex(0);
		}
		randomCourseResult.add(new RandomCourseResult(bar, dirString));
	}

	synchronized public void setAppendDirectoryBar(String key, Bar bar) {
	    this.appendFolders.put(key, bar);
	}

	public static class CommandFolder {

		private String name;
		private CommandFolder[] folder = new CommandFolder[0];
		private String sql;
		private RandomCourseData[] rcourse = RandomCourseData.EMPTY;
		private boolean showall = false;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public CommandFolder[] getFolder() {
			return folder;
		}

		public void setFolder(CommandFolder[] songs) {
			this.folder = songs;
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public RandomCourseData[] getRandomCourse() { return rcourse; }

		public void setRandomCourse(RandomCourseData[] course) { this.rcourse = course; }

		public boolean isShowall() {
			return showall;
		}

		public void setShowall(boolean showall) {
			this.showall = showall;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RandomFolder {
		private String name;
		private Map<String, Object> filter;
		public String getName() {
			return "[RANDOM] " + name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Map<String, Object> getFilter() {
			return filter;
		}

		public void setFilter(Map<String, Object> filter) {
			this.filter = filter;
		}

        public Boolean filterSong(ScoreData scoreData) {
            Set<String> filterKey = this.getFilter().keySet();
            for (String key : filterKey) {
                String getterMethodName = "get" + key.substring(0, 1).toUpperCase()
                        + key.substring(1);
                try {
                    if (this.getFilter().get(key) instanceof Integer) { // Fork for integer values of the filter key.
                        Integer value = (Integer)this.getFilter().get(key);
                        if (scoreData == null) {
                            if (0 != value) {
                                return false;
                            }
                        } else {
                            Method getterMethod = ScoreData.class.getMethod(getterMethodName);
                            Object propertyValue = getterMethod.invoke(scoreData);
                            if (!propertyValue.equals(value)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    Object valueArr[] = ((String)this.getFilter().get(key)).split("&&");
                    for (Object value : valueArr) // Fork for string values of the filter key.
                    {
                        value = ((String)value).replaceAll("\\s",""); // Clean from whitespaces.
                        if (scoreData == null) {
                            String stringValue = (String) value;
                            if (!stringValue.isEmpty() && !stringValue.substring(0, 1).equals("<")) {
                                return false; // Because lack of value would be less than anything.
                            }
                        } else {
                            Method getterMethod = ScoreData.class.getMethod(getterMethodName);
                            Object propertyValue = getterMethod.invoke(scoreData);
                            if (propertyValue instanceof Integer) {
                                String valueString = (String)value;
                                Integer propertyValueInt = (Integer)propertyValue;
                                Integer filterValueInt;
                                // Checking the position for the integer bit of the key value.
                                if (valueString.substring(1,2).equals("=")){ // If the operation is either >= or <=.
                                    filterValueInt = Integer.parseInt(valueString.substring(2));
                                }
                                // If the operation is > or <.
                                else filterValueInt = Integer.parseInt(valueString.substring(1));

                                if (valueString.substring(0,1).equals(">")) // Fork for > and >= operations.
                                {
                                    if (valueString.substring(1,2).equals("="))
                                    {
                                        if (!(propertyValueInt >= filterValueInt))
                                        {
                                            return false;
                                        }
                                    }
                                    else if (!(propertyValueInt > filterValueInt))
                                    {
                                        return false;
                                    }
                                }
                                if (valueString.substring(0,1).equals("<")) // Fork for < and <= operations.
                                {
                                    if (valueString.substring(1,2).equals("="))
                                    {
                                        if (!(propertyValueInt <= filterValueInt))
                                        {
                                            return false;
                                        }
                                    }
                                    else if (!(propertyValueInt < filterValueInt))
                                    {
                                        return false;
                                    }
                                }
                            }
                            else if (!propertyValue.equals(value)) {
                                return false;
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

    }

	static class RandomCourseResult {
		public GradeBar course;
		public String dirString;

		public RandomCourseResult(GradeBar course, String dirString) {
			this.course = course;
			this.dirString = dirString;
		}
	}

	/**
	 * 選曲バー内のスコアデータ等を読み込むためのスレッド
	 */
	static class BarContentsLoaderThread extends Thread {

		private final MusicSelector select;
		/**
		 * データ読み込み対象の選曲バー
		 */
		private Bar[] bars;
		/**
		 * 読み込み終了フラグ
		 */
		private boolean stop = false;

		public BarContentsLoaderThread(MusicSelector select, Bar[] bar) {
			this.select = select;
			this.bars = bar;
		}

		@Override
		public void run() {
			final MainController main = select.main;
			final PlayerConfig config = select.resource.getPlayerConfig();
			final ScoreDataCache rival = select.getRivalScoreDataCache();
			final String rivalName = rival != null ? select.getRival().getName() : null;

			final SongData[] songs = Stream.of(bars).filter(bar -> bar instanceof SongBar && ((SongBar) bar).existsSong())
					.map(bar -> ((SongBar) bar).getSongData()).toArray(SongData[]::new);
			// loading score
			// TODO collectorを使用してスコアをまとめて取得
			for (Bar bar : bars) {
				if (bar instanceof SongBar && ((SongBar) bar).existsSong()) {
					SongData sd = ((SongBar) bar).getSongData();
					if (bar.getScore() == null) {
						bar.setScore(select.getScoreDataCache().readScoreData(sd, config.getLnmode()));
					}
					if (rival != null && bar.getRivalScore() == null) {
						final ScoreData rivalScore = rival.readScoreData(sd, config.getLnmode());
						if(rivalScore != null) {
							rivalScore.setPlayer(rivalName);							
						}
						bar.setRivalScore(rivalScore);
					}
					for(int i = 0;i < MusicSelector.REPLAY;i++) {
						((SongBar) bar).setExistsReplay(i, main.getPlayDataAccessor().existsReplayData(sd.getSha256(), sd.hasUndefinedLongNote(),config.getLnmode(), i));						
					}
				} else if (bar instanceof GradeBar && ((GradeBar)bar).existsAllSongs()) {
					final GradeBar gb = (GradeBar) bar;
					String[] hash = new String[gb.getSongDatas().length];
					boolean ln = false;
					for (int j = 0; j < gb.getSongDatas().length; j++) {
						hash[j] = gb.getSongDatas()[j].getSha256();
						ln |= gb.getSongDatas()[j].hasUndefinedLongNote();
					}
					CourseDataConstraint[] constraint = gb.getCourseData().getConstraint();
					gb.setScore(main.getPlayDataAccessor().readScoreData(hash, ln, config.getLnmode(), 0, constraint));
					gb.setMirrorScore(main.getPlayDataAccessor().readScoreData(hash, ln, config.getLnmode(), 1, constraint));
					gb.setRandomScore(main.getPlayDataAccessor().readScoreData(hash, ln, config.getLnmode(), 2, constraint));
					for(int i = 0;i < MusicSelector.REPLAY;i++) {
						gb.setExistsReplay(i, main.getPlayDataAccessor().existsReplayData(hash, ln ,config.getLnmode(), i, constraint));						
					}
				}

				if (select.resource.getConfig().isFolderlamp()) {
					if (bar instanceof DirectoryBar) {
						((DirectoryBar) bar).updateFolderStatus();
					}
				}
				if (stop) {
					break;
				}
			}
			// loading song information
			final SongInformationAccessor info = main.getInfoDatabase();
			if(info != null) {
				info.getInformation(songs);
			}
			// loading banner
			// loading stagefile
			for (Bar bar : bars) {
				if (bar instanceof SongBar && ((SongBar) bar).existsSong()) {
					final SongBar songbar = (SongBar) bar;
					SongData song = songbar.getSongData();
					try {
						Path bannerfile = Paths.get(song.getPath()).getParent().resolve(song.getBanner());
						// System.out.println(bannerfile.getPath());
						if (song.getBanner().length() > 0 && Files.exists(bannerfile)) {
							songbar.setBanner(select.getBannerResource().get(bannerfile.toString()));
						}
					} catch (Exception e) {
						logger.warn("banner読み込み失敗 : {}", song.getBanner());
					}
					try {
						Path stagefilefile = Paths.get(song.getPath()).getParent().resolve(song.getStagefile());
						// System.out.println(stagefilefile.getPath());
						if (song.getStagefile().length() > 0 && Files.exists(stagefilefile)) {
							songbar.setStagefile(select.getStagefileResource().get(stagefilefile.toString()));
						}
					} catch (Exception e) {
						logger.warn("stagefile読み込み失敗 : {}", song.getStagefile());
					}
				}
				if (stop) {
					break;
				}
			}
		}

		/**
		 * データ読み込みを中断する
		 */
		public void stopRunning() {
			stop = true;
		}
	}

	// ---------------------------------------------------------
	// 【追加】 難易度フィルター操作用メソッド
	// ---------------------------------------------------------

	/**
	 * 現在の難易度フィルター設定を取得します
	 */
	public int getDifficultyFilter() {
		return select.resource.getPlayerConfig().getDifficultyFilter();
	}

	/**
	 * 難易度フィルターを順次切り替えます (ALL -> BEG -> ... -> INS -> ALL)
	 * 切り替え時にオプション変更音を鳴らします。
	 */
	public void toggleDifficultyFilter() {
		PlayerConfig config = select.resource.getPlayerConfig();
		config.setDifficultyFilter((config.getDifficultyFilter() + 1) % 6);
		this.updateBar();
		select.play(OPTION_CHANGE);
	}

	/**
	 * 難易度フィルターを直接指定します
	 * @param difficulty 0:ALL, 1:BEG, 2:NOR, 3:HYP, 4:ANO, 5:INS
	 */
	public void setDifficultyFilter(int difficulty) {
		int d = (difficulty < 0 || difficulty > 5) ? 0 : difficulty;
		select.resource.getPlayerConfig().setDifficultyFilter(d);
		this.updateBar();
		select.play(OPTION_CHANGE);
	}

}
