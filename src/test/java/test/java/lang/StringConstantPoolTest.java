package test.java.lang;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 3/31/17.
 */
public class StringConstantPoolTest {
    @Test
    void reusesStringLiterals() throws Throwable {
        String foo = "test";
        String bar = "test";

        assertThat(foo, is(sameInstance(bar)));
    }

    @Test
    void reusesStringLiteralsWhenConcatStringLiterals() throws Throwable {
        String foo = "test";
        String bar = "t" + "est";

        assertThat(foo, is(sameInstance(bar)));
    }


    @Test
    void reusesStringLiteralsWhenConcatFinalLocalVariables() throws Throwable {
        final String foot = "foot";
        final String ball = "ball";

        assertThat(foot + ball, is(sameInstance("football")));
    }


    @Test
    void reusesStringLiteralsWhenConcatFinalFields() throws Throwable {
        String football = new Object() {
            final String foot = "foot";
            final String ball = "ball";

            String join() {
                return foot + ball;
            }
        }.join();

        assertThat(football, is(sameInstance("football")));
    }

    @Test
    void reusesStringLiteralsByUsingIntern() throws Throwable {
        String foo = new String("test");

        assertThat(foo, is(not(sameInstance("test"))));
        assertThat(foo.intern(), is(sameInstance("test")));
    }

    @Test
    void donotReusesStringLiteralsWhenConcatNonFinalLocalVariables() throws Throwable {
        String foot = "foot";
        String ball = "ball";

        assertThat(foot + ball, is(not(sameInstance("football"))));
    }

    @Test
    void donotReusesStringLiteralsWhenConcatFinalParameters() throws Throwable {
        String foot = "foot";
        String ball = "ball";

        assertThat(concat(foot, ball), is(not(sameInstance("football"))));
    }

    private String concat(final String left, final String right) {
        return left + right;
    }
}
