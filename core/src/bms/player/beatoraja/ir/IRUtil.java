package bms.player.beatoraja.ir;

/**
 * IRに関するユーティリティクラス
 */
public class IRUtil {

    /**
     * IR名がrianIRであるかどうかを判定する
     * @param irName IR名
     * @return rianIRであればtrue
     */
    public static boolean isRianIR(String irName) {
        return irName != null && irName.startsWith("rianIR");
    }

    /**
     * IR接続をスキップすべきかどうかを判定する
     * @param irName IR名
     * @param isDxMode DX MODEがオンかどうか
     * @return スキップすべきであればtrue
     */
    public static boolean shouldSkipIR(String irName, boolean isDxMode) {
        // rianIRの場合は常にスキップしない
        if (isRianIR(irName)) {
            return false;
        }
        // rianIR以外かつDX MODEオンの場合はスキップ
        return isDxMode;
    }
}
