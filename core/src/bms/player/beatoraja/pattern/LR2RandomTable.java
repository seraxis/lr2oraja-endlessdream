package bms.player.beatoraja.pattern;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * source: https://github.com/MatVeiQaaa/LR2HackBox/blob/8b03b3fa363bfca79bd316cf59eb367e6f0df9e6/src/LR2HackBox/Features/Unrandomizer_SeedMap7K.hpp<br>
 * TODO: Rewrite this as a binary file on disk instead
 * @see bms.player.beatoraja.modmenu.RandomTrainer
 */
public class LR2RandomTable {
	private static HashMap<Integer, Integer> randomSeedMap;

	public static int getSeed7K(int in) {
		if (randomSeedMap == null) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			try {
				ObjectInputStream ois = new ObjectInputStream(cl.getResourceAsStream("resources/lr2randomtable.dat"));
				randomSeedMap = (HashMap<Integer, Integer>) ois.readObject();
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				Logger.getGlobal().severe("Failed to load lr2 random table data: " + e.getMessage());
			}
		}
		return randomSeedMap.getOrDefault(in, 0xFFFF);
	}
}
