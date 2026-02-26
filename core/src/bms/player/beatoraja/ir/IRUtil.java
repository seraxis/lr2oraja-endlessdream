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
        boolean rianIR = isRianIR(irName);
        // rianIR かつ DX MODEオフ、または、rianIR以外 かつ DX MODEオン の場合にスキップ
        return rianIR != isDxMode;
    }
}
