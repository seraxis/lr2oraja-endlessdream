package bms.player.beatoraja;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.exceptions.PlayerConfigException;
import bms.player.beatoraja.modmenu.*;
import bms.tool.mdprocessor.HttpDownloadProcessor;
import bms.tool.mdprocessor.HttpDownloadSource;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

import bms.player.beatoraja.AudioConfig.DriverType;
import bms.player.beatoraja.MainState.MainStateType;
import bms.player.beatoraja.audio.*;
import bms.player.beatoraja.config.KeyConfiguration;
import bms.player.beatoraja.config.SkinConfiguration;
import bms.player.beatoraja.decide.MusicDecide;
import bms.player.beatoraja.external.*;
import bms.player.beatoraja.obs.*;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;
import bms.player.beatoraja.input.KeyCommand;
import bms.player.beatoraja.ir.*;
import bms.player.beatoraja.play.BMSPlayer;
import bms.player.beatoraja.play.TargetProperty;
import bms.player.beatoraja.result.CourseResult;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.TableBar;
import bms.player.beatoraja.skin.SkinLoader;
import bms.player.beatoraja.skin.SkinObject.SkinOffset;
import bms.player.beatoraja.skin.SkinProperty;
import bms.player.beatoraja.song.*;
import bms.player.beatoraja.stream.StreamController;
import bms.tool.mdprocessor.MusicDownloadProcessor;

/**
 * アプリケーションのルートクラス
 *
 * @author exch
 */
public class MainController {
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	private static final String VERSION = Version.versionLong;

	public static final boolean debug = false;
	public static final int debugTextXpos = 10;

	/**
	 * 起動時間
	 */
	private final long boottime = System.currentTimeMillis();
	private final Calendar cl = Calendar.getInstance();
	private long mouseMovedTime;

	private BMSPlayer bmsplayer;
	private MusicDecide decide;
	private MusicSelector selector;
	private MusicResult result;
	private CourseResult gresult;
	private KeyConfiguration keyconfig;
	private SkinConfiguration skinconfig;

	private AudioDriver audio;

	private BMSLoudnessAnalyzer loudnessAnalyzer;

	private PlayerResource resource;

	private BitmapFont systemfont;

	private MainState current;
	
	private TimerManager timer;

	private Config config;
	private PlayerConfig player;
	private BMSPlayerMode auto;
	private boolean songUpdated;

	private SongInformationAccessor infodb;

	private IRStatus[] ir;

	private RivalDataAccessor rivals = new RivalDataAccessor();

	private RankingDataCache ircache = new RankingDataCache();

	private SpriteBatch sprite;
	/**
	 * 1曲プレイで指定したBMSファイル
	 */
	private Path bmsfile;

	private BMSPlayerInputProcessor input;
	/**
	 * FPSを描画するかどうか
	 */
	private boolean showfps;
	/**
	 * プレイデータアクセサ
	 */
	private PlayDataAccessor playdata;

	private SystemSoundManager sound;

	private Thread screenshot;

	private MusicDownloadProcessor download;
	private HttpDownloadProcessor httpDownloadProcessor;

	private StreamController streamController;

	private ObsListener obsListener;
	private ObsWsClient obsClient;

	public static final int offsetCount = SkinProperty.OFFSET_MAX + 1;
	private final SkinOffset[] offset = new SkinOffset[offsetCount];

	protected TextureRegion black;
	protected TextureRegion white;

	private final Array<MainStateListener> stateListener = new Array<MainStateListener>();

	public ImGuiRenderer imGui;

	public List<IRSendStatus> irSendStatus = new ArrayList<IRSendStatus>();

	public MainController(Path f, Config config, PlayerConfig player, BMSPlayerMode auto, boolean songUpdated) {
		this.auto = auto;
		this.config = config;
		this.songUpdated = songUpdated;

		for(int i = 0;i < offset.length;i++) {
			offset[i] = new SkinOffset();
		}

		if(player == null) {
            try {
                player = PlayerConfig.readPlayerConfig(config.getPlayerpath(), config.getPlayername());
            } catch (PlayerConfigException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
		this.player = player;

		this.bmsfile = f;

		if (config.isEnableIpfs()) {
			Path ipfspath = Paths.get("ipfs").toAbsolutePath();
			if (!ipfspath.toFile().exists())
				ipfspath.toFile().mkdirs();
			List<String> roots = new ArrayList<>(Arrays.asList(getConfig().getBmsroot()));
			if (ipfspath.toFile().exists() && !roots.contains(ipfspath.toString())) {
				roots.add(ipfspath.toString());
				getConfig().setBmsroot(roots.toArray(new String[roots.size()]));
			}
		}
		if (config.isEnableHttp()) {
			Path httpdlPath = Paths.get(config.getDownloadDirectory()).toAbsolutePath();
			if (!httpdlPath.toFile().exists())
				httpdlPath.toFile().mkdirs();
			List<String> roots = new ArrayList<>(Arrays.asList(getConfig().getBmsroot()));
			if (httpdlPath.toFile().exists() && !roots.contains(httpdlPath.toString())) {
				roots.add(httpdlPath.toString());
				getConfig().setBmsroot(roots.toArray(new String[roots.size()]));
			}
		}
		try {
			Class.forName("org.sqlite.JDBC");
			if(config.isUseSongInfo()) {
				infodb = new SongInformationAccessor(config.getSonginfopath());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		playdata = new PlayDataAccessor(config);

		initializeIRConfig();

		switch(config.getAudioConfig().getDriver()) {
			case PortAudio:
				try {
					audio = new PortAudioDriver(config);
				} catch(Throwable e) {
					e.printStackTrace();
					config.getAudioConfig().setDriver(DriverType.OpenAL);
				}
				break;
		}

		timer = new TimerManager();
		sound = new SystemSoundManager(this);

		if(config.isUseDiscordRPC()) {
			stateListener.add(new DiscordListener());
		}

		if(config.isUseObsWs()) {
			obsListener = new ObsListener(config);
			obsClient = obsListener.getObsClient();
			stateListener.add(obsListener);
		}
	}

	private void initializeIRConfig() {
		Array<IRStatus> irarray = new Array<IRStatus>();
		for(IRConfig irconfig : player.getIrconfig()) {
			final IRConnection ir = IRConnectionManager.getIRConnection(irconfig.getIrname());
			if(ir != null) {
				if(irconfig.getUserid().length() == 0 || irconfig.getPassword().length() == 0) {
				} else {
					try {
						IRResponse<IRPlayerData> response = ir.login(new IRAccount(irconfig.getUserid(), irconfig.getPassword(), ""));
						if(response.isSucceeded()) {
							irarray.add(new IRStatus(irconfig, ir, response.getData()));
						} else {
							logger.warn("IRへのログイン失敗 : {}", response.getMessage());
						}
					} catch (IllegalArgumentException e) {
						logger.info("trying pre-0.8.5 IR login method");
						IRResponse<IRPlayerData> response = ir.login(irconfig.getUserid(), irconfig.getPassword());
						if(response.isSucceeded()) {
							irarray.add(new IRStatus(irconfig, ir, response.getData()));
						} else {
							logger.warn("IRへのログイン失敗 : {}", response.getMessage());
						}
					}
				}
			}

		}
		ir = irarray.toArray(IRStatus.class);
		
		rivals.update(this);
	}

	public boolean hasObsListener() {
		return obsListener != null;
	}

	public ObsListener getObsListener() {
		return obsListener;
	}

	public void saveLastRecording(String reason) {
		if (config.isUseObsWs() && obsClient != null) {
			obsClient.saveLastRecording(reason);
		}
	}

	public SkinOffset getOffset(int index) {
		return offset[index];
	}

	public SongDatabaseAccessor getSongDatabase() {
		return MainLoader.getScoreDatabaseAccessor();
	}

	public SongInformationAccessor getInfoDatabase() {
		return infodb;
	}

	public PlayDataAccessor getPlayDataAccessor() {
		return playdata;
	}
	
	public RivalDataAccessor getRivalDataAccessor() {
		return rivals;
	}
	
	public RankingDataCache getRankingDataCache() {
		return ircache;
	}

	public SpriteBatch getSpriteBatch() {
		return sprite;
	}

	public PlayerResource getPlayerResource() {
		return resource;
	}

	public Config getConfig() {
		return config;
	}

	public PlayerConfig getPlayerConfig() {
		return player;
	}

	public void changeState(MainStateType state) {
		MainState newState = null;
		switch (state) {
		case MUSICSELECT:
			if (this.bmsfile != null) {
				exit();
			} else {
				newState = selector;
			}
			break;
		case DECIDE:
			newState = config.isSkipDecideScreen() ? createBMSPlayerState() : decide;
			break;
		case PLAY:
			newState = createBMSPlayerState();
			break;
		case RESULT:
			newState = result;
			break;
		case COURSERESULT:
			newState = gresult;
			break;
		case CONFIG:
			newState = keyconfig;
			break;
		case SKINCONFIG:
			newState = skinconfig;
			break;
		}

		if (newState != null && current != newState) {
			changeState(newState);
		}
		if (current.getStage() != null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(current.getStage(), input.getKeyBoardInputProcesseor()));
		} else {
			Gdx.input.setInputProcessor(input.getKeyBoardInputProcesseor());
		}
	}

	private void changeState(MainState newState) {
		newState.create();
		if(newState.getSkin() != null) {
			newState.getSkin().prepare(newState);
		}
		if(current != null) {
			current.shutdown();
			current.setSkin(null);
		}
		current = newState;
		timer.setMainState(newState);
		current.prepare();
		updateMainStateListener(0);
	}

	public void loadNewProfile(PlayerConfig pc) {
		config.setPlayername(pc.getId());
		player = pc;

		playdata = new PlayDataAccessor(config);

		initializeIRConfig();
		// Dispose MusicSelector to unallocate loaded skin
		selector.dispose();
		initializeStates();
		updateStateReferences();
		triggerLnWarning();
		setTargetList();

		changeState(selector);
		if (current.getStage() != null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(current.getStage(), input.getKeyBoardInputProcesseor()));
		} else {
			Gdx.input.setInputProcessor(input.getKeyBoardInputProcesseor());
		}

		lastConfigSave = System.nanoTime();
	}

	private MainState createBMSPlayerState() {
		if (bmsplayer != null) {
			bmsplayer.dispose();
		}
		return new BMSPlayer(this, resource);
	}

	public MainState getCurrentState() {
		return current;
	}

	public static MainStateType getStateType(MainState state) {
		if (state instanceof KeyConfiguration) {
			return MainStateType.CONFIG;
		} else if (state instanceof BMSPlayer) {
			return MainStateType.PLAY;
		} else if (state instanceof MusicSelector) {
			return MainStateType.MUSICSELECT;
		} else if (state instanceof SkinConfiguration) {
			return MainStateType.SKINCONFIG;
		} else if (state instanceof CourseResult) {
			return MainStateType.COURSERESULT;
		} else if (state instanceof MusicDecide) {
			return MainStateType.DECIDE;
		} else if (state instanceof MusicResult) {
			return MainStateType.RESULT;
		}
		return null;
	}

	public void setPlayMode(BMSPlayerMode auto) {
		this.auto = auto;

	}

	public void create() {
		final long t = System.currentTimeMillis();
		sprite = SpriteBatchHelper.createSpriteBatch();
		SkinLoader.initPixmapResourcePool(config.getSkinPixmapGen());

        try (var perf = PerformanceMetrics.get().Event("ImGui init")) {
            ImGuiRenderer.init();
        }

        try (var perf = PerformanceMetrics.get().Event("System font load")) {
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(config.getSystemfontpath()));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 24;
			systemfont = generator.generateFont(parameter);
			generator.dispose();
		} catch (GdxRuntimeException e) {
			logger.error("System Font読み込み失敗");
		}

        try (var perf = PerformanceMetrics.get().Event("Input Processor constructor")) {
			input = new BMSPlayerInputProcessor(config, player);
		}

		switch(config.getAudioConfig().getDriver()) {
		case OpenAL:
			audio = new GdxSoundDriver(config);
			break;
//		case AudioDevice:
//			audio = new GdxAudioDeviceDriver(config);
//			break;
		}
		loudnessAnalyzer = new BMSLoudnessAnalyzer(config);
    	initializeStates();
		updateStateReferences();
		MiscSettingMenu.setMain(this);
		if (bmsfile != null) {
			if(resource.setBMSFile(bmsfile, auto)) {
				changeState(MainStateType.PLAY);
			} else {
				// ダミーステートに移行してすぐexitする
				changeState(MainStateType.CONFIG);
				exit();
			}
		} else {
			changeState(MainStateType.MUSICSELECT);
		}

		logger.info("初期化時間(ms) : {}", System.currentTimeMillis() - t);

		Thread polling = new Thread(() -> {
			long time = 0;
			for (;;) {
				final long now = System.nanoTime() / 1000000;
				if (time != now) {
					time = now;
					input.poll();
				} else {
					try {
						Thread.sleep(0, 500000);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		polling.start();

        triggerLnWarning();

		setTargetList();

		Pixmap plainPixmap = new Pixmap(2,1, Pixmap.Format.RGBA8888);
		plainPixmap.drawPixel(0,0, Color.toIntBits(255,0,0,0));
		plainPixmap.drawPixel(1,0, Color.toIntBits(255,255,255,255));
		Texture plainTexture = new Texture(plainPixmap);
		black = new TextureRegion(plainTexture,0,0,1,1);
		white = new TextureRegion(plainTexture,1,0,1,1);
		plainPixmap.dispose();

		Gdx.gl.glClearColor(0, 0, 0, 1);

		if (config.isEnableIpfs()) {
			download = new MusicDownloadProcessor(config.getIpfsUrl(), (md5) -> {
				SongData[] s = getSongDatabase().getSongDatas(md5);
				String[] result = new String[s.length];
				for(int i = 0;i < result.length;i++) {
					result[i] = s[i].getPath();
				}
				return result;
			});
			download.start(null);
		}

		if (config.isEnableHttp()) {
			HttpDownloadSource httpDownloadSource = HttpDownloadProcessor.DOWNLOAD_SOURCES.get(config.getDownloadSource()).build(config);
			httpDownloadProcessor = new HttpDownloadProcessor(this, httpDownloadSource, config.getDownloadDirectory());
			DownloadTaskState.initialize(httpDownloadProcessor);
			DownloadTaskMenu.setProcessor(httpDownloadProcessor);
		}

		if(ir.length > 0) {
			ImGuiNotify.info(String.format("%d IR Connection Succeed", ir.length));

			Thread irResendProcess = new Thread(() -> {
				for (;;) {
					final long now = System.currentTimeMillis();
						try {
							List<IRSendStatus> removeIrSendStatus = new ArrayList<IRSendStatus>();

							for(IRSendStatus score : irSendStatus) {
								long timeUntilNextTry = (long)(Math.pow(4, score.retry) * 1000);
								if (score.retry != 0 && now - score.lastTry >= timeUntilNextTry) {
									score.send();
								}
								if(score.isSent) {
									removeIrSendStatus.add(score);
								}
								if(score.retry > getConfig().getIrSendCount()) {
									removeIrSendStatus.add(score);
									ImGuiNotify.error(String.format("Failed to send a score for %s %s", score.song.getTitle(), score.song.getSubtitle()));
								}
							}
							irSendStatus.removeAll(removeIrSendStatus);

							try {
								Thread.sleep(3000, 0);
							} catch (InterruptedException e) {
							}
						} catch (Exception e) {
							logger.error(e.getMessage());
						}
				}
			});
			irResendProcess.start();
		}

        lastConfigSave = System.nanoTime();
	}

	private void initializeStates() {
		resource = new PlayerResource(audio, config, player, loudnessAnalyzer);

		try (var perf = PerformanceMetrics.get().Event("MusicSelector constructor")) {
			selector = new MusicSelector(this, songUpdated);
		}

		if(player.getRequestEnable()) {
			streamController = new StreamController(selector);
			streamController.run();
		}

		decide = new MusicDecide(this);
		result = new MusicResult(this);
		gresult = new CourseResult(this);
		keyconfig = new KeyConfiguration(this);
		skinconfig = new SkinConfiguration(this, player);
	}

	private void updateStateReferences() {
		SkinMenu.init(this, player);
		SongManagerMenu.injectMusicSelector(selector);
	}

	private void triggerLnWarning() {
		String lnModeName = switch (player.getLnmode()) {
			case 1 -> "CN";
			case 2 -> "HCN";
			default -> "LN";
		};
		if (!lnModeName.equals("LN")) {
			// give them a really insistent warning
			String lnWarning = "Long Note mode is " + lnModeName + ".\n"
				+ "This is not recommended.\n"
				+ "Your scores may be incompatible with IR.\n"
				+ "You may change this in play options.";
			ImGuiNotify.warning(lnWarning, 8000);
		}
	}

	private void setTargetList() {
		Array<String> targetlist = new Array<String>(player.getTargetlist());
		for(int i = 0;i < rivals.getRivalCount();i++) {
			targetlist.add("RIVAL_" + (i + 1));
		}
		TargetProperty.setTargets(targetlist.toArray(String.class), this);
	}

	private long prevtime;

	private final StringBuilder message = new StringBuilder();

	public void render() {
//		input.poll();
		timer.update();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		current.render();
		sprite.begin();
		if (current.getSkin() != null) {
			current.getSkin().updateCustomObjects(current);
			current.getSkin().drawAllObjects(sprite, current);
		}
		sprite.end();

		final Stage stage = current.getStage();
		if (stage != null) {
			stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
			stage.draw();
		}

		// show fps
		if (showfps && systemfont != null) {
			sprite.begin();
			systemfont.setColor(Color.CYAN);
			message.setLength(0);
			systemfont.draw(sprite, message.append("FPS ").append(Gdx.graphics.getFramesPerSecond()), debugTextXpos,
					config.getResolution().height - 2);
					if(debug) {
				message.setLength(0);
				systemfont.draw(sprite, message.append("Skin Pixmap Images ").append(SkinLoader.getResource().size()), debugTextXpos,
						config.getResolution().height - 26);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Total Memory Used(MB) ").append(Runtime.getRuntime().totalMemory() / (1024 * 1024)), debugTextXpos,
						config.getResolution().height - 50);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Total Free Memory(MB) ").append(Runtime.getRuntime().freeMemory() / (1024 * 1024)), debugTextXpos,
						config.getResolution().height - 74);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Max Sprite In Batch ").append(sprite.maxSpritesInBatch), debugTextXpos,
						config.getResolution().height - 98);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Skin Pixmap Resource Size ").append(SkinLoader.getResource().size()), debugTextXpos,
						config.getResolution().height - 122);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Stagefile Pixmap Resource Size ").append(selector.getStagefileResource().size()), debugTextXpos,
						config.getResolution().height - 146);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Banner Pixmap Resource Size ").append(selector.getBannerResource().size()), debugTextXpos,
						config.getResolution().height - 170);
						if (current.getSkin() != null) {
					message.setLength(0);
					systemfont.draw(sprite, message.append("Skin Prepare Time ").append(current.getSkin().pcntPrepare), debugTextXpos,
							config.getResolution().height - 194);
					message.setLength(0);
					systemfont.draw(sprite, message.append("Skin Draw Time ").append(current.getSkin().pcntDraw), debugTextXpos,
							config.getResolution().height - 218);
					var i = 0;
					var l = current.getSkin().pcntmap.keySet().stream().mapToInt(c->c.getSimpleName().length()).max().orElse(1);
					var f = "%" + l + "s";
					message.setLength(0);
					message.append(String.format(f,"SkinObject")).append(" num // prepare cur/avg/max // draw cur/avg/max");
					systemfont.draw(sprite, message, debugTextXpos, config.getResolution().height - 242);
					var entrys = current.getSkin().pcntmap.entrySet().stream()
						.sorted((e1,e2) -> e1.getKey().getSimpleName().compareTo(e2.getKey().getSimpleName()))
						.toList();
					for (Map.Entry<Class, long[]> e : entrys) {
						message.setLength(0);
						message.append(String.format(f,e.getKey().getSimpleName())).append(" ")
						.append(e.getValue()[0]).append(" // ")
						.append(e.getValue()[1]/100).append(" / ")
						.append(e.getValue()[2]/100000).append(" / ")
						.append(e.getValue()[3]/100).append(" // ")
						.append(e.getValue()[4]/100).append(" / ")
						.append(e.getValue()[5]/100000).append(" / ")
						.append(e.getValue()[6]/100);
						systemfont.draw(sprite, message, debugTextXpos, config.getResolution().height - (266 + i * 24));
						i++;
					}
				}
			}

			sprite.end();
		}

        periodicConfigSave();

        if (config.isEnableHttp()) { DownloadTaskState.update(); }
        PerformanceMetrics.get().commit();

		imGui.start();
		imGui.render();
		imGui.end();

		// TODO renderループに入れるのではなく、MusicDownloadProcessorのListenerとして実装したほうがいいのでは
		if(download != null && download.isDownload()){
			downloadIpfsMessageRenderer(download.getMessage());
		}

		final long time = System.currentTimeMillis();
		if(time > prevtime) {
		    prevtime = time;
            current.input();
            // event - move pressed
            if (input.isMousePressed()) {
                input.setMousePressed();
                current.getSkin().mousePressed(current, input.getMouseButton(), input.getMouseX(), input.getMouseY());
            }
            // event - move dragged
            if (input.isMouseDragged()) {
                input.setMouseDragged();
                current.getSkin().mouseDragged(current, input.getMouseButton(), input.getMouseX(), input.getMouseY());
            }

            // マウスカーソル表示判定
            if(input.isMouseMoved()) {
            	input.setMouseMoved(false);
            	mouseMovedTime = time;
			}
			Gdx.input.setCursorCatched(current == bmsplayer && time > mouseMovedTime + 5000);
			// FPS表示切替
            if (input.isActivated(KeyCommand.SHOW_FPS)) {
                showfps = !showfps;
            }
            // fullscreen - windowed
            if (!input.getKeyState(Input.Keys.ALT_LEFT) && !input.getKeyState(Input.Keys.ALT_RIGHT) && input.isActivated(KeyCommand.SWITCH_SCREEN_MODE)) {
                boolean fullscreen = Gdx.graphics.isFullscreen();

                if (fullscreen) {
					// Restore window decorations
					Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
					Gdx.graphics.setUndecorated(false);
                    Gdx.graphics.setWindowedMode(config.getWindowWidth(), config.getWindowHeight());

					// Try and find the highest resolution display mode available, otherwise use the current mode
					Graphics.DisplayMode maxResOrCurrent = Arrays.stream(Gdx.graphics.getDisplayModes())
							.max(Comparator.comparingInt((Graphics.DisplayMode mode) -> mode.width)
									.thenComparingInt(mode -> mode.height)
									.thenComparingInt(mode -> mode.refreshRate))
							.orElse(Gdx.graphics.getDisplayMode());

					// Center window on screen
					int windowX = (maxResOrCurrent.width / 2) - (config.getWindowWidth() / 2);
					int windowY = (maxResOrCurrent.height / 2) - (config.getWindowHeight() / 2);
					// Handle max res contents pushing the decorations off screen
					if (windowY == 0) {
						windowY += 32;
					}

					graphics.getWindow().setPosition(windowX, windowY);

                } else {
					// Try and find the best resolution mode that fits the window size, skins will behave strangely if
					// fullscreened with the wrong display mode
					Graphics.DisplayMode windowResOrCurrent = Arrays.stream(Gdx.graphics.getDisplayModes())
							.filter(mode -> mode.width == config.getWindowWidth() && mode.height == config.getWindowHeight())
							.max(Comparator.comparingInt(mode -> mode.refreshRate))
							.orElse(Gdx.graphics.getDisplayMode());
                    Gdx.graphics.setFullscreenMode(windowResOrCurrent);
                }
                config.setDisplaymode(fullscreen ? Config.DisplayMode.WINDOW : Config.DisplayMode.FULLSCREEN);
            }

            // if (input.getFunctionstate()[4] && input.getFunctiontime()[4] != 0) {
            // int resolution = config.getResolution();
            // resolution = (resolution + 1) % RESOLUTION.length;
            // if (config.isFullscreen()) {
            // Gdx.graphics.setWindowedMode((int) RESOLUTION[resolution].width,
            // (int) RESOLUTION[resolution].height);
            // Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            // Gdx.graphics.setFullscreenMode(currentMode);
            // }
            // else {
            // Gdx.graphics.setWindowedMode((int) RESOLUTION[resolution].width,
            // (int) RESOLUTION[resolution].height);
            // }
            // config.setResolution(resolution);
            // input.getFunctiontime()[4] = 0;
            // }

            // screen shot
            if (input.isActivated(KeyCommand.SAVE_SCREENSHOT)) {
                if (screenshot == null || !screenshot.isAlive()) {
            		final byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight(), true);
                    screenshot = new Thread(() -> {
                		// 全ピクセルのアルファ値を255にする(=透明色を無くす)
                		for(int i = 3;i < pixels.length;i+=4) {
                			pixels[i] = (byte) 0xff;
                		}
                    	new ScreenShotFileExporter().send(current, pixels);
                    });
                    screenshot.start();
                    this.saveLastRecording("ON_SCREENSHOT");
                }
            }

            if (input.isActivated(KeyCommand.POST_TWITTER)) {
                if (screenshot == null || !screenshot.isAlive()) {
            		final byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight(), false);
                    screenshot = new Thread(() -> {
                		// 全ピクセルのアルファ値を255にする(=透明色を無くす)
                		for(int i = 3;i < pixels.length;i+=4) {
                			pixels[i] = (byte) 0xff;
                		}
                    	new ScreenShotTwitterExporter(player).send(current, pixels);
                    });
                    screenshot.start();
                }
            }

			if (input.isActivated(KeyCommand.TOGGLE_MOD_MENU)) {
				imGui.toggleMenu();
			}

			if (download != null && download.getDownloadpath() != null) {
            	this.updateSong(download.getDownloadpath());
            	download.setDownloadpath(null);
            }
			if (updateSong != null && !updateSong.isAlive()) {
				selector.getBarManager().updateBar();
				updateSong = null;
			}
        }
	}

	public void dispose() {
		saveConfig();

		if (bmsplayer != null) {
			bmsplayer.dispose();
		}
		if (selector != null) {
			selector.dispose();
		}
		if (streamController != null) {
		    streamController.dispose();
        }
		if (decide != null) {
			decide.dispose();
		}
		if (result != null) {
			result.dispose();
		}
		if (gresult != null) {
			gresult.dispose();
		}
		if (keyconfig != null) {
			keyconfig.dispose();
		}
		if (skinconfig != null) {
			skinconfig.dispose();
		}
		imGui.dispose();
		resource.dispose();
//		input.dispose();
		SkinLoader.getResource().dispose();
		ShaderManager.dispose();
		if (download != null) {
			download.dispose();
		}
		if (loudnessAnalyzer != null) {
			loudnessAnalyzer.shutdown();
		}

		logger.info("全リソース破棄完了");
	}

	public void pause() {
		current.pause();
	}

	public void resize(int width, int height) {
		current.resize(width, height);
	}

	public void resume() {
		current.resume();
	}

	public void saveConfig(){
		Config.write(config);
		PlayerConfig.write(config.getPlayerpath(), player);
		logger.info("設定情報を保存");
	}

    private long lastConfigSave = 0;
    private Thread configWrite;

    private void periodicConfigSave() {
        // let's not start anything heavy during play
        if (current instanceof BMSPlayer) { return; }

        // save once every 5 minutes
        long now = System.nanoTime();
        if ((now - lastConfigSave) < 2 * 60 * 1000000000L) { return; }

        if (configWrite != null && configWrite.isAlive()) {
            logger.error("Couldn't write config files - save process is stuck.");
            return;
        }

        lastConfigSave = now;

        // the write are quite slow but we can do them on a separate thread;
        // we still serialize the configs into json on the
        // main thread to avoid multithreading issues
        final String configJson = Config.getConfigJson(config);
        final String playerConfigJson = PlayerConfig.getConfigJson(player);
        configWrite = new Thread(() -> {
            Config.write(config, configJson);
            PlayerConfig.write(config.getPlayerpath(), player, playerConfigJson);
        });
        configWrite.start();
    }

	public void exit() {
		Gdx.app.exit();
	}

	public BMSPlayerInputProcessor getInputProcessor() {
		return input;
	}

	public AudioDriver getAudioProcessor() {
		return audio;
	}

	public IRStatus[] getIRStatus() {
		return ir;
	}

	public SystemSoundManager getSoundManager() {
		return sound;
	}

	public MusicDownloadProcessor getMusicDownloadProcessor(){
		return download;
	}

	public ImGuiRenderer getImGui() {
		return imGui;
	}

	public void setImGui(ImGuiRenderer imGui) {
		this.imGui = imGui;
	}

	public void updateMainStateListener(int status) {
		for(MainStateListener listener : stateListener) {
			listener.update(current, status);
		}
	}

	public long getPlayTime() {
		return System.currentTimeMillis() - boottime;
	}

	public Calendar getCurrnetTime() {
		cl.setTimeInMillis(System.currentTimeMillis());
		return cl;
	}

	public TimerManager getTimer() {
		return timer;
	}

	public long getStartTime() {
		return timer.getStartTime();
	}

	public long getStartMicroTime() {
		return timer.getStartMicroTime();
	}

	public long getNowTime() {
		return timer.getNowTime();
	}

	public long getNowTime(int id) {
		return timer.getNowTime(id);
	}

	public long getNowMicroTime() {
		return timer.getNowMicroTime();
	}

	public long getNowMicroTime(int id) {
		return timer.getNowMicroTime(id);
	}

	public long getTimer(int id) {
		return getMicroTimer(id) / 1000;
	}

	public long getMicroTimer(int id) {
		return timer.getMicroTimer(id);
	}

	public boolean isTimerOn(int id) {
		return getMicroTimer(id) != Long.MIN_VALUE;
	}

	public void setTimerOn(int id) {
		timer.setTimerOn(id);
	}

	public void setTimerOff(int id) {
		setMicroTimer(id, Long.MIN_VALUE);
	}

	public void setMicroTimer(int id, long microtime) {
		timer.setMicroTimer(id, microtime);
	}

	public HttpDownloadProcessor getHttpDownloadProcessor() {
		return httpDownloadProcessor;
	}

	public void setHttpDownloadProcessor(HttpDownloadProcessor httpDownloadProcessor) {
		this.httpDownloadProcessor = httpDownloadProcessor;
	}

	public void switchTimer(int id, boolean on) {
		timer.switchTimer(id, on);
	}

	private UpdateThread updateSong;

	public void updateSong(String path) {
		if (updateSong == null || !updateSong.isAlive()) {
			updateSong = new SongUpdateThread(path);
			updateSong.start();
		} else {
			logger.warn("楽曲更新中のため、更新要求は取り消されました");
		}
	}

	public void updateTable(TableBar reader) {
		if (updateSong == null || !updateSong.isAlive()) {
			updateSong = new TableUpdateThread(reader);
			updateSong.start();
		} else {
			logger.warn("楽曲更新中のため、更新要求は取り消されました");
		}
	}

	private UpdateThread downloadIpfs;

	public void downloadIpfsMessageRenderer(String message) {
		if (downloadIpfs == null || !downloadIpfs.isAlive()) {
			downloadIpfs = new DownloadMessageThread(message);
			downloadIpfs.start();
		}
	}

	public static String getVersion() {
		return VERSION;
	}

	abstract class UpdateThread extends Thread {

		protected String message;

		public UpdateThread(String message) {
			this.message = message;
		}
	}

	/**
	 * 楽曲データベース更新用スレッド
	 *
	 * @author exch
	 */
	class SongUpdateThread extends UpdateThread {

		private final String path;

		public SongUpdateThread(String path) {
			super("updating folder : " + (path == null ? "ALL" : path));
			this.path = path;
		}

		public void run() {
			ImGuiNotify.info(this.message);
			getSongDatabase().updateSongDatas(path, config.getBmsroot(), false, getInfoDatabase());
		}
	}

	/**
	 * 難易度表更新用スレッド
	 *
	 * @author exch
	 */
	class TableUpdateThread extends UpdateThread {

		private final TableBar accessor;

		public TableUpdateThread(TableBar bar) {
			super("updating table : " + bar.getAccessor().name);
			accessor = bar;
		}

		public void run() {
			ImGuiNotify.info(this.message);
			TableData td = accessor.getAccessor().read();
			if (td != null) {
				accessor.getAccessor().write(td);
				accessor.setTableData(td);
			}
		}
	}

	class DownloadMessageThread extends UpdateThread {
		public DownloadMessageThread(String message) {
			super(message);
		}

		public void run() {
			while (download != null && download.isDownload() && download.getMessage() != null) {
				ImGuiNotify.info(download.getMessage());
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static class IRStatus {

		public final IRConfig config;
		public final IRConnection connection;
		public final IRPlayerData player;

		public IRStatus(IRConfig config, IRConnection connection, IRPlayerData player) {
			this.config = config;
			this.connection = connection;
			this.player = player;
		}
	}

	public static class IRSendStatus {
		public final IRConnection ir;
		public final SongData song;
		public final ScoreData score;
		public int retry = 0;
		public long lastTry = 0;
		public boolean isSent = false;
		public IRSendStatus(IRConnection ir, SongData song, ScoreData score) {
			this.ir = ir;
			this.song = song;
			this.score = score;
		}

		public boolean send() {
			logger.info("IRへスコア送信中 : {}", song.getTitle());
			lastTry = System.currentTimeMillis();
			IRResponse<Object> send1 = ir.sendPlayData(new IRChartData(song), new bms.player.beatoraja.ir.IRScoreData(score));
			retry++;
			if(send1.isSucceeded()) {
				logger.info("IRスコア送信完了 : {}", song.getTitle());
				isSent = true;
				return true;
			} else {
				logger.warn("IRスコア送信失敗 : {}", send1.getMessage());
				return false;
			}

		}
	}
}
