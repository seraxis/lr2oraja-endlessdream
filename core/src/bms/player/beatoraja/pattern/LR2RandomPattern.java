package bms.player.beatoraja.pattern;

import bms.player.beatoraja.modmenu.RandomTrainer;
import com.badlogic.gdx.utils.IntArray;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for converting LR2/Raja random seed and permutations
 *
 * @apiNote Currently, this class only considers the basic random mod(i.e. no R-Random, Random-EX etc) and 7-Keys, this is based on the actual usage
 */
public class LR2RandomPattern {
	/**
	 * Convert LR2 seed to Raja seed
	 */
	public static long fromLR2SeedToRaja(int randomSeed) {
		return RandomTrainer.getRandomSeedMap().get(Integer.parseInt(getLR2LaneOrder(randomSeed, false)));
	}

	/**
	 * Convert Raja seed to LR2 seed
	 */
	public static int fromRajaToLR2Seed(long randomSeed) {
		return LR2RandomTable.GetSeed7K(getRajaLaneOrder(randomSeed, false));
	}

	/**
	 * Calculate the lane order based on LR2 random seed
	 *
	 * @param withScratch with scratch line or not
	 * @return a string like "1234567", represents the final lane order. If withScratch is flagged, it would be something like "01234567"
	 * @apiNote Scratch would always be left most one, i.e. this random system doesn't consider scratch line into random
	 * @see bms.player.beatoraja.modmenu.RandomTrainer
	 */
	public static String getLR2LaneOrder(int randomSeed, boolean withScratch) {
		LR2Random rng = new LR2Random(randomSeed);
		int[] targets = {0, 1, 2, 3, 4, 5, 6, 7};
		for (int a = 1; a < 7; ++a) {
			int b = a + rng.nextInt(7 - a + 1);
			int tmp = targets[a];
			targets[a] = targets[b];
			targets[b] = tmp;
		}
		int[] lanes = {0, 1, 2, 3, 4, 5, 6, 7};
		for (int i = 1; i < 8;++i) {
			lanes[targets[i]] = i;
		}
		return Arrays.stream(lanes)
				.filter(lane -> withScratch || lane != 0)
				.mapToObj(Integer::toString)
				.collect(Collectors.joining());
	}

	/**
	 * Calculate the lane order based on Raja random seed
	 *
	 * @param withScratch with scratch line or not
	 * @return a string like "1234567", represents the final lane order. If withScratch is flagged, it would be something like "01234567"
	 * @apiNote Scratch would always be left most one, i.e. this random system doesn't consider scratch line into random
	 * @see LaneShuffleModifier
	 */
	public static String getRajaLaneOrder(long randomSeed, boolean withScratch) {
		Random rng = new Random(randomSeed);
		int[] keys = {0, 1, 2, 3, 4, 5, 6};
		IntArray l = new IntArray(keys);
		int[] result = IntStream.range(0, 8).toArray();
		for (int lane = 0; lane < 7; lane++) {
			int r = rng.nextInt(l.size);
			result[keys[lane]] = l.get(r);
			l.removeIndex(r);
		}
		// Now, result is a 8-length permutation from 0~7, the 7 will always be placed at right most and it represents the scratch
		// Therefore, we need to do some extra hack to keep the result form as the same as getLR2LaneOrder
		int[] shiftedResult = new int[8];
		shiftedResult[0] = 0;
		System.arraycopy(result, 0, shiftedResult, 1, 7);
		for (int i = 1; i < shiftedResult.length; ++i) {
			++shiftedResult[i];
		}
		return Arrays.stream(shiftedResult)
				.filter(lane -> withScratch || lane != 0)
				.mapToObj(Integer::toString)
				.collect(Collectors.joining());
	}
}
