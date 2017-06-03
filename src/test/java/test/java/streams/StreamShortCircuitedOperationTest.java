package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 5/26/17.
 */
public class StreamShortCircuitedOperationTest {


    @Test
    void makesFilterShortCircuited() throws Throwable {
        final Integer[] numbers = {1, 2, 3};

        Optional<Integer> result = Stream.of(numbers).filter(once(it -> true, failing("matching more than once!"))).findAny();

        assertThat(result.get(), isIn(numbers));
    }

    @Test
    void makesFiltersShortCircuitedAsConditionalAndOperator() throws Throwable {
        Predicate<Integer> expectsToBeShortCircuited = once(it -> false, failing("matching more than once!"));

        Optional<Integer> result = Stream.of(1).filter(expectsToBeShortCircuited).filter(expectsToBeShortCircuited).findAny();

        assertThat(result.isPresent(), is(false));
    }

    @Test
    void cannotMakesFiltersShortCircuitedAsConditionalOrOperator() throws Throwable {
        Predicate<Integer> expectsToBeShortCircuited = once(it -> true, failing("matching more than once!"));

        Stream<Integer> it = Stream.of(1).filter(expectsToBeShortCircuited).filter(expectsToBeShortCircuited);

        assertThrows(AssertionError.class, it::findAny);
    }

    private <T> Predicate<T> failing(String message) {
        return it -> {
            throw new AssertionError(message);
        };
    }

    private <T> Predicate<T> once(Predicate<T>... conditions) {
        Iterator<Predicate<T>> cursor = Arrays.stream(conditions).iterator();
        return it -> cursor.next().test(it);
    }

    interface Specification {

        boolean isSatisfied();
    }

    private Predicate<Specification> anything() {
        return item -> true;
    }

    private Specification failsIfNotShortCircuited() {
        return () -> {
            throw new IllegalStateException("specification is not short-circuited!");
        };
    }

    private Specification foo() {
        return () -> true;
    }
}
