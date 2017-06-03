package test.java.streams;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.holi.utils.CardinalMatchers.once;
import static java.util.Spliterator.CONCURRENT;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by holi on 6/2/17.
 */
public class StreamArrayGeneratorFunctionTest {

    @Test
    void createsArrayOnceInSequentialArrayStreamWithStatelessOperations() throws Throwable {
        arrayStream().filter(this::condition).toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInSequentialRangeStreamWithStatelessOperations() throws Throwable {
        rangeStream().filter(this::condition).toArray(expectsToCreateArray(once()));
    }


    @Test
    void createsArrayOnceInSequentialUnknownSizedStreamWithStatelessOperations() throws Throwable {
        unknownSizeStream().filter(this::condition).toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInSequentialArrayStreamWithStatefulOperations() throws Throwable {
        arrayStream().filter(this::condition).sorted().toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInSequentialRangeStreamWithStatefulOperations() throws Throwable {
        rangeStream().filter(this::condition).sorted().toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInSequentialUnknownSizedStreamWithStatefulOperations() throws Throwable {
        unknownSizeStream().filter(this::condition).sorted().toArray(expectsToCreateArray(once()));
    }


    @Test
    void createsArrayOnceInParallelArrayStreamWithStatelessOperations() throws Throwable {
        arrayStream().parallel().filter(this::condition).toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInParallelRangeStreamWithStatelessOperations() throws Throwable {
        rangeStream().parallel().filter(this::condition).toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInUnknownSizedParallelStreamWithStatelessOperations() throws Throwable {
        unknownSizeStream().parallel().filter(this::condition).toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInParallelArrayStreamWithStatefulOperations() throws Throwable {
        rangeStream().parallel().sorted().toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInParallelRangeStreamWithStatefulOperations() throws Throwable {
        rangeStream().parallel().sorted().toArray(expectsToCreateArray(once()));
    }

    @Test
    void createsArrayOnceInParallelUnknownSizedStreamWithStatefulOperations() throws Throwable {
        unknownSizeStream().parallel().sorted().toArray(expectsToCreateArray(once()));
    }

    private boolean condition(String it) {
        return it.length() % 2 == 0;
    }

    private IntFunction<String[]> expectsToCreateArray(Matcher<Number> cardinality) {
        AtomicInteger times = new AtomicInteger();
        return size -> {
            assertThat(times.incrementAndGet(), cardinality);
            return new String[size];
        };
    }

    private Stream<String> rangeStream() {
        return IntStream.rangeClosed(1, 1000000).mapToObj(String::valueOf);
    }

    private Stream<String> arrayStream() {
        return Stream.of(rangeStream().collect(toList()).toArray(new String[0]));
    }

    private Stream<String> unknownSizeStream() {
        return stream(spliteratorUnknownSize(rangeStream().iterator(), CONCURRENT), false);
    }
}
