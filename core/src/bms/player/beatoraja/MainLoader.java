package bms.player.beatoraja;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import de.damios.guacamole.gdx.log.LoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;

import bms.player.beatoraja.exceptions.PlayerConfigException;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import imgui.ImGui;
import imgui.ImGuiIO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import bms.player.beatoraja.AudioConfig.DriverType;
import bms.player.beatoraja.ir.IRConnectionManager;
import bms.player.beatoraja.launcher.PlayConfigurationView;
import bms.player.beatoraja.song.SQLiteSongDatabaseAccessor;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.song.SongDatabaseAccessor;
import bms.player.beatoraja.song.SongUtils;
import org.slf4j.jul.JULServiceProvider;

/**
 * 起動用クラス
 *
 * @author exch
 */
public class MainLoader extends Application {
	private static final Logger logger = LoggerFactory.getLogger(MainLoader.class);

	private static final boolean ALLOWS_32BIT_JAVA = false;

	private static SongDatabaseAccessor songdb;

	private static final Set<String> illegalSongs = new HashSet<String>();

	private static Path bmsPath;

	private static VersionChecker version;

	public static void main(String[] args) {

		if(!ALLOWS_32BIT_JAVA && !System.getProperty( "os.arch" ).contains( "64")) {
			JOptionPane.showMessageDialog(null, "This Application needs 64bit-Java.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		try {
			rootLogger.addHandler(new FileHandler("beatoraja_log.xml"));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		BMSPlayerMode auto = null;
		for (String s : args) {
			if (s.startsWith("-")) {
				if (s.equals("-a")) {
					auto = BMSPlayerMode.AUTOPLAY;
				}
				if (s.equals("-p")) {
					auto = BMSPlayerMode.PRACTICE;
				}
				if (s.equals("-r") || s.equals("-r1")) {
					auto = BMSPlayerMode.REPLAY_1;
				}
				if (s.equals("-r2")) {
					auto = BMSPlayerMode.REPLAY_2;
				}
				if (s.equals("-r3")) {
					auto = BMSPlayerMode.REPLAY_3;
				}
				if (s.equals("-r4")) {
					auto = BMSPlayerMode.REPLAY_4;
				}
				if (s.equals("-s")) {
					auto = BMSPlayerMode.PLAY;
				}
			} else {
				bmsPath = Paths.get(s);
				if(auto == null) {
					auto = BMSPlayerMode.PLAY;
				}
			}
		}



		if (Files.exists(Config.configpath) && (bmsPath != null || auto != null)) {
			IRConnectionManager.getAllAvailableIRConnectionName();
			play(bmsPath, auto, true, null, null, bmsPath != null);
		} else {
			launch(args);
		}
	}

	public static void play(Path bmsPath, BMSPlayerMode playerMode, boolean forceExit, Config config, PlayerConfig player, boolean songUpdated) {
		//configuratorStage.setIconified(true);
		if(config == null) {
            try {
                config = Config.read();
            } catch (PlayerConfigException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        for (SongData song : getScoreDatabaseAccessor().getSongDatas(SongUtils.illegalsongs)) {
            MainLoader.putIllegalSong(song.getSha256());
        }

		if(illegalSongs.size() > 0) {
			JOptionPane.showMessageDialog(null, "This Application detects " + illegalSongs.size() + " illegal BMS songs. \n Remove them, update song database and restart.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		try {
			MainController main = new MainController(bmsPath, config, player, playerMode, songUpdated);

			Lwjgl3ApplicationConfiguration gdxConfig = new Lwjgl3ApplicationConfiguration();
            // This line is provided for macos-fix, the original issue is mac dropped the OpenGL full support,
            // according to the document from libgdx, mac machine now only supports OpenGL 3.2 and needs to emulate GL30
            // to make libgdx work
            // However, this line may break some old machine which has no OpenGL 3.2 support. It seems that libgdx defaults
            // to emulate GL20, this version gap might be danger. Therefore, this line is wrapped as macos only
            // NOTE: SpriteBatchHelper is also macos only, it degenerates to beatoraja's default behaviour when executing
            // on other platforms
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                gdxConfig.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2);
            }
            final int w = config.getResolution().width;
            final int h = config.getResolution().height;
			config.setWindowWidth(w);
			config.setWindowHeight(h);
            String targetMonitorName = config.getMonitorName();
            Graphics.Monitor targetMonitor = null;
			Graphics.DisplayMode gdxDisplayMode;
            if (targetMonitorName != null && !targetMonitorName.isEmpty()) {
                Optional<Graphics.Monitor> optMonitor = Arrays.stream(Lwjgl3ApplicationConfiguration.getMonitors())
                        .filter(monitor -> String.format("%s [%s, %s]", monitor.name, Integer.toString(monitor.virtualX), Integer.toString(monitor.virtualY)).equals(targetMonitorName))
                        .findAny();
                if (optMonitor.isPresent()) {
                    targetMonitor = optMonitor.get();
                }
            }
            if (config.getDisplaymode() == Config.DisplayMode.FULLSCREEN) {
                Graphics.DisplayMode d = null;
                Graphics.DisplayMode[] displayModes = Lwjgl3ApplicationConfiguration.getDisplayModes();
                if (targetMonitor != null) {
                    displayModes = Lwjgl3ApplicationConfiguration.getDisplayModes(targetMonitor);
                }
                for (Graphics.DisplayMode display : displayModes) {
                    System.out.println("available DisplayMode : w - " + display.width + " h - " + display.height
                            + " refresh - " + display.refreshRate + " color bit - " + display.bitsPerPixel);
                    if (display.width == w
                            && display.height == h
                            && (d == null || (d.refreshRate <= display.refreshRate && d.bitsPerPixel <= display.bitsPerPixel))) {
                        d = display;
                    }
                }

				// We need to do the following hack to enter full-screen mode on specified monitor
				// 1. enter borderless mode first
				// 2. move the window position to target monitor's origin point
				// 3. enter full-screen mode manually after window has created
				gdxConfig.setDecorated(false);
				gdxConfig.setWindowedMode(w, h);
				if (targetMonitor != null) {
					int posX = targetMonitor.virtualX;
					int posY = targetMonitor.virtualY;
					gdxConfig.setWindowPosition(posX, posY);
				}
				gdxDisplayMode = d;
				if (gdxDisplayMode == null) {
					logger.warn("Current resolution({}x{}) is not compatible with current monitor, full-screen mode might be malfunctioning", w, h);
					gdxDisplayMode = targetMonitor == null ? Lwjgl3ApplicationConfiguration.getDisplayMode() : Lwjgl3ApplicationConfiguration.getDisplayMode(targetMonitor);
				}
            } else {
                if (config.getDisplaymode() == Config.DisplayMode.BORDERLESS) {
                    gdxConfig.setDecorated(false);
                    //System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
                } else {
					gdxConfig.setDecorated(true);
				}
                gdxConfig.setWindowedMode(w, h);
                if (targetMonitor != null) {
                    Graphics.DisplayMode d = Lwjgl3ApplicationConfiguration.getDisplayMode(targetMonitor);
					if (config.getDisplaymode() == Config.DisplayMode.BORDERLESS) {
						int posX = targetMonitor.virtualX;
						int posY = targetMonitor.virtualY;
						gdxConfig.setWindowPosition(posX, posY);
					}
					gdxDisplayMode = d;
                } else {
					gdxDisplayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
                }
            }
            // vSync
            gdxConfig.useVsync(config.isVsync());
            gdxConfig.setIdleFPS(config.getMaxFramePerSecond());
            gdxConfig.setForegroundFPS(config.getMaxFramePerSecond());
            gdxConfig.setTitle(MainController.getVersion());

			gdxConfig.setAudioConfig(config.getAudioConfig().getDeviceSimultaneousSources(), config.getAudioConfig().getDeviceBufferSize(), 1);

			Config.DisplayMode displaymode = config.getDisplaymode();
			//new Lwjgl3Application(main, gdxConfig);
			Graphics.DisplayMode finalGdxDisplayMode = gdxDisplayMode;
			new Lwjgl3Application(new ApplicationListener() {
				public void resume() {
					main.resume();
				}

				public void resize(int width, int height) {
					main.resize(width, height);
				}

				public void render() {
                    try (var perf = PerformanceMetrics.get().Watch("render")) {
                        main.render();
                    }
                }

				public void pause() {
					main.pause();
				}

				public void dispose() {
					main.dispose();
				}

				public void create() {
					logger.info("Starting {}", Version.versionLong);
					logger.info("[Build info] Build date: {}, Commit: {}", Version.getBuildDate(), Version.getGitCommitHash());
					main.create();
					if (displaymode == Config.DisplayMode.FULLSCREEN) {
						Gdx.graphics.setFullscreenMode(finalGdxDisplayMode);
					}
				}
			}, gdxConfig);
			//System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("{} : {}", e.getClass().getName(), e.getMessage());
		}
		System.exit(0);
	}

	public static Graphics.DisplayMode[] getAvailableDisplayMode() {
		return Lwjgl3ApplicationConfiguration.getDisplayModes();
	}

	public static Graphics.DisplayMode getDesktopDisplayMode() {
		return Lwjgl3ApplicationConfiguration.getDisplayMode();
	}

	public static SongDatabaseAccessor getScoreDatabaseAccessor() {
		if(songdb == null) {
			try {
				Config config = Config.read();
				Class.forName("org.sqlite.JDBC");
				songdb = new SQLiteSongDatabaseAccessor(config.getSongpath(), config.getBmsroot());
			} catch (ClassNotFoundException | PlayerConfigException e) {
				logger.error("Failed to access score database: {}", e.getLocalizedMessage());
			}
        }
		return songdb;
	}

	public static VersionChecker getVersionChecker() {
		if(version == null) {
			version = new GithubVersionChecker();
		}
		return version;
	}

	public static void setVersionChecker(VersionChecker version) {
		if(version != null) {
			MainLoader.version = version;
		}
	}

	public static Path getBMSPath() {
		return bmsPath;
	}

	public static void putIllegalSong(String hash) {
		illegalSongs.add(hash);
	}

	public static String[] getIllegalSongs() {
		return illegalSongs.toArray(new String[illegalSongs.size()]);
	}

	public static int getIllegalSongCount() {
		return illegalSongs.size();
	}

	@Override
	public void start(javafx.stage.Stage primaryStage) throws Exception {
        Config config;
        try {
            config = Config.read();
        } catch (PlayerConfigException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Config error");
			alert.setHeaderText("Config failed to load");
			alert.setContentText(String.format("Failed to load config: %s", e.getMessage()));
			alert.showAndWait();
			config = Config.validateConfig(new Config());
        }

        try {
//			final long t = System.currentTimeMillis();
			ResourceBundle bundle = ResourceBundle.getBundle("resources.UIResources");
			FXMLLoader loader = new FXMLLoader(
					MainLoader.class.getResource("/bms/player/beatoraja/launcher/PlayConfigurationView.fxml"), bundle);
			VBox stackPane = (VBox) loader.load();
			PlayConfigurationView bmsinfo = (PlayConfigurationView) loader.getController();
			bmsinfo.setBMSInformationLoader(this);
			bmsinfo.update(config);
			Scene scene = new Scene(stackPane, stackPane.getPrefWidth(), stackPane.getPrefHeight());
			primaryStage.setScene(scene);
			primaryStage.setTitle(MainController.getVersion() + " configuration");
			primaryStage.setOnCloseRequest((event) -> {
				bmsinfo.exit();
			});
			primaryStage.show();
//			logger.info("初期化時間(ms) : " + (System.currentTimeMillis() - t));

		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public interface VersionChecker {
		public String getMessage();
		public String getDownloadURL();
	}

	private static class GithubVersionChecker implements VersionChecker {

		private String dlurl;
		private String message;

		public String getMessage() {
			if(message == null) {
				getInformation();
			}
			return message;
		}

		public String getDownloadURL() {
			if(message == null) {
				getInformation();
			}
			return dlurl;
		}

		private void getInformation() {
			try {
				URL url = new URL("https://api.github.com/repos/seraxis/lr2oraja-endlessdream/releases/latest");
				ObjectMapper mapper = new ObjectMapper();
				GithubLastestRelease lastestData = mapper.readValue(url, GithubLastestRelease.class);
				final String name = lastestData.name;
				if (Version.compareToString(name) == 0) {
					message = "Already on the latest version";
				} else if (Version.compareToString(name) == -1) {
					message = String.format("Version [%s] is available to download", name);
					dlurl = lastestData.html_url;
				} else {
                    // TODO: can we actually check if the current build is greater than the previous one by commit hash or build date? Doesn't seem possible with the latter
                    message = "On Unstable Development Build [" + Version.getGitCommitHash() + "] for " + Version.getVersion();
                    dlurl = "https://github.com/seraxis/lr2oraja-endlessdream/releases/tag/pre-release";
                }
			} catch (Exception e) {
				logger.warn("最新版URL取得時例外:{}", e.getMessage());
				message = "バージョン情報を取得できませんでした";
			}
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	static class GithubLastestRelease{
		public String html_url;
		public String name;
	}

}
