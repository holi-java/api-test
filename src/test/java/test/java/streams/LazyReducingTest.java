package test.java.streams;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LazyReducingTest {
    private final List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    private final Predicate<Integer> condition = it -> it <= 10;

    @Test
    void sumAsInt() throws Throwable {
        assertThat(SumOp.of(Integer.class).sum(numbers, condition), equalTo(55));
    }


    @Test
    void sumAsDouble() throws Throwable {
        assertThat(SumOp.of(Double.class).sum(numbers, condition), equalTo(55.));
    }

    @Test
    void throwsIllegalArgumentExceptionWhenNoSuchSumOpForSumming() throws Throwable {
        assertThrows(IllegalArgumentException.class, () -> SumOp.of(BigInteger.class).sum(numbers, condition));
    }

    static/**/
    public class SumOp<T extends Number> {
        private static final Map<Class<?>, SumOp<?>> OPERATORS = new HashMap<>();
        private final T identity;
        private final BinaryOperator<T> plusOp;
        private final Function<Number, T> valueExtractor;

        static {
           register(Integer.class, new SumOp<>(0, Integer::sum, Number::intValue));
           register(Double.class, new SumOp<>(0d, Double::sum, Number::doubleValue));
           //todo: add more SumOp for other Number types
        }

        public static <T extends Number> void register(Class<T> type,
                                                       SumOp<T> sumOp) {
            OPERATORS.put(type, sumOp);
        }

        public static <T extends Number> SumOp<T> of(Class<T> type) {
            return (SumOp<T>) OPERATORS.computeIfAbsent(type, it -> {
                String message = "No SumOp registered for type:" + type.getName();
                throw new IllegalArgumentException(message);
            });
        }

        public SumOp(T identity,
                     BinaryOperator<T> plusOp,
                     Function<Number, T> valueExtractor) {
            this.identity = identity;
            this.valueExtractor = valueExtractor;
            this.plusOp = plusOp;
        }

        public <I extends Number> T sum(List<I> numbers,
                                        Predicate<I> condition) {
            return sum(numbers.parallelStream().filter(condition));
        }

        public T sum(Stream<? extends Number> stream) {
            return stream.reduce(identity, this::plus, plusOp);
        }

        private T plus(Number augend, Number addend) {
            return plusOp.apply(valueIn(augend), valueIn(addend));
        }

        private T valueIn(Number it) {
            return valueExtractor.apply(it);
        }
    }
}