package bms.player.beatoraja;

import static bms.player.beatoraja.Resolution.*;
import static bms.player.beatoraja.obs.ObsWsClient.ObsRecordingMode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.system.RobustFile;
import java.util.Map;
import java.util.HashMap;

import bms.player.beatoraja.exceptions.PlayerConfigException;
import bms.tool.mdprocessor.HttpDownloadProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.SerializationException;


/**
 * 各種設定項目。config.jsonで保持される
 *
 * @author exch
 */
public class Config implements Validatable {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	
	/**
	 * 旧コンフィグパス。そのうち削除
	 */
	static final Path configpath_old = Paths.get("config.json");
	/**
	 * コンフィグパス(UTF-8)
	 */
	static final Path configpath = Paths.get("config_sys.json");	

	/**
	 * 選択中のプレイヤー名
	 */
	private String playername;

    /**
     * Last version boot, used for dynamically displaying changelogs
     */
    private String lastBootedVersion = "";
    /**
	 * ディスプレイモード
	 */
	private DisplayMode displaymode = DisplayMode.WINDOW;
	/**
	 * 垂直同期
	 */
	private boolean vsync;
	/**
	 * 解像度
	 */
	private Resolution resolution = HD;

	private boolean useResolution = true;
	private int windowWidth = 1280;
	private int windowHeight = 720;

	/**
	 * フォルダランプの有効/無効
	 */
	private boolean folderlamp = true;

	/**
	 * オーディオコンフィグ
	 */
	private AudioConfig audio;

	/**
	 * 最大FPS。垂直同期OFFの時のみ有効
	 */
	private int maxFramePerSecond = 240;

	private int prepareFramePerSecond = 0;
	/**
	 * 検索バー同時表示上限数
	 */
	private int maxSearchBarCount = 10;
	/**
	 * When selecting a song, if set to true, game changes to play scene instead of decide scene
	 */
	private boolean skipDecideScreen = false;
	/**
	 * 所持していない楽曲バーを表示するかどうか
	 */
	private boolean showNoSongExistingBar = true;
	/**
	 * 選曲バー移動速度の最初
	 */
	private int scrolldurationlow = 300;
	/**
	 * 選曲バー移動速度の2つ目以降
	 */
	private int scrolldurationhigh = 50;
	/**
	 * 選曲バーとレーンカバーのアナログスクロール
	 */
	private boolean analogScroll = true;
	/**
	 * 選曲バー移動速度に関連（アナログスクロール）
	 */
	private int analogTicksPerScroll = 3;

	/**
	 * プレビュー再生
	 */
	private SongPreview songPreview = SongPreview.LOOP;
	/**
	 * スキン画像のキャッシュイメージを作成するかどうか
	 */
    private boolean cacheSkinImage = false;
    /**
     * songinfoデータベースを使用するかどうか
     */
    private boolean useSongInfo = true;

	private String songpath = SONGPATH_DEFAULT;
	public static final String SONGPATH_DEFAULT = "songdata.db";

	private String songinfopath = SONGINFOPATH_DEFAULT;
	public static final String SONGINFOPATH_DEFAULT = "songinfo.db";

	private String tablepath = TABLEPATH_DEFAULT;
	public static final String TABLEPATH_DEFAULT = "table";

	private String playerpath = PLAYERPATH_DEFAULT;
	public static final String PLAYERPATH_DEFAULT = "player";

	private String skinpath = SKINPATH_DEFAULT;
	public static final String SKINPATH_DEFAULT = "skin";

	private String bgmpath = "bgm";

	private String soundpath = "sound";

	private String systemfontpath = "font/VL-Gothic-Regular.ttf";
	private String messagefontpath = "font/VL-Gothic-Regular.ttf";
	/**
	 * BMSルートディレクトリパス
	 */
	private String[] bmsroot = new String[0];
	/**
	 * 難易度表URL
	 */
	private String[] tableURL = DEFAULT_TABLEURL;

	private String[] availableURL = AVAILABLE_TABLEURL;
	/**
	 * BGA表示
	 */
	private int bga = BGA_ON;
	public static final int BGA_ON = 0;
	public static final int BGA_AUTO = 1;
	public static final int BGA_OFF = 2;
	/**
	 * BGA拡大
	 */
	private int bgaExpand = BGAEXPAND_KEEP_ASPECT_RATIO;
	public static final int BGAEXPAND_FULL = 0;
	public static final int BGAEXPAND_KEEP_ASPECT_RATIO = 1;
	public static final int BGAEXPAND_OFF = 2;

	private int frameskip = 1;

	private boolean updatesong = false;

	private int skinPixmapGen = 4;
	private int stagefilePixmapGen = 2;
	private int bannerPixmapGen = 2;
	private int songResourceGen = 1;

	private boolean enableIpfs = true;
	private String ipfsurl = "https://gateway.ipfs.io/";

	private boolean enableHttp = true;
	private String downloadSource = HttpDownloadProcessor.getDefaultDownloadSource().getName();
	// Only for passing parameter, not used as a config option
	private String defaultDownloadURL = HttpDownloadProcessor.getDefaultDownloadSource().getDefaultURL();
	private String overrideDownloadURL = "";
	private String downloadDirectory = DEFAULT_DOWNLOAD_DIRECTORY;
	public static final String DEFAULT_DOWNLOAD_DIRECTORY = "http_download";

	private int irSendCount = 5;

	private boolean useDiscordRPC = false;
	private boolean setClipboardScreenshot = false;
	private String monitorName = "";
    private int webhookOption = 0; // 0 - Off, 1 - Image, 2 - Rich
    private String webhookName = "";
    private String webhookAvatar = "";
	/**
	 * Discord webhook urls
	 */
	private String[] webhookUrl = new String[0];

	/**
	 * OBS WebSocket Control
	 */
	private boolean useObsWs = false;
	private String obsWsHost = "localhost";
	private int obsWsPort = 4455;
	private String obsWsPass = "";
	private int obsWsRecStopWait = 5000;
	private int obsWsRecMode = 0;
	private HashMap<String, String> obsScenes = new HashMap<>();
	private HashMap<String, String> obsActions = new HashMap<>();

	/**
	 * Bank of available tables
	 *
	 * A users tableurl list is subtracted from this list to avoid unintentional duplicate table entries
	 */
    public static final String[] AVAILABLE_TABLEURL = {
			//
			// Default list
			//
			// stardust, starlight, satellite, stella
			"https://mqppppp.neocities.org/StardustTable.html",
			"https://djkuroakari.github.io/starlighttable.html",
			"https://stellabms.xyz/sl/table.html",
			"https://stellabms.xyz/st/table.html",
			// normal 1/2 insane 1/2
			"https://darksabun.club/table/archive/normal1/",
			"https://darksabun.club/table/archive/insane1/",
			"http://rattoto10.jounin.jp/table.html",
			"http://rattoto10.jounin.jp/table_insane.html",
			// overjoy
			"https://rattoto10.jounin.jp/table_overjoy.html",
			//
			// Optional list
			//
			// stream + chordjack
			"https://lets-go-time-hell.github.io/code-stream-table/",
			"https://lets-go-time-hell.github.io/Arm-Shougakkou-table/",
			"https://su565fx.web.fc2.com/Gachimijoy/gachimijoy.html",
			// stellaverse quirked up
			"https://stellabms.xyz/so/table.html",
			"https://stellabms.xyz/sn/table.html",
			// osu
			"https://air-afother.github.io/osu-table/",
			// AI
			"https://bms.hexlataia.xyz/tables/ai.html",
			// Library
			"https://bms.hexlataia.xyz/tables/db.html",
			"https://stellabms.xyz/upload.html",
			"https://exturbow.github.io/github.io/index.html",
			"https://bms.hexlataia.xyz/tables/olduploader.html",
			//"http://upl.konjiki.jp/",
			// beginner
			"http://fezikedifficulty.futene.net/list.html",
			// LN
			"https://ladymade-star.github.io/luminous/table.html",
			"https://vinylhouse.web.fc2.com/lntougou/difficulty.html",
			"http://flowermaster.web.fc2.com/lrnanido/gla/LN.html",
			"https://skar-wem.github.io/ln/",
			"http://cerqant.web.fc2.com/zindy/table.html",
			"https://notepara.com/glassist/lnoj",
			// Scratch
			"https://egret9.github.io/Scramble/",
			"http://minddnim.web.fc2.com/sara/3rd_hard/bms_sara_3rd_hard.html",
			// delay
			"https://lets-go-time-hell.github.io/Delay-joy-table/",
			"https://kamikaze12345.github.io/github.io/delaytrainingtable/table.html",
			"https://wrench616.github.io/Delay/",
			// high difficulty
			"https://darksabun.club/table/archive/old-overjoy/",
			"https://monibms.github.io/Dystopia/dystopia.html",
			"https://www.firiex.com/tables/joverjoy",
			// hard judge
			"https://plyfrm.github.io/table/timing/",
			// artist search
			"https://plyfrm.github.io/table/bmssearch/index.html",
			// DP
			"https://yaruki0.net/DPlibrary/",
			"https://stellabms.xyz/dp/table.html",
			"https://stellabms.xyz/dpst/table.html",
			"https://deltabms.yaruki0.net/table/data/dpdelta_head.json",
			"https://deltabms.yaruki0.net/table/data/insane_head.json",
			"http://ereter.net/dpoverjoy/",
			// Stella Extensions
			"https://notmichaelchen.github.io/stella-table-extensions/satellite-easy.html",
			"https://notmichaelchen.github.io/stella-table-extensions/satellite-normal.html",
			"https://notmichaelchen.github.io/stella-table-extensions/satellite-hard.html",
			"https://notmichaelchen.github.io/stella-table-extensions/satellite-fullcombo.html",
			"https://notmichaelchen.github.io/stella-table-extensions/stella-easy.html",
			"https://notmichaelchen.github.io/stella-table-extensions/stella-normal.html",
			"https://notmichaelchen.github.io/stella-table-extensions/stella-hard.html",
			"https://notmichaelchen.github.io/stella-table-extensions/stella-fullcombo.html",
			"https://notmichaelchen.github.io/stella-table-extensions/dp-satellite-easy.html",
			"https://notmichaelchen.github.io/stella-table-extensions/dp-satellite-normal.html",
			"https://notmichaelchen.github.io/stella-table-extensions/dp-satellite-hard.html",
			"https://notmichaelchen.github.io/stella-table-extensions/dp-satellite-fullcombo.html",
			// Walkure
			"http://walkure.net/hakkyou/for_glassist/bms/?lamp=easy",
			"http://walkure.net/hakkyou/for_glassist/bms/?lamp=normal",
			"http://walkure.net/hakkyou/for_glassist/bms/?lamp=hard",
			"http://walkure.net/hakkyou/for_glassist/bms/?lamp=fc",
    };

	private static final String[] DEFAULT_TABLEURL = {
			// stardust, starlight, satellite, stella
			"https://mqppppp.neocities.org/StardustTable.html",
			"https://djkuroakari.github.io/starlighttable.html",
			"https://stellabms.xyz/sl/table.html",
			"https://stellabms.xyz/st/table.html",
			// normal 1/2 insane 1/2
			"https://darksabun.club/table/archive/normal1/",
			"https://darksabun.club/table/archive/insane1/",
			"http://rattoto10.jounin.jp/table.html",
			"http://rattoto10.jounin.jp/table_insane.html",
			// overjoy
			"https://rattoto10.jounin.jp/table_overjoy.html"
	};

	public Config() {
	}

	public String getPlayername() {
		return playername;
	}

	public void setPlayername(String playername) {
		this.playername = playername;
	}

    public String getLastBootedVersion() { return lastBootedVersion; }

    public void setLastBootedVersion(String lastBootedVersion) { this.lastBootedVersion = lastBootedVersion; }

	public boolean isVsync() {
		return vsync;
	}

	public void setVsync(boolean vsync) {
		this.vsync = vsync;
	}

	public int getBga() {
		return bga;
	}

	public void setBga(int bga) {
		this.bga = bga;
	}

	public AudioConfig getAudioConfig() {
		return audio;
	}

	public void setAudioConfig(AudioConfig audio) {
		this.audio = audio;
	}

	public int getMaxFramePerSecond() {
		return maxFramePerSecond;
	}

	public void setMaxFramePerSecond(int maxFramePerSecond) {
		this.maxFramePerSecond = maxFramePerSecond;
	}

	public int getPrepareFramePerSecond() {
		return prepareFramePerSecond;
	}

	public void setPrepareFramePerSecond(int prepareFramePerSecond) {
		this.prepareFramePerSecond = prepareFramePerSecond;
	}

	public String[] getBmsroot() {
		return bmsroot;
	}

	public void setBmsroot(String[] bmsroot) {
		this.bmsroot = bmsroot;
	}

	public String[] getTableURL() {
		return tableURL;
	}

	public void setTableURL(String[] tableURL) {
		this.tableURL = tableURL;
	}

	public String[] getAvailableURL() {
		return availableURL;
	}

	public void setAvailableURL(String[] availableURL) {
		this.availableURL = availableURL;
	}

	public boolean isFolderlamp() {
		return folderlamp;
	}

	public void setFolderlamp(boolean folderlamp) {
		this.folderlamp = folderlamp;
	}

	public Resolution getResolution() {
		return resolution;
	}

	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int width) {
		this.windowWidth = width;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int height) {
		this.windowHeight = height;
	}

	public int getFrameskip() {
		return frameskip;
	}

	public void setFrameskip(int frameskip) {
		this.frameskip = frameskip;
	}

	public String getBgmpath() {
		return bgmpath;
	}

	public void setBgmpath(String bgmpath) {
		this.bgmpath = bgmpath;
	}

	public String getSoundpath() {
		return soundpath;
	}

	public void setSoundpath(String soundpath) {
		this.soundpath = soundpath;
	}

	public int getMaxSearchBarCount() {
	    return maxSearchBarCount;
    }

    public void setMaxSearchBarCount(int maxSearchBarCount) {
	    this.maxSearchBarCount = maxSearchBarCount;
    }

	public void setSkipDecideScreen(boolean skipDecideScreen) {
		this.skipDecideScreen = skipDecideScreen;
	}

	public boolean isSkipDecideScreen() {
		return skipDecideScreen;
	}

	public boolean isShowNoSongExistingBar() {
		return showNoSongExistingBar || isEnableHttp();
	}

	public void setShowNoSongExistingBar(boolean showNoExistingSongBar) {
		this.showNoSongExistingBar = showNoExistingSongBar;
	}

	public int getScrollDurationLow(){
		return scrolldurationlow;
	}
	public void setScrollDutationLow(int scrolldurationlow){
		this.scrolldurationlow = scrolldurationlow;
	}
	public int getScrollDurationHigh(){
		return scrolldurationhigh;
	}
	public void setScrollDutationHigh(int scrolldurationhigh){
		this.scrolldurationhigh = scrolldurationhigh;
	}

    public boolean isAnalogScroll() {
        return analogScroll;
    }
    public void setAnalogScroll(boolean analogScroll) {
        this.analogScroll = analogScroll;
    }

    public int getAnalogTicksPerScroll() {
        return analogTicksPerScroll;
    }
    public void setAnalogTicksPerScroll(int analogTicksPerScroll) {
        this.analogTicksPerScroll = Math.max(analogTicksPerScroll, 1);
    }

	public SongPreview getSongPreview() {
		return songPreview;
	}

	public void setSongPreview(SongPreview songPreview) {
		this.songPreview = songPreview;
	}

	public boolean isUseSongInfo() {
		return useSongInfo;
	}

	public void setUseSongInfo(boolean useSongInfo) {
		this.useSongInfo = useSongInfo;
	}

	public int getBgaExpand() {
		return bgaExpand;
	}

	public void setBgaExpand(int bgaExpand) {
		this.bgaExpand = bgaExpand;
	}

	public boolean isCacheSkinImage() {
		return cacheSkinImage;
	}

	public void setCacheSkinImage(boolean cacheSkinImage) {
		this.cacheSkinImage = cacheSkinImage;
	}

	public boolean isUseDiscordRPC() {
		return useDiscordRPC;
	}

	public void setUseDiscordRPC(boolean useDiscordRPC) {
		this.useDiscordRPC = useDiscordRPC;
	}

	public boolean isUseObsWs() {
		return useObsWs;
	}

	public void setUseObsWs(boolean useObsWs) {
		this.useObsWs = useObsWs;
	}

	public HashMap<String, String> getObsScenes() {
		return obsScenes;
	}

	public void setObsScenes(HashMap<String, String> obsScenes) {
		this.obsScenes = obsScenes;
	}

	public HashMap<String, String> getObsActions() {
		return obsActions;
	}

	public void setObsActions(HashMap<String, String> obsActions) {
		this.obsActions = obsActions;
	}

	public boolean isSetClipboardWhenScreenshot() {
		return setClipboardScreenshot;
	}

	public void setClipboardWhenScreenshot(boolean setClipboardScreenshot) {
		this.setClipboardScreenshot = setClipboardScreenshot;
	}

	public boolean isUpdatesong() {
		return updatesong;
	}

	public void setUpdatesong(boolean updatesong) {
		this.updatesong = updatesong;
	}

	public DisplayMode getDisplaymode() {
		return displaymode;
	}

	public void setDisplaymode(DisplayMode displaymode) {
		this.displaymode = displaymode;
	}

	public int getSkinPixmapGen() {
		return skinPixmapGen;
	}

	public void setSkinPixmapGen(int skinPixmapGen) {
		this.skinPixmapGen = skinPixmapGen;
	}

	public int getStagefilePixmapGen() {
		return stagefilePixmapGen;
	}

	public void setStagefilePixmapGen(int stagefilePixmapGen) {
		this.stagefilePixmapGen = stagefilePixmapGen;
	}

	public int getBannerPixmapGen() {
		return bannerPixmapGen;
	}

	public void setBannerPixmapGen(int bannerPixmapGen) {
		this.bannerPixmapGen = bannerPixmapGen;
	}

	public int getSongResourceGen() {
		return songResourceGen;
	}

	public void setSongResourceGen(int songResourceGen) {
		this.songResourceGen = songResourceGen;
	}

	public boolean isEnableIpfs() {
		return enableIpfs;
	}

	public void setEnableIpfs(boolean enableIpfs) {
		this.enableIpfs = enableIpfs;
	}

	public String getDownloadDirectory() {
		return downloadDirectory;
	}

	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}

	public String getIpfsUrl() {
		return ipfsurl;
	}

	public void setIpfsUrl(String ipfsUrl) {
		this.ipfsurl = ipfsUrl;
	}

	public String getSongpath() {
		return songpath;
	}

	public void setSongpath(String songpath) {
		this.songpath = songpath;
	}

	public String getSonginfopath() {
		return songinfopath;
	}

	public void setSonginfopath(String songinfopath) {
		this.songinfopath = songinfopath;
	}

	public String getTablepath() {
		return tablepath;
	}

	public void setTablepath(String tablepath) {
		this.tablepath = tablepath;
	}

	public String getPlayerpath() {
		return playerpath;
	}

	public void setPlayerpath(String playerpath) {
		this.playerpath = playerpath;
	}

	public String getSkinpath() {
		return skinpath;
	}

	public void setSkinpath(String skinpath) {
		this.skinpath = skinpath;
	}

	public String getSystemfontpath() {
		return systemfontpath;
	}

	public void setSystemfontpath(String systemfontpath) {
		this.systemfontpath = systemfontpath;
	}

	public String getMessagefontpath() {
		return messagefontpath;
	}

	public void setMessagefontpath(String messagefontpath) {
		this.messagefontpath = messagefontpath;
	}

	public boolean isEnableHttp() {
		return enableHttp;
	}

	public void setEnableHttp(boolean enableHttp) {
		this.enableHttp = enableHttp;
	}

	public String getDownloadSource() {
		return downloadSource;
	}

	public void setDownloadSource(String downloadSource) {
		this.downloadSource = downloadSource;
	}

	public String getDefaultDownloadURL() {
		return defaultDownloadURL;
	}

	public void setDefaultDownloadURL(String defaultDownloadURL) {
		this.defaultDownloadURL = defaultDownloadURL;
	}

	public String getOverrideDownloadURL() {
		return overrideDownloadURL;
	}

	public void setOverrideDownloadURL(String overrideDownloadURL) {
		this.overrideDownloadURL = overrideDownloadURL;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

    public String getWebhookName() { return webhookName; }

    public void setWebhookName(String webhookName) { this.webhookName = webhookName; }

    public String getWebhookAvatar() { return webhookAvatar; };

    public void setWebhookAvatar(String webhookAvatar) { this.webhookAvatar = webhookAvatar; }

	public String[] getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(String[] webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

    // 0 - Off, 1 - Image, 2 - Rich
    public int getWebhookOption() { return webhookOption; }

    public void setWebhookOption(int webhookOption) { this.webhookOption = webhookOption; }

	public String getObsWsHost() { return obsWsHost; }

	public void setObsWsHost(String obsWsHost) { this.obsWsHost = obsWsHost; }

	public int getObsWsPort() { return obsWsPort; }

	public void setObsWsPort(int obsWsPort) {
		this.obsWsPort = MathUtils.clamp(obsWsPort, 0, 65535);
	}

	public String getObsWsPass() {
        if (obsWsPass == null) {
            return null;
        }
		if (obsWsPass.isBlank()) {
			return null;
		}
		return obsWsPass;
	}

	public void setObsWsPass(String obsWsPass) { this.obsWsPass = obsWsPass; }

	public int getObsWsRecStopWait() { return obsWsRecStopWait; }

	public void setObsWsRecStopWait(int obsWsRecStopWait) {
		this.obsWsRecStopWait = MathUtils.clamp(obsWsRecStopWait, 0, 10000);
	}

	public int getObsWsRecMode() { return obsWsRecMode; }

	public void setObsWsRecMode(int obsWsRecMode) {
		this.obsWsRecMode = MathUtils.clamp(obsWsRecMode, 0, ObsRecordingMode.values().length - 1);
	}

	public String getObsScene(String stateName) {
		return obsScenes.get(stateName);
	}

	public void setObsScene(String stateName, String sceneName) {
		if (sceneName == null || sceneName.isEmpty()) {
			obsScenes.remove(stateName);
		} else {
			obsScenes.put(stateName, sceneName);
		}
	}

	public String getObsAction(String stateName) {
		return obsActions.get(stateName);
	}

	public void setObsAction(String stateName, String actionName) {
		if (actionName == null || actionName.isEmpty()) {
			obsActions.remove(stateName);
		} else {
			obsActions.put(stateName, actionName);
		}
	}

    public boolean validate() {
		displaymode = (displaymode != null) ? displaymode : DisplayMode.WINDOW;
		resolution = (resolution != null) ? resolution : Resolution.HD;

		windowWidth = MathUtils.clamp(windowWidth, Resolution.SD.width, Resolution.ULTRAHD.width);
		windowHeight = MathUtils.clamp(windowHeight, Resolution.SD.height, Resolution.ULTRAHD.height);

		if(audio == null) {
			audio = new AudioConfig();
		}
		audio.validate();
		maxFramePerSecond = MathUtils.clamp(maxFramePerSecond, 0, 50000);
		prepareFramePerSecond = MathUtils.clamp(prepareFramePerSecond, 0, 100000);
        maxSearchBarCount = MathUtils.clamp(maxSearchBarCount, 1, 100);
        songPreview = (songPreview != null) ? songPreview : SongPreview.LOOP;

		scrolldurationlow = MathUtils.clamp(scrolldurationlow, 2, 1000);
		scrolldurationhigh = MathUtils.clamp(scrolldurationhigh, 1, 1000);
		irSendCount = MathUtils.clamp(irSendCount, 1, 100);

		skinPixmapGen = MathUtils.clamp(skinPixmapGen, 0, 100);
		stagefilePixmapGen = MathUtils.clamp(stagefilePixmapGen, 0, 100);
		bannerPixmapGen = MathUtils.clamp(bannerPixmapGen, 0, 100);
		songResourceGen = MathUtils.clamp(songResourceGen, 0, 100);

		bmsroot = Validatable.removeInvalidElements(bmsroot);

		if(tableURL == null) {
			tableURL = DEFAULT_TABLEURL;
		}
		tableURL = Validatable.removeInvalidElements(tableURL);

		bga = MathUtils.clamp(bga, 0, 2);
		bgaExpand = MathUtils.clamp(bgaExpand, 0, 2);
		if (ipfsurl == null) {
			ipfsurl = "https://gateway.ipfs.io/";
		}

		songpath = songpath != null ? songpath : SONGPATH_DEFAULT;
		songinfopath = songinfopath != null ? songinfopath : SONGINFOPATH_DEFAULT;
		tablepath = tablepath != null ? tablepath : TABLEPATH_DEFAULT;
		playerpath = playerpath != null ? playerpath : PLAYERPATH_DEFAULT;
		skinpath = skinpath != null ? skinpath : SKINPATH_DEFAULT;
		downloadDirectory = validatePath(downloadDirectory) ? downloadDirectory : DEFAULT_DOWNLOAD_DIRECTORY;
		return true;
	}

	public static Config read() throws PlayerConfigException {
        RobustFile.Parser<Config> parser = (byte[] data) -> {
            Json json = new Json();
            json.setIgnoreUnknownFields(true);
            try {
                String ss = new String(data, StandardCharsets.UTF_8);
                Config config = json.fromJson(Config.class, ss);
                if (config == null) { throw new SerializationException("(unknown error)"); }
                return config;
            }
            catch (SerializationException e) {
                throw new ParseException("Configの読み込み失敗 - Path : " + configpath +
                                             " , Log : " + e.getLocalizedMessage(),
                                         0);
            }
        };

        Config config = null;
		if (Files.exists(configpath)) {
            try {
                config = RobustFile.load(configpath, parser);
            }
            catch (IOException e) {
                writeBackupConfigFile();
                e.printStackTrace();
            }
        } else if(Files.exists(configpath_old)) {
			// 旧コンフィグ読み込み。そのうち削除
			Json json = new Json();
			json.setIgnoreUnknownFields(true);
			try (FileReader reader = new FileReader(configpath_old.toFile())) {
				config = json.fromJson(Config.class, reader);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		if(config == null) {
			config = new Config();
		}
		return validateConfig(config);
	}

	private static void writeBackupConfigFile() {
		try {
            Path configBackupPath = configpath.resolveSibling("config_sys_backup.json");
			Files.copy(configpath, configBackupPath, StandardCopyOption.REPLACE_EXISTING);
			logger.info("Backup config written to {}", configBackupPath);
		} catch (IOException e) {
			logger.error("Failed to write backup config file: {}", e.getLocalizedMessage());
		}
	}

	public static Config validateConfig(Config config) throws PlayerConfigException {
		config.validate();
		PlayerConfig.init(config);
		return config;
	}

    public static String getConfigJson(Config config) {
        Json json = new Json();
        json.setUsePrototypes(false);
        json.setOutputType(OutputType.json);
        return json.prettyPrint(config);
    }

    public static void write(Config config) {
        write(config, getConfigJson(config));
    }

    public static void write(Config config, String configJson) {
        try {
            RobustFile.write(configpath, configJson.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

	public int getIrSendCount() {
		return irSendCount;
	}

	public void setIrSendCount(int irSendCount) {
		this.irSendCount = irSendCount;
	}

	public boolean isUseResolution() {
		return useResolution;
	}

	public void setUseResolution(boolean useResolution) {
		this.useResolution = useResolution;
	}

    public enum DisplayMode {
		FULLSCREEN,BORDERLESS,WINDOW;
	}

	public enum SongPreview {
		NONE,ONCE,LOOP;
	}

	private boolean validatePath(String path) {
		try {
			Paths.get(path);
		} catch (InvalidPathException | NullPointerException e) {
			return false;
		}
		return true;
	}
}
