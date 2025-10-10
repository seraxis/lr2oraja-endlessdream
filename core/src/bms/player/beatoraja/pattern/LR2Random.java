package bms.player.beatoraja.pattern;

public class LR2Random {
    public LR2Random() { setSeed(4357); }
    public LR2Random(int seed) { setSeed(seed); }

    // Matsumoto and Nishimura, 1998

    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df;   /* constant vector a */
    private static final int UPPER_MASK = 0x80000000; /* most significant w-r bits */
    private static final int LOWER_MASK = 0x7fffffff; /* least significant r bits */

    private int mti;
    private int[] mt = new int[N + 1];
    private int[] mtr = new int[N];

    public void setSeed(int seed) {
        for (int i = 0; i < N; ++i) {
            mt[i] = seed & 0xffff0000;
            seed = 69069 * seed + 1;
            mt[i] |= (seed & 0xffff0000) >>> 16;
            seed = 69069 * seed + 1;
        }
        generateMT();
    }

    public int nextInt(int max) {
        long randMax = max;
        var r = Integer.toUnsignedLong(randMT());
        return (int)((r * randMax) >>> 32);
    }

    /* Tempering parameters */
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;

    private void generateMT() {
        final int[] mag01 = {0, MATRIX_A};
        int kk;
        int y;
        for (kk = 0; kk < N - M; kk++) {
            int b = mt[kk + 1] & LOWER_MASK;
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
        }

        mt[N] = mt[0];
        for (; kk < N; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
        }

        // #define TEMPERING_SHIFT_U(y) (y >> 11)
        // #define TEMPERING_SHIFT_S(y) (y << 7)
        // #define TEMPERING_SHIFT_T(y) (y << 15)
        // #define TEMPERING_SHIFT_L(y) (y >> 18)
        for (kk = 0; kk < N; kk++) {
            y = mt[kk];
            // y ^= TEMPERING_SHIFT_U(y);
            // y ^= TEMPERING_SHIFT_S(y) & TEMPERING_MASK_B;
            // y ^= TEMPERING_SHIFT_T(y) & TEMPERING_MASK_C;
            // y ^= TEMPERING_SHIFT_L(y);
            y ^= (y >>> 11);
            y ^= (y << 7) & TEMPERING_MASK_B;
            y ^= (y << 15) & TEMPERING_MASK_C;
            y ^= (y >>> 18);
            mtr[kk] = y;
        }
        mti = 0;
    }

    public int randMT() {
        if (mti >= N) {
            generateMT();
        }
        return mtr[mti++];
    }
}
