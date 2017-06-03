package test.design;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 5/29/17.
 */
public class StreamCollectingAccordingSeparationOfConcernsTest {


    final Stream<? extends Number> numbers = Stream.of(1, 2L, 3, 4.);

    @Test
    void collectingTypeSafely() throws Throwable {
        List<Integer> result = numbers.collect(instanceOf(Integer.class, toList()));

        assertThat(result, equalTo(Arrays.asList(1, 3)));
    }

    @Test
    void mappingAndThenCollecting() throws Throwable {
        Integer result = numbers.collect(instanceOf(Integer.class, reducing(0, Integer::sum)));

        assertThat(result, equalTo(4));
    }

    static <T, U extends T, A, R> Collector<T, ?, R> instanceOf(Class<U> type, Collector<U, A, R> downstream) {
        return filtering​(type::isInstance, Collectors.mapping(type::cast, downstream));
    }

    static <T, A, R> Collector<T, ?, R> filtering​(Predicate<T> predicate, Collector<T, A, R> downstream) {
        return mapping(downstream, target -> (result, it) -> {
            if (predicate.test(it)) {
                target.accept(result, it);
            }
        });
    }

    private static <T, A, R> Collector<T, A, R> mapping(final Collector<T, A, R> downstream, final Function<BiConsumer<A, T>, BiConsumer<A, T>> mapping) {
        return Collector.of(
                downstream.supplier(),
                mapping.apply(downstream.accumulator()),
                downstream.combiner(),
                downstream.finisher(),
                downstream.characteristics().stream().toArray(Characteristics[]::new)
        );
    }


}


