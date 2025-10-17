package bms.player.beatoraja.pattern;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * source: https://github.com/MatVeiQaaa/LR2HackBox/blob/8b03b3fa363bfca79bd316cf59eb367e6f0df9e6/src/LR2HackBox/Features/Unrandomizer_SeedMap7K.hpp<br>
 * TODO: Rewrite this as a binary file on disk instead
 * @see bms.player.beatoraja.modmenu.RandomTrainer
 */
public class LR2RandomTable {
	private static Properties prop = new Properties();

	public static int GetSeed7K(String in) {
		if (prop.isEmpty()) {
			try {
				prop.load(LR2RandomTable.class.getClassLoader().getResourceAsStream("resources/lr2randomtable.properties"));
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getGlobal().severe("Failed to load lr2 random table: " + e.getMessage());
			}
		}
		String r = (String) prop.get(in);
		return r == null ? 0xFFFF : Integer.parseInt(r);
	}
}
