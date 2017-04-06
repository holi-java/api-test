package test.java.lang;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/6/17.
 */
public class StringReplacingFirstTest {
    private final String string = "a, b, c";

    @Test
    void replaceLastWithoutLookahead() throws Throwable {
        assertThat(string.replaceFirst(",(?=[^,]+$)", " and"), equalTo("a, b and c"));
    }

    @Test
    void replaceLastAndOutCapturedGroup() throws Throwable {
        assertThat(string.replaceFirst(",([^,]+$)", " and$1"), equalTo("a, b and c"));
    }
}
