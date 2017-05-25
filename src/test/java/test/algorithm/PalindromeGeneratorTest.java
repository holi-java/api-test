package test.algorithm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 5/25/17.
 */
public class PalindromeGeneratorTest {

    @Test
    void digits1() throws Throwable {
        assertThat(palindromes(1), equalTo(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    void digits2() throws Throwable {
        assertThat(palindromes(2), contains(11, 22, 33, 44, 55, 66, 77, 88, 99));
    }

    @Test
    void digits3() throws Throwable {
        List<Integer> result = palindromes(3);

        assertThat("head", result.subList(0, 10), equalTo(asList(101, 111, 121, 131, 141, 151, 161, 171, 181, 191)));
        assertThat("tail", result.subList(80, 90), equalTo(asList(909, 919, 929, 939, 949, 959, 969, 979, 989, 999)));
        assertThat(result, hasSize(90));
    }

    @Test
    void digits4() throws Throwable {
        List<Integer> result = palindromes(4);

        assertThat("head", result.subList(0, 10), equalTo(asList(1001, 1111, 1221, 1331, 1441, 1551, 1661, 1771, 1881, 1991)));
        assertThat("tail", result.subList(80, 90), equalTo(asList(9009, 9119, 9229, 9339, 9449, 9559, 9669, 9779, 9889, 9999)));
        assertThat(result, hasSize(90));
    }

    @Test
    void digits5() throws Throwable {
        List<Integer> result = palindromes(5);

        assertThat("head", result.subList(0, 10), equalTo(asList(10001, 10101, 10201, 10301, 10401, 10501, 10601, 10701, 10801, 10901)));
        assertThat("tail", result.subList(890, 900), equalTo(asList(99099, 99199, 99299, 99399, 99499, 99599, 99699, 99799, 99899, 99999)));
        assertThat(result, hasSize(900));
    }

    @Test
    void digits9() throws Throwable {
        List<Integer> result = palindromes(9);

        assertThat(result, hasSize((int) (9 * Math.pow(10, 4))));

    }

    @Test
    void max() throws Throwable {
        List<Integer> result = palindromes(10);

        assertThat(result.get(result.size() - 1), equalTo(2147447412));
    }


    private static List<Integer> palindromes(int digits) {
        return palindromes(digits, 0);
    }

    private static List<Integer> palindromes(int digits, int shifts) {
        List<Integer> result = new ArrayList<>();
        int radix = 10;
        int high = (int) Math.pow(radix, digits - 1);
        int renaming = digits - 2;
        if (renaming > 0) {
            for (int i = start(digits, shifts); i <= 9; i++) {
                for (Integer m : palindromes(renaming, shifts + 1)) {
                    int ret = i * high + m * radix + low(digits, i);
                    if (ret < 0) {//overflow
                        return result;
                    }
                    result.add(ret);
                }
            }
        } else {
            for (int i = start(digits, shifts); i <= 9; i++) {
                result.add(i * high + low(digits, i));
            }
        }
        return result;
    }

    private static int low(int digits, int high) {
        return digits > 1 ? high : 0;
    }

    private static int start(int digits, int shifts) {
        return digits > 1 && shifts == 0 ? 1 : 0;
    }
}
