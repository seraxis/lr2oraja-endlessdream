package bms.player.beatoraja.ir;

import bms.player.beatoraja.PlayerInformation;

/**
 * IR用プレイヤーデータ
 *
 * @author exch
 */
public class IRPlayerData {

    /**
     * プレイヤーID
     */
    public final String id;
    /**
     * プレイヤー名
     */
    public final String name;
    /**
     * 段位
     */
    public final String rank;

    public IRPlayerData(String id, String name, String rank) {
        this.id = id;
        this.name = name;
        this.rank = rank;
    }

    public PlayerInformation toPlayerInformation() {
        var playerInformation = new PlayerInformation();
        playerInformation.setId(this.id);
        playerInformation.setName(this.name);
        playerInformation.setRank(this.rank);
        return playerInformation;
    }
}
