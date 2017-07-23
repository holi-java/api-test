package test.java.streams;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.holi.utils.CardinalMatchers.once;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Spliterator.CONCURRENT;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterators.spliterator;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test.java.streams.StreamExceptionTracker.shouldStopTraversing;

@interface Feature {
    String value();
}

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
abstract class StreamExceptionallyTest {

    static final int DEFAULT_VALUE = -1;
    static final BiConsumer<Exception, Consumer<? super Integer>> SKIPPING = (ex, unused) -> {/**/};
    static final BiConsumer<Exception, Consumer<? super Integer>> DEFAULT = (ex, action) -> action.accept(DEFAULT_VALUE);

    private Function<Stream<Integer>, Stream<Integer>> streamMode;

    StreamExceptionallyTest(Function<Stream<Integer>, Stream<Integer>> streamMode) {
        this.streamMode = streamMode;
    }

    @Test
    @Feature("Rethrowing custom Exception")
    void rethrowExceptionByExceptionHandler() throws Throwable {
        RuntimeException expected = createAnExceptionDisablingRethrowingException();

        //@formatter:off
        Stream<Integer> it = testWith(Stream.of("bad"), (ex, unused) -> { throw expected; });
        //@formatter:on

        assertThat(assertThrows(RuntimeException.class, it::count), sameInstance(expected));
    }

    @Test
    @Feature("Applying default value for the failed operations")
    void applyingDefaultValueForTheFailedOperations() throws Throwable {
        assertThat(testWith(Stream.of("bad"), DEFAULT).collect(toList()), equalTo(singletonList(DEFAULT_VALUE)));
    }

    @Test
    @Feature(value = "skip processing for the failed operations")
    void skipProcessingForTheFailedOperations() throws Throwable {
        assertThat(testWith(Stream.of("bad"), SKIPPING).collect(toList()), is(emptyList()));
    }

    @Test
    void collectingElementsFromSourceStream() throws Throwable {
        final BiConsumer<Exception, Consumer<? super Integer>> UNUSED = null;

        assertThat(testWith(Stream.of("1"), UNUSED).collect(toList()), is(singletonList(1)));
    }

    @Test
    void continueToProcessingRemainingValidElementsWhenOccursFailedOperations() throws Throwable {
        assertThat(testWith(Stream.of("1", "bad", "2"), SKIPPING).collect(toList()), is(asList(1, 2)));
    }

    @Test
    void failFastWhenSubsequentOperationsFailed() throws Throwable {
        RuntimeException expected = createAnExceptionDisablingRethrowingException();
        Stream<Integer> it = testWith(Stream.of("1", "2", "3"), SKIPPING);

        //@formatter:off
        Throwable actual = assertThrows(RuntimeException.class, () -> it.reduce((ex, unused) -> { throw expected; }));
        //@formatter:on
        assertThat(actual, sameInstance(expected));
    }

    @Test
    void closesSourceStreamWhenExceptionallyStreamClosed() throws Throwable {
        AtomicInteger closes = new AtomicInteger(0);

        testWith(Stream.<String>empty().onClose(closes::incrementAndGet), SKIPPING).close();

        assertThat(closes, once());
    }

    @Test
    void supportsInfiniteStream() throws Throwable {
        final int LIMIT = 100;
        Stream<String> infinity = Stream.iterate("bad", identity());

        List<Integer> result = testWith(infinity, DEFAULT).limit(LIMIT).collect(toList());

        assertThat(result, hasSize(LIMIT));
        assertThat(result.stream().distinct().collect(toList()), equalTo(singletonList(DEFAULT_VALUE)));
    }

    @Test
    void eachStreamConsumerIsThreadSafe() throws Throwable {
        Map<Consumer<?>, Set<Thread>> threads = new ConcurrentHashMap<>();

        testWithParallelism(action -> {
            synchronized (threads) {
                threads.computeIfAbsent(action, __ -> new HashSet<>()).add(Thread.currentThread());
            }
        });

        threads.forEach((consumer, related) -> assertThat(related, hasSize(1)));
    }

    @Test
    void supportConcurrentSpliteratorWithBadEstimateSize() throws Throwable {
        List<Integer> expected = streamWithBadEstimateSize(CONCURRENT).filter(Objects::nonNull).map(Integer::parseInt).collect(toList());

        List<Integer> actual = testWith(streamWithBadEstimateSize(CONCURRENT), SKIPPING).collect(toList());

        assertThat(actual, equalTo(expected));
    }

    @Test
    void supportSizedSpliteratorWithBadEstimateSize() throws Throwable {
        List<Integer> expected = streamWithBadEstimateSize(SIZED).filter(Objects::nonNull).map(Integer::parseInt).collect(toList());

        List<Integer> actual = testWith(streamWithBadEstimateSize(SIZED), SKIPPING).collect(toList());

        assertThat(actual, equalTo(expected));
    }

    private Stream<String> streamWithBadEstimateSize(int characteristics) {
        int badEstimateSize = 1;

        return stream(spliterator(asList("1", null, "2").iterator(), badEstimateSize, characteristics), false);
    }

    private RuntimeException createAnExceptionDisablingRethrowingException() {
        // @formatter:off
        return new RuntimeException() {/*disable rethrow exception by ForkJoinTask*/};
        // @formatter:on
    }

    private Stream<Integer> testWith(Stream<String> source, BiConsumer<Exception, Consumer<? super Integer>> handler) {
        return exceptionally(streamMode.apply(source.map(Integer::parseInt)), handler);
    }

    protected Set<Thread> collectParallelismThreads() throws Throwable {
        Set<Thread> threads = new CopyOnWriteArraySet<>();
        testWithParallelism(action -> threads.add(Thread.currentThread()));
        return threads;
    }

    private void testWithParallelism(Consumer<Consumer<? super Integer>> collector) throws Throwable {
        Stream<String> source = Stream.iterate("bad", identity()).limit(1000);
        new ForkJoinPool(20).submit(testWith(source, (e, action) -> collector.accept(action))::count).get();
    }

    abstract <T> Stream<T> exceptionally(Stream<T> source, BiConsumer<Exception, Consumer<? super T>> handler);

}

class AllTests {

    @Nested
    class TestWithSpliterator {
        @Nested
        class SequentialStreamTest extends ExceptionHandlingBySpliterator {
            SequentialStreamTest() {
                super(Stream::sequential);
            }

        }

        @Nested
        class ParallelStreamTest extends ExceptionHandlingBySpliterator {
            ParallelStreamTest() {
                super(Stream::parallel);
            }

            @Test
            void streamParallelismEnabled() throws Throwable {
                assertThat(collectParallelismThreads(), hasSize(greaterThan(2)));
            }
        }

        abstract class ExceptionHandlingBySpliterator extends StreamExceptionallyTest {
            ExceptionHandlingBySpliterator(Function<Stream<Integer>, Stream<Integer>> streamMode) {
                super(streamMode);
            }

            <T> Stream<T> exceptionally(Stream<T> source, BiConsumer<Exception, Consumer<? super T>> handler) {
                class ExceptionallySpliterator extends AbstractSpliterator<T> implements Consumer<T> {

                    private Spliterator<T> source;
                    private boolean valueInReady = false;
                    private T value;
                    private boolean stop;

                    private ExceptionallySpliterator(Spliterator<T> source) {
                        super(source.estimateSize(), source.characteristics());
                        this.source = source;
                    }

                    @Override
                    public Spliterator<T> trySplit() {
                        Spliterator<T> it = source.trySplit();
                        return it == null ? null : new ExceptionallySpliterator(it);
                    }

                    @Override
                    public boolean tryAdvance(Consumer<? super T> action) {
                        if (stop) return false;

                        boolean exists = tryConsuming(action);
                        if (valueInReady) action.accept(dump());
                        return exists;
                    }


                    private boolean tryConsuming(Consumer<? super T> action) {
                        try {
                            return source.tryAdvance(this);
                        } catch (Exception ex) {
                            stop = shouldStopTraversing(ex);
                            handler.accept(ex, action);
                            return true;
                        }
                    }

                    private T dump() {
                        valueInReady = false;
                        T result = value;
                        value = null;
                        return result;
                    }


                    @Override
                    public void accept(T value) {
                        valueInReady = true;
                        this.value = value;
                    }
                }

                return stream(new ExceptionallySpliterator(source.spliterator()), source.isParallel()).onClose(source::close);
            }
        }
    }

    @Nested
    class TestWithIterator {
        @Nested
        class SequentialStreamTest extends ExceptionHandlingByIterator {

            SequentialStreamTest() {
                super(Stream::sequential);
            }

        }

        @Nested
        class ParallelStreamTest extends ExceptionHandlingByIterator {

            ParallelStreamTest() {
                super(Stream::parallel);
            }

            @Test
            void parallelismDisabled() throws Throwable {
                //TODO: how to enable parallelism for Iterator?
                assertThat(collectParallelismThreads(), hasSize(1));
            }

        }


        abstract class ExceptionHandlingByIterator extends StreamExceptionallyTest {
            ExceptionHandlingByIterator(Function<Stream<Integer>, Stream<Integer>> streamMode) {
                super(streamMode);
            }

            <T> Stream<T> exceptionally(Stream<T> source, BiConsumer<Exception, Consumer<? super T>> handler) {
                return stream(spliteratorUnknownSize(exceptionally(source.iterator(), handler), 0), false).onClose(source::close);
            }


            //Don't worried the thread-safe & robust since it is invisible for anyone
            private <T> Iterator<T> exceptionally(Iterator<T> source, BiConsumer<Exception, Consumer<? super T>> handler) {
                class ExceptionallyIterator implements Iterator<T>, Consumer<T> {
                    private T value;
                    private boolean valueInReady = false;
                    private boolean stop = false;

                    @Override
                    public boolean hasNext() {
                        while (true) {
                            if (valueInReady) return true;
                            if (stop) return false;
                            try {
                                return source.hasNext();
                            } catch (Exception ex) {
                                stop = shouldStopTraversing(ex);
                                handler.accept(ex, this);
                            }
                        }
                    }

                    @Override
                    public T next() {
                        return valueInReady ? dump() : source.next();
                    }

                    private T dump() {
                        valueInReady = false;
                        T result = value;
                        value = null;
                        return result;
                    }

                    @Override
                    public void accept(T value) {
                        this.value = value;
                        this.valueInReady = true;
                    }
                }
                return new ExceptionallyIterator();
            }

        }

    }
}


class StreamExceptionTracker {

    private static final String STREAM_BUG_CLASS = "java.util.stream.Streams$StreamBuilderImpl";

    public static boolean shouldStopTraversing(Exception ex) {
        for (StackTraceElement element : ex.getStackTrace()) {
            if (STREAM_BUG_CLASS.equals(element.getClassName())) {
                return true;
            }
        }
        return false;
    }
}