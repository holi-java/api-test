package test.java.lang;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/10/17.
 */
public class CallingStaticMethodWithinNullReferenceTest {
    @Test
    void success() throws Throwable {
        Statical it = null;

        assertThat(it.foo().bar().calls, equalTo(Arrays.asList("foo", "bar")));
    }

    static class Statical {
        static final List<String> calls = new ArrayList<>();

        public static Statical foo() {
            return call("foo");
        }

        public static Statical bar() {
            return call("bar");
        }

        private static Statical call(String name) {
            calls.add(name);
            return null;
        }
    }
}
