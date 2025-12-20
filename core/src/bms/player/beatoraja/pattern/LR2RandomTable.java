package bms.player.beatoraja.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * source: https://github.com/MatVeiQaaa/LR2HackBox/blob/8b03b3fa363bfca79bd316cf59eb367e6f0df9e6/src/LR2HackBox/Features/Unrandomizer_SeedMap7K.hpp<br>
 * @see bms.player.beatoraja.modmenu.RandomTrainer
 */
public class LR2RandomTable {
	private static final Logger logger = LoggerFactory.getLogger(LR2RandomTable.class);

	private static HashMap<Integer, Integer> randomSeedMap;

	public static int getSeed7K(int in) {
		if (randomSeedMap == null) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			try {
				ObjectInputStream ois = new ObjectInputStream(cl.getResourceAsStream("resources/lr2randomtable.dat"));
				randomSeedMap = (HashMap<Integer, Integer>) ois.readObject();
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				logger.error("Failed to load lr2 random table data: ", e);
			}
		}
		return randomSeedMap.getOrDefault(in, 0xFFFF);
	}
}
