package bms.player.beatoraja.ir;

/**
 * IR接続用インターフェイス
 * 
 * @author exch
 */
public interface IRConnection {

	/**
	 * IRに新規ユーザー登録する。
	 * 
	 * @param id
	 *            ユーザーID
	 * @param pass
	 *            パスワード
	 * @param name
	 *            ユーザー名
	 * @return
	 */
	public IRResponse<IRPlayerData> register(String id, String pass, String name);

	/**
	 * IRにログインする。起動時に呼び出される
	 * 
	 * @param id
	 *            ユーザーID
	 * @param pass
	 *            パスワード
	 */
	public IRResponse<IRPlayerData> login(String id, String pass);

	/**
	 * ライバルデータを収録する
	 * 
	 * @return ライバルデータ
	 */
	public IRResponse<IRPlayerData[]> getRivals();

	/**
	 * IRに設定されている表データを収録する
	 * 
	 * @return IRで取得可能な表データ
	 */
	public IRResponse<IRTableData[]> getTableDatas();

	/**
	 * スコアデータを取得する
	 * 
	 * @return
	 */
	public IRResponse<IRScoreData[]> getPlayData(IRPlayerData player, IRChartData chart);

	public IRResponse<IRScoreData[]> getCoursePlayData(IRPlayerData player, IRCourseData course);

	/**
	 * スコアデータを送信する
	 * 
	 * @param model
	 *            楽曲データ
	 * @param score
	 *            スコア
	 * @return 送信結果
	 */
	public IRResponse<Object> sendPlayData(IRChartData model, IRScoreData score);

	/**
	 * コーススコアデータを送信する
	 * 
	 * @param course
	 *            コースデータ
	 * @param score
	 *            スコア
	 * @return 送信結果
	 */
	public IRResponse<Object> sendCoursePlayData(IRCourseData course, IRScoreData score);

	/**
	 * 楽曲のURLを取得する
	 * 
	 * @return 楽曲URL。存在しない場合はnull
	 */
	public String getSongURL(IRChartData chart);

	/**
	 * コースのURLを取得する
	 * 
	 * @param course
	 *            コースデータ
	 * @return コースURL。存在しない場合はnull
	 */
	public String getCourseURL(IRCourseData course);

	/**
	 * プレイヤーURLを取得する
	 * 
	 * @return
	 */
	public String getPlayerURL(IRPlayerData player);

}
