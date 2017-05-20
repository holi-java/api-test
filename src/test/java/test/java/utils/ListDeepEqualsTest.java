package test.java.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 5/20/17.
 */
public class ListDeepEqualsTest {

    @Test
    void equality() throws Throwable {
        assertThat(asList(new byte[]{1}), deepEqualTo(asList(new byte[]{1})));
    }

    @Test
    void matchesNullValue() throws Throwable {
        assertThat(null, deepEqualTo(null));
    }

    @Test
    void reportsDiagnosticMessages() throws Throwable {
        Throwable error = assertThrows(AssertionError.class, () -> assertThat(asList(new byte[]{1}), deepEqualTo(asList(new byte[]{2}))));

        assertThat(error, hasMessage(containsString("Expected: <[[2]]>")));
        assertThat(error, hasMessage(containsString("but: was <[[1]]>")));
    }

    private Matcher<Throwable> hasMessage(Matcher<String> messageMatcher) {
        return hasProperty("message", messageMatcher);
    }

    Matcher<List<?>> deepEqualTo(List<?> expected) {
        return new BaseMatcher<List<?>>() {
            @Override
            public boolean matches(Object item) {
                return Objects.equals(arrayToList((List<?>) item), arrayToList(expected));
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("was ").appendValue(arrayToList((List<?>) item));
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(arrayToList(expected));
            }
        };
    }

    static List<?> arrayToList(List<?> it) {
        if (it == null) {
            return it;
        }
        List<Object> result = new ArrayList<>();
        for (Object item : it) {
            result.add(arrayToList(item));
        }
        return result;
    }

    static Object arrayToList(Object it) {
        if (!isArray(it)) {
            return it;
        }
        return Arrays.asList(toArray(it));
    }

    static Object[] toArray(Object it) {
        int n = Array.getLength(it);
        Object[] result = new Object[n];
        for (int i = 0; i < n; i++) {
            result[i] = Array.get(it, i);
        }
        return result;
    }

    static boolean isArray(Object it) {
        return it != null && it.getClass().isArray();
    }

}
