package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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

    private Function<Stream<Integer>, Stream<Integer>> streamMode;
    final BiConsumer<Exception, Consumer<? super Integer>> SKIPPING = (ex, unused) -> {/**/};

    StreamExceptionallyTest(Function<Stream<Integer>, Stream<Integer>> streamMode) {
        this.streamMode = streamMode;
    }


    @Test
    @Feature("Rethrowing custom Exception")
    void rethrowExceptionByExceptionHandler() throws Throwable {
        RuntimeException expected = new RuntimeException();

        Stream<Integer> it = testWith(Stream.of("bad").map(Integer::parseInt), (ex, unused) -> {
            throw expected;
        });

        Throwable actual = assertThrows(RuntimeException.class, it::count);

        assertThat(actual, sameInstance(expected));
    }

    @Test
    @Feature("Applying default value for the failed operations")
    void applyingDefaultValueForTheFailedOperations() throws Throwable {
        final int DEFAULT_VALUE = -1;

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
        RuntimeException expected = new RuntimeException();
        Stream<Integer> it = testWith(Stream.of(1, 2, 3), SKIPPING);

        Throwable actual = assertThrows(RuntimeException.class, () -> it.reduce((ex, unused) -> {
            throw expected;
        }));

        assertThat(actual, sameInstance(expected));
    }

    Stream<Integer> testWith(Stream<Integer> source, BiConsumer<Exception, Consumer<? super Integer>> handler) {
        return exceptionally(streamMode.apply(source), handler);
    }

    <T> Stream<T> exceptionally(Stream<T> source, BiConsumer<Exception, Consumer<? super T>> handler) {
        class ExceptionallySpliterator extends AbstractSpliterator<T>
                implements Consumer<T> {

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


class SequentialStreamExceptionallyTest extends StreamExceptionallyTest {

    SequentialStreamExceptionallyTest() {
        super(Stream::sequential);
    }
}

class ParallelStreamExceptionallyTest extends StreamExceptionallyTest {

    ParallelStreamExceptionallyTest() {
        super(Stream::parallel);
    }
}