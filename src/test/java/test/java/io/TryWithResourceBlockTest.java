package test.java.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.holi.utils.CardinalMatchers.exactly;
import static com.holi.utils.CardinalMatchers.never;
import static com.holi.utils.CardinalMatchers.once;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by holi on 4/28/17.
 */
public class TryWithResourceBlockTest {
    private AtomicInteger closed = new AtomicInteger();

    @Test
    void closesResourceAfterExitTheBlock() throws Throwable {
        try (AutoCloseable resource = whenClosed(closed)) {
            assertThat(closed, never());
        }

        assertThat(closed, once());
    }

    @Test
    void closesResourceEvenIfFailsInBlock() throws Throwable {
        assertThrows(RuntimeException.class, () -> {
            try (AutoCloseable resource = whenClosed(closed)) {
                throw new RuntimeException();
            }
        });

        assertThat(closed, once());
    }

    @Test
    void catchesExceptionFailsOnClosesResource() throws Throwable {
        try (AutoCloseable resource = failsOnClosing(new IOException())) {

        } catch (IOException expected) {
            assertTrue(true);
            return;
        }

        fail("Exception can't be caught that fails on closes a resource");
    }

    @Test
    void catchesExceptionFailsInBlock() throws Throwable {
        try (AutoCloseable resource = whenClosed(closed)) {
            throw new RuntimeException();
        } catch (RuntimeException expected) {
            assertTrue(true);
            if (true) return;// the fail method can't be reached.
        }

        fail("Exception can't be caught that fails on closes a resource");
    }

    @Test
    void reportsExceptionWhenFailsOnClosesResource() throws Throwable {
        assertThrows(IOException.class, () -> {
            try (AutoCloseable resource = failsOnClosing(new IOException())) { /**/ }
        });
    }

    @Test
    void reportsExceptionFailsInBlockEvenIfFailsOnClosesResource() throws Throwable {
        assertThrows(RuntimeException.class, () -> {
            try (AutoCloseable resource = failsOnClosing(new IOException())) {
                throw new RuntimeException();
            }
        });
    }

    @Test
    void dropsTheClosingExceptionWhenFailsOnFinallyBlock() throws Throwable {
        assertThrows(Error.class, () -> {
            try (AutoCloseable resource = failsOnClosing(new IOException())) {/**/} finally {
                throw new Error();
            }
        });
    }

    @Test
    void dropsTheExceptionFailsInBlockWhenFailsOnFinallyBlock() throws Throwable {
        assertThrows(Error.class, () -> {
            try (AutoCloseable resource = whenClosed(closed)) {
                throw new RuntimeException();
            } finally {
                throw new Error();
            }
        });
    }

    @Test
    void tryWithMultiResources() throws Throwable {
        try (AutoCloseable first = whenClosed(closed); AutoCloseable last = whenClosed(closed)) {
            assertThat(closed, never());
        }

        assertThat(closed, exactly(2));
    }

    @Test
    void closesRestOfResourcesEvenIfFailsOnPreviousResource() throws Throwable {
        assertThrows(IOException.class, () -> {
            try (AutoCloseable first = failsOnClosing(new IOException()); AutoCloseable last = whenClosed(closed)) {
                assertThat(closed, never());
            }
        });


        assertThat(closed, once());
    }

    @Test
    void closesPreviousResourcesEvenIfFailsOnNextResources() throws Throwable {
        assertThrows(IOException.class, () -> {
            try (AutoCloseable first = whenClosed(closed); AutoCloseable last = failsOnClosing(new IOException())) {
                assertThat(closed, never());
            }
        });


        assertThat(closed, once());
    }

    private AutoCloseable whenClosed(AtomicInteger closed) {
        return () -> closed.incrementAndGet();
    }

    private AutoCloseable failsOnClosing(IOException error) {
        return () -> {
            throw error;
        };
    }
}
