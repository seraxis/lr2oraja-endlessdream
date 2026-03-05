package bms.player.beatoraja;

import java.util.Objects;

/**
 * プレイヤーの情報
 *
 * @author exch
 */
public class PlayerInformation {

	/**
	 * プレイヤーID
	 */
	private String id;
	/**
	 * プレイヤー名
	 */
	private String name;
	/**
	 * 段位
	 */
	private String rank;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerInformation that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, rank);
    }
}
