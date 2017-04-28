package test.java.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by holi on 4/28/17.
 */
public class TryWithResourceBlockTest {
    private AtomicBoolean closed = new AtomicBoolean(false);

    @Test
    void closesResourceAfterExitTheBlock() throws Throwable {
        try (AutoCloseable resource = whenClosed(closed)) {
            assertResourceHasNotBeenClosed();
        }

        assertResourceHasBeenClosed();
    }

    @Test
    void closesResourceEvenIfFailsInBlock() throws Throwable {
        assertThrows(RuntimeException.class, () -> {
            try (AutoCloseable resource = whenClosed(closed)) {
                throw new RuntimeException();
            }
        });

        assertResourceHasBeenClosed();
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

    private void assertResourceHasNotBeenClosed() {
        assertFalse(closed.get(), "stream is closed!");
    }

    private void assertResourceHasBeenClosed() {
        assertTrue(closed.get(), "stream is not closed!");
    }

    private AutoCloseable whenClosed(AtomicBoolean closed) {
        return () -> closed.set(true);
    }

    private AutoCloseable failsOnClosing(IOException error) {
        return () -> {
            throw error;
        };
    }
}
