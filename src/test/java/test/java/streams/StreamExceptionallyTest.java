package test.java.streams;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.holi.utils.CardinalMatchers.once;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Spliterators.spliterator;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@interface Feature {
    String value();
}

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
abstract class StreamExceptionallyTest {

    static final BiConsumer<Exception, Consumer<? super Integer>> SKIPPING = (ex, unused) -> {/**/};
    static final int DEFAULT_VALUE = -1;

    private Function<Stream<Integer>, Stream<Integer>> streamMode;

    StreamExceptionallyTest(Function<Stream<Integer>, Stream<Integer>> streamMode) {
        this.streamMode = streamMode;
    }


    @Test
    @Feature("Rethrowing custom Exception")
    void rethrowExceptionByExceptionHandler() throws Throwable {
        RuntimeException expected = createAnDisabledRethrowingException();
        //@formatter:off
        Stream<Integer> it = testWith(Stream.of("bad").map(Integer::parseInt), (ex, unused) -> { throw expected; });
        //@formatter:on

        Throwable actual = assertThrows(RuntimeException.class, it::count);
        assertThat(actual, sameInstance(expected));
    }

    @Test
    @Feature("Applying default value for the failed operations")
    void applyingDefaultValueForTheFailedOperations() throws Throwable {
        Stream<Integer> it = testWith(Stream.of("bad").map(Integer::parseInt), (ex, action) -> action.accept(DEFAULT_VALUE));

        assertThat(it.collect(toList()), equalTo(singletonList(DEFAULT_VALUE)));
    }

    @Test
    @Feature(value = "skip processing for the failed operations")
    void skipProcessingForTheFailedOperations() throws Throwable {
        Stream<Integer> it = testWith(Stream.of("bad").map(Integer::parseInt), SKIPPING);

        assertThat(it.collect(toList()), is(emptyList()));
    }

    @Test
    void collectingElementsFromSourceStream() throws Throwable {
        final BiConsumer<Exception, Consumer<? super Integer>> UNUSED = null;

        Stream<Integer> it = testWith(Stream.of("1").map(Integer::parseInt), UNUSED);

        assertThat(it.collect(toList()), is(singletonList(1)));
    }

    @Test
    void continueToProcessingRemainingValidElementsWhenOccursFailedOperations() throws Throwable {
        Stream<Integer> it = testWith(Stream.of("1", "bad", "2").map(Integer::parseInt), SKIPPING);

        assertThat(it.collect(toList()), is(asList(1, 2)));
    }

    @Test
    void failFastWhenSubsequentOperationsFailed() throws Throwable {
        RuntimeException expected = createAnDisabledRethrowingException();

        Stream<Integer> it = testWith(Stream.of(1, 2, 3), SKIPPING);

        //@formatter:off
        Throwable actual = assertThrows(RuntimeException.class, () -> it.reduce((ex, unused) -> { throw expected; }));
        //@formatter:on
        assertThat(actual, sameInstance(expected));
    }

    @Test
    void closesSourceStreamWhenExceptionallyStreamClosed() throws Throwable {
        AtomicInteger closes = new AtomicInteger(0);

        testWith(Stream.<Integer>empty().onClose(closes::incrementAndGet), SKIPPING).close();

        assertThat(closes, once());
    }

    @Test
    void supportsInfiniteStream() throws Throwable {
        final int LIMIT = 100;
        Stream<Integer> infinity = Stream.iterate("bad", identity()).map(Integer::parseInt);

        List<Integer> result = testWith(infinity, (e, action) -> action.accept(DEFAULT_VALUE)).limit(LIMIT).collect(toList());

        assertThat(result, hasSize(LIMIT));
        assertThat(result.stream().distinct().collect(toList()), equalTo(singletonList(DEFAULT_VALUE)));
    }

    private RuntimeException createAnDisabledRethrowingException() {
        // @formatter:off
        return new RuntimeException() {/*disable rethrow exception by ForkJoinTask*/};
        // @formatter:on
    }

    Stream<Integer> testWith(Stream<Integer> source, BiConsumer<Exception, Consumer<? super Integer>> handler) {
        return exceptionally(streamMode.apply(source), handler);
    }

    abstract <T> Stream<T> exceptionally(Stream<T> apply, BiConsumer<Exception, Consumer<? super T>> handler);

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
        }

        abstract class ExceptionHandlingBySpliterator extends StreamExceptionallyTest {
            ExceptionHandlingBySpliterator(Function<Stream<Integer>, Stream<Integer>> streamMode) {
                super(streamMode);
            }

            <T> Stream<T> exceptionally(Stream<T> source, BiConsumer<Exception, Consumer<? super T>> handler) {
                class ExceptionallySpliterator extends AbstractSpliterator<T> implements Consumer<T> {

                    private Spliterator<T> source;
                    private T value;
                    private long fence;

                    ExceptionallySpliterator(Spliterator<T> source) {
                        super(source.estimateSize(), source.characteristics());
                        this.fence = source.getExactSizeIfKnown();
                        this.source = source;
                    }

                    @Override
                    public Spliterator<T> trySplit() {
                        Spliterator<T> it = source.trySplit();
                        return it == null ? null : new ExceptionallySpliterator(it);
                    }

                    @Override
                    public boolean tryAdvance(Consumer<? super T> action) {
                        return fence != 0 && consuming(action);
                    }

                    private boolean consuming(Consumer<? super T> action) {
                        Boolean state = tryConsuming(action);
                        if (state == null) {
                            return true;
                        }
                        if (state) {
                            action.accept(value);
                            value = null;
                            return true;
                        }
                        return false;
                    }


                    private Boolean tryConsuming(Consumer<? super T> action) {
                        fence--;
                        try {
                            return source.tryAdvance(this);
                        } catch (Exception ex) {
                            handler.accept(ex, action);
                            return null;
                        }
                    }

                    @Override
                    public void accept(T value) {
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

        }


        abstract class ExceptionHandlingByIterator extends StreamExceptionallyTest {
            ExceptionHandlingByIterator(Function<Stream<Integer>, Stream<Integer>> streamMode) {
                super(streamMode);
            }

            <T> Stream<T> exceptionally(Stream<T> source, BiConsumer<Exception, Consumer<? super T>> handler) {
                Spliterator<T> s = source.spliterator();
                return StreamSupport.stream(
                        spliterator(exceptionally(s, handler), s.estimateSize(), s.characteristics()),
                        false
                ).onClose(source::close);
            }


            //Don't worried the thread-safe & robust since it is invisible for anyone
            private <T> Iterator<T> exceptionally(Spliterator<T> spliterator, BiConsumer<Exception, Consumer<? super T>> handler) {
                class ExceptionallyIterator implements Iterator<T>, Consumer<T> {
                    private Iterator<T> source = Spliterators.iterator(spliterator);
                    private long fence = spliterator.getExactSizeIfKnown();
                    private T value;
                    private boolean valueInReady = false;

                    @Override
                    public boolean hasNext() {
                        if (fence == 0) return false;
                        if (valueInReady) return true;

                        try {
                            fence--;
                            return source.hasNext();
                        } catch (Exception ex) {
                            handler.accept(ex, this);
                            return valueInReady || hasNext();
                        }
                    }

                    @Override
                    public T next() {
                        return valueInReady ? dump() : source.next();
                    }

                    private T dump() {
                        T result = value;
                        valueInReady = false;
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


