package test.design;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;
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
        return new Collector<T, A, R>() {
            @Override
            public Supplier<A> supplier() {
                return downstream.supplier();
            }

            @Override
            public BiConsumer<A, T> accumulator() {
                BiConsumer<A, T> target = downstream.accumulator();
                return (result, it) -> {
                    if (predicate.test(it)) {
                        target.accept(result, it);
                    }
                };
            }

            @Override
            public BinaryOperator<A> combiner() {
                return downstream.combiner();
            }

            @Override
            public Function<A, R> finisher() {
                return downstream.finisher();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return downstream.characteristics();
            }
        };
    }


}
