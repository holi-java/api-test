package test.java.utils;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by holi on 5/20/17.
 */
public class ValueMappingExtensionTest {

    private final Map<String, String> map = new HashMap<>(Collections.singletonMap("foo", "bar"));

    @Test
    void donotConsumingIfValueAbsent() throws Throwable {
        map.compute("fuzz", ValueMapping.ifPresent(rejectAction()));
    }

    @Test
    void consumingIfValuePresent() throws Throwable {
        AtomicReference<String> ref = new AtomicReference<>();

        String result = map.compute("foo", ValueMapping.ifPresent(ref::set));

        assertThat(result, equalTo("bar"));
        assertThat(ref.get(), equalTo(result));
    }

    @Test
    void consumingOnlyIfValuePresent() throws Throwable {
        map.compute("foo", ValueMapping.ifPresent(anything()).orElse(rejectMapping()));
    }


    @Test
    void computesOnlyIfValueAbsent() throws Throwable {
        String result = map.compute("fuzz", ValueMapping.ifPresent(rejectAction()).orElse(() -> "buzz"));

        assertThat(result, equalTo("buzz"));
    }

    @Test
    void computesValueFromKeyIfKeyAbsent() throws Throwable {
        String result = map.compute("fuzz", ValueMapping.<String, String>ifPresent(anything()).orElse(String::toUpperCase));

        assertThat(result, equalTo("FUZZ"));
    }

    @Test
    void shortCircuitMappings() throws Throwable {
        String result = map.compute("fuzz", ValueMapping.ifPresent(anything())
                .orElse(() -> null)
                .orElse(() -> "buzz")
                .orElse(rejectMapping()));

        assertThat(result, equalTo("buzz"));
    }

    private Consumer<String> anything() {
        return it -> {/**/};
    }

    private Supplier<String> rejectMapping() {
        return () -> {
            fail("mapping has been called");
            return null;
        };
    }

    private Consumer<String> rejectAction() {
        return it -> fail("action has been called");
    }

    private /**/
    interface ValueMapping<K, V> extends BiFunction<K, V, V> {
        default ValueMapping<K, V> orElse(Supplier<V> other) {
            return orElse(k -> other.get());
        }

        default ValueMapping<K, V> orElse(Function<K, V> other) {
            return (k, v) -> {
                V result = apply(k, v);
                return !Objects.isNull(result) ? result : other.apply(k);
            };
        }

        static <K, V> ValueMapping<K, V> ifPresent(Consumer<V> action) {
            return (k, v) -> {
                if (!Objects.isNull(v)) {
                    action.accept(v);
                }
                return v;
            };
        }
    }
}
