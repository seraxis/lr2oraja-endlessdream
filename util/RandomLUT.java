import java.io.*;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.*;


public class RandomLUT {

    public static void main(String args[]) {
        long startTime = System.nanoTime();

        HashMap<Integer, Long> random_LUT = new HashMap<Integer, Long>(5040, (float) 1.0);

        List<Integer> lanes = Stream.of(1, 2, 3, 4, 5, 6, 7)
            .collect(Collectors.toList());
        int size = 7;

        HashSet<Integer> perms = Permutations.of(lanes)
            .map(p -> Integer.parseInt(p.
                 map(n -> n.toString())
                 .collect(Collectors.joining()))
                 ).collect(Collectors.toCollection(HashSet::new));

        
        int[] shuffle_lanes = lanes.stream().map(n -> n-1).mapToInt(n -> n).toArray();

        while (!perms.isEmpty()) {
            long seed = (long) (Math.random() * 65536 * 256);

            Integer random = Integer.parseInt(Arrays.stream(shuffle(shuffle_lanes, seed)).mapToObj(l -> Integer.toString(l + 1)).collect(Collectors.joining()));

            if (perms.contains(random)) {
                perms.remove(random);
                random_LUT.put(random, seed);
            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        random_LUT.entrySet().forEach(System.out::println);
        System.out.println("map size: " + random_LUT.size());
        System.out.println("Crude execution time: " + (duration / 1000000) + "ms");

        try {
            FileOutputStream fos = new FileOutputStream("randomtrainer.dat");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);

            objectOutputStream.writeObject(random_LUT);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ex) {
            System.out.println("oops");
        }
    }

	protected static int[] shuffle(int[] keys, long seed) {
		java.util.Random rand = new java.util.Random(seed);
		List<Integer> l = new ArrayList<Integer>(keys.length);
		for (int key : keys) {
			l.add(key);
		}
		int max = 0;
		for (int key : keys) {
			max = Math.max(max, key);
		}
		int[] result = new int[max + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		for (int lane = 0; lane < keys.length; lane++) {
			int r = rand.nextInt(l.size());
			result[keys[lane]] = l.get(r);
			l.remove(r);
		}

		return result;
	}

}


class Permutations {

    public static <T> Stream<Stream<T>> of(final List<T> items) {
        return IntStream.range(0, factorial(items.size())).mapToObj(i -> permutation(i, items).stream());
    }

    private static int factorial(final int num) {
        return IntStream.rangeClosed(2, num).reduce(1, (x, y) -> x * y);
    }

    private static <T> List<T> permutation(final int count, final LinkedList<T> input, final List<T> output) {
        if (input.isEmpty()) { return output; }

        final int factorial = factorial(input.size() - 1);
        output.add(input.remove(count / factorial));
        return permutation(count % factorial, input, output);
    }

    private static <T> List<T> permutation(final int count, final List<T> items) {
        return permutation(count, new LinkedList<>(items), new ArrayList<>());
    }

}