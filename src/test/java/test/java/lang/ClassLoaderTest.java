package test.java.lang;

import com.holi.Library;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassLoaderTest {
    final Library<Integer> library = new Library<>(1, 2, 3);

    @Test
    void applyingFunction() throws Throwable {
        assertTrue(library.exists(function(item -> item == 1)));
        assertFalse(library.exists(function(item -> item == 4)));
    }

    @Test
    void applyingPredicate() throws Throwable {
        assertTrue(library.exists(match(item -> item == 1)));
        assertFalse(library.exists(match(item -> item == 4)));
    }

    public <T> Predicate<T> match(Predicate<T> origin) {
        return origin;
    }

    public <T, R> Function<T, R> function(Function<T, R> origin) {
        return origin;
    }

}
