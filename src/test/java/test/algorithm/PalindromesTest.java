package test.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Created by holi on 5/25/17.
 */
public class PalindromesTest {


    @Test
    void digits1() throws Throwable {
        assertThat(palindromes(1), equalTo(asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)));
    }

    @Test
    void digits2() throws Throwable {
        assertThat(palindromes(2), equalTo(asList(11L, 22L, 33L, 44L, 55L, 66L, 77L, 88L, 99L)));
    }


    @Test
    void digits3() throws Throwable {
        List<Long> result = palindromes(3);

        assertThat("head", result.subList(0, 10), equalTo(asList(101L, 111L, 121L, 131L, 141L, 151L, 161L, 171L, 181L, 191L)));
        assertThat("tail", result.subList(80, 90), equalTo(asList(909L, 919L, 929L, 939L, 949L, 959L, 969L, 979L, 989L, 999L)));
        assertThat(result, hasSize(90));
    }


    @Test
    void digits4() throws Throwable {
        List<Long> result = palindromes(4);

        assertThat("head", result.subList(0, 10), equalTo(asList(1001L, 1111L, 1221L, 1331L, 1441L, 1551L, 1661L, 1771L, 1881L, 1991L)));
        assertThat("tail", result.subList(80, 90), equalTo(asList(9009L, 9119L, 9229L, 9339L, 9449L, 9559L, 9669L, 9779L, 9889L, 9999L)));
        assertThat(result, hasSize(90));
    }

    @Test
    void digits5() throws Throwable {
        List<Long> result = palindromes(5);

        assertThat("head", result.subList(0, 10), equalTo(asList(10001L, 10101L, 10201L, 10301L, 10401L, 10501L, 10601L, 10701L, 10801L, 10901L)));
        assertThat("tail", result.subList(890, 900), equalTo(asList(99099L, 99199L, 99299L, 99399L, 99499L, 99599L, 99699L, 99799L, 99899L, 99999L)));
        assertThat(result, hasSize(900));
    }

    @Test
    void digits9() throws Throwable {
        List<Long> result = palindromes(9);

        assertThat(result, hasSize((int) (9 * Math.pow(10, 4))));
    }

    @Test
    void max10() throws Throwable {
        List<Long> result = palindromes(10);

        assertThat(result.get(result.size() - 1), equalTo(9999999999L));
    }

    private static List<Long> palindromes(int digits) {
        return Palindromes.of(digits).boxed().collect(Collectors.toList());
    }

}
