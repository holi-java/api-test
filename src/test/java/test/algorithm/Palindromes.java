package test.algorithm;

import java.time.Duration;
import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.function.Function.identity;

/**
 * Created by holi on 5/26/17.
 */
public class Palindromes {
    private static final int[] startingNonZerosTable = {
            0,// 0 includes palindromes(0)  ---^
            0, 1,// 1 2
            10, 10,//3 4
            100, 100, //5 6
            1000, 1000,//7 8
            10000, 10000,// 9 10
            100000, 100000,//11 12
            1000000, 1000000,//13 14
            10000000, 10000000,//15 16
            100000000, 100000000,//17 18
            1000000000//19
    };

    private static final int MIN_DIGIT = 0;
    private static final int MAX_DIGIT = 9;
    private static final int RADIX = MAX_DIGIT - MIN_DIGIT + 1;
    private static volatile long[][] cache = new long[startingNonZerosTable.length][];
    //

    static {
        cache[0] = new long[0];
        cache[1] = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        cache[2] = new long[]{0L, 11L, 22L, 33L, 44L, 55L, 66L, 77L, 88L, 99L};
    }

    public static LongStream since1(int end) {
        return between(1, end);
    }

    public static LongStream between(int start, int end) {
        return IntStream.rangeClosed(start, end)
                .mapToObj(Palindromes::of)
                .flatMapToLong(identity());
    }

    public static LongStream of(int digits) {
        return Arrays.stream(palindromes0(digits))
                .skip(startingNonZerosTable[digits]);
    }

    private final static long[] palindromes0(int digits) {
        if (cache[digits] != null) {
            return cache[digits];
        }

        long[] result = new long[sizeOf(digits)];
        int size = 0;
        long high = (long) Math.pow(RADIX, digits - 1);

        for (int i = MIN_DIGIT; i <= MAX_DIGIT; i++) {
            for (long mid : palindromes0(digits - 2)) {
                long value = i * high + mid * RADIX + i;
                if (value < 0) {//overflow
                    return cache[digits] = Arrays.copyOf(result, size);
                }
                result[size++] = value;
            }
        }
        return cache[digits] = result;
    }

    private static int sizeOf(int digits) {
        return MAX_DIGIT * (int) Math.pow(RADIX, (digits - 1) >>> 1)
                + startingNonZerosTable[digits];
    }

    //                  v--- java -Xms1024m -Xmx2048m test.algorithm.Palindromes
    public static void main(String[] args) {
        Duration duration = timing(() -> {
            LongSummaryStatistics result = since1(15).summaryStatistics();
            long max = result.getMax();
            long count = result.getCount();

            System.out.printf("Max: %d, Count: %d%n", max, count);
        });

        System.out.printf("Time Elapsed:%s%n", duration);
    }

    private static Duration timing(Runnable task) {
        long starts = System.currentTimeMillis();
        task.run();
        return Duration.ofMillis(System.currentTimeMillis() - starts);
    }
}
