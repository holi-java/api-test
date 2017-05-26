package test.algorithm;

import java.time.Duration;
import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Created by holi on 5/26/17.
 */
public class Palindromes {
    private static final int RADIX = 10;
    private static final int[] startingNonZeros = {
            0,// 0
            0, 1,// 1 2
            10, 10,//3 4
            100, 100, //5 6
            1000, 1000,//7 8
            10000, 10000,// 9 10
            100000, 100000,//11 12
            1000000, 1000000,//13 14
            10000000, 10000000,//15 16
            100000000, 100000000,//17 18
            1000000000, 1000000000//19 20
    };
    private static final long[][] cache = new long[20][];

    static {
        cache[0] = new long[0];
        cache[1] = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        cache[2] = new long[]{0L, 11L, 22L, 33L, 44L, 55L, 66L, 77L, 88L, 99L};
    }

    public static LongStream since1(int end) {
        return between(1, end);
    }

    public static LongStream between(int start, int end) {
        return IntStream.rangeClosed(start, end).mapToObj(Palindromes::of).flatMapToLong(Function.identity());
    }

    public static LongStream of(int digits) {
        return Arrays.stream(zerosHeadingPalindromes(digits)).skip(startingNonZeros[digits]);
    }

    private final static long[] zerosHeadingPalindromes(int digits) {
        if (cache[digits] != null) {
            return cache[digits];
        }

        long[] mid = zerosHeadingPalindromes(digits - 2);
        long[] result = new long[sizeOf(digits)];
        int size = 0;
        long radix = (long) Math.pow(RADIX, digits - 1);

        for (int high = 0; high <= 9; high++) {
            for (long m : mid) {
                long value = high * radix + m * RADIX + high;
                if (value < 0) {//overflow
                    return cache[digits] = Arrays.copyOf(result, size);
                }
                result[size++] = value;
            }
        }
        return cache[digits] = result;
    }

    private static int sizeOf(int digits) {
        return 9 * (int) Math.pow(10, (digits - 1) >>> 1) + startingNonZeros[digits];
    }

    //                  v--- java -Xms1024m -Xmx2048m test.algorithm.Palindromes
    public static void main(String[] args) {
        Duration duration = timing(() -> {
            LongSummaryStatistics result = Palindromes.since1(15).summaryStatistics();

            System.out.printf("Max: %d, Count: %d%n", result.getMax(), result.getCount());
        });

        System.out.printf("Time Elapsed:%s%n", duration);
    }

    private static Duration timing(Runnable task) {
        long starts = System.currentTimeMillis();
        task.run();
        return Duration.ofMillis(System.currentTimeMillis() - starts);
    }
}
