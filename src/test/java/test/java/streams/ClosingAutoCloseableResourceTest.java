package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.holi.utils.CardinalMatchers.never;
import static com.holi.utils.CardinalMatchers.once;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by holi on 4/25/17.
 */
public class ClosingAutoCloseableResourceTest {
    private final AtomicInteger closed = new AtomicInteger();
    private final AutoCloseable resource = closed::incrementAndGet;

    @Test
    void closesResourceAndMappedStreamAfterMappedStreamConsumedOnMainStream() throws Throwable {
        Stream<AutoCloseable> it = Stream.of(resource).flatMap(this::closeResourceWhenStreamClosed);
        assertThat(closed, never());

        it.findAny();
        assertThat(closed, once());
    }

    @Test
    void doesNotClosesResourceAndMappedStreamAfterMainStreamClosed() throws Throwable {
        Stream<AutoCloseable> it = Stream.of(resource).flatMap(this::closeResourceWhenStreamClosed);
        assertThat(closed, never());

        it.close();
        assertThat(closed, never());
    }

    @Test
    void doesNotClosesResourceAfterStreamConsumedAsMainStream() throws Throwable {
        Stream<AutoCloseable> main = closeResourceWhenStreamClosed(resource);
        assertThat(closed, never());

        main.findAny();
        assertThat(closed, never());
    }

    @Test
    void closesResourceAfterStreamClosedAsMainStream() throws Throwable {
        Stream<AutoCloseable> main = closeResourceWhenStreamClosed(resource);
        assertThat(closed, never());

        main.close();
        assertThat(closed, once());
    }


    private Stream<AutoCloseable> closeResourceWhenStreamClosed(AutoCloseable resource) {
        return Stream.of(resource).onClose(() -> closeQuietly(resource));
    }

    private void closeQuietly(AutoCloseable resource) {
        try {
            resource.close();
        } catch (Exception ignored) {}
    }

}
