package test.java.lang;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 3/31/17.
 */
public class StringPoolTest {
    @Test
    void sameStringLiteralsAreShared() throws Throwable {
        String foo = "test";
        String bar = "test";

        assertThat(foo, is(sameInstance(bar)));
    }

    @Test
    void stringLiteralsAreSharedToConnectedStringWhichAreTheSame() throws Throwable {
        String foo = "test";
        String bar = "t" + "est";

        assertThat(foo, is(sameInstance(bar)));
    }

    @Test
    void valueOfUsingStringPoolLiterals() throws Throwable {
        assertThat(String.valueOf("test"), is(sameInstance("test")));
    }

    @Test
    void usingSharedStringsThroughInternMethod() throws Throwable {
        String foo = new String("test");

        assertThat(foo, is(not(sameInstance("test"))));
        assertThat(foo.intern(), is(sameInstance("test")));
    }
}
