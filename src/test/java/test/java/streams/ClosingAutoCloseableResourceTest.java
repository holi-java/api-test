package test.java.streams;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/25/17.
 */
public class ClosingAutoCloseableResourceTest {
    private final AtomicInteger close = new AtomicInteger();
    private final AutoCloseable resource = close::incrementAndGet;

    @Test
    void closesResourceAndMappedStreamAfterMappedStreamConsumedOnMainStream() throws Throwable {
        Stream<AutoCloseable> it = Stream.of(resource).flatMap(this::closeResourceWhenStreamClosed);
        assertThat(close, hasNeverBeenCalled());

        it.findAny();
        assertThat(close, hasBeenCalledOnce());
    }

    @Test
    void doesNotClosesResourceAndMappedStreamAfterMainStreamClosed() throws Throwable {
        Stream<AutoCloseable> it = Stream.of(resource).flatMap(this::closeResourceWhenStreamClosed);
        assertThat(close, hasNeverBeenCalled());

        it.close();
        assertThat(close, hasNeverBeenCalled());
    }

    @Test
    void doesNotClosesResourceAfterStreamConsumedAsMainStream() throws Throwable {
        Stream<AutoCloseable> main = closeResourceWhenStreamClosed(resource);
        assertThat(close, hasNeverBeenCalled());

        main.findAny();
        assertThat(close, hasNeverBeenCalled());
    }

    @Test
    void closesResourceAfterStreamClosedAsMainStream() throws Throwable {
        Stream<AutoCloseable> main = closeResourceWhenStreamClosed(resource);
        assertThat(close, hasNeverBeenCalled());

        main.close();
        assertThat(close, hasBeenCalledOnce());
    }


    private Stream<AutoCloseable> closeResourceWhenStreamClosed(AutoCloseable resource) {
        return Stream.of(resource).onClose(() -> closeQuietly(resource));
    }

    private void closeQuietly(AutoCloseable resource) {
        try {
            resource.close();
        } catch (Exception ignored) {}
    }

    private Matcher<Number> hasBeenCalledOnce() {
        return exactly(1);
    }

    private Matcher<Number> hasNeverBeenCalled() {
        return exactly(0);
    }

    private Matcher<Number> exactly(int times) {
        return new FeatureMatcher<Number, Integer>(equalTo(times), "call times", "") {
            @Override
            protected Integer featureValueOf(Number actual) {
                return actual.intValue();
            }
        };
    }

}
