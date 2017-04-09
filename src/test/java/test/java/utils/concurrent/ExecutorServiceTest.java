package test.java.utils.concurrent;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutorServiceTest {
    private Path path;

    @BeforeEach
    void createTemporaryFile() throws Throwable {
        path = Files.createTempFile("test", "test");
    }

    @AfterEach
    void deleteTemporaryFile() throws Throwable {
        Files.delete(path);
    }

    @Test
    public void fileExists() throws Throwable {
        Files.write(path, "foo\nbar\nbaz".getBytes());
        assertPathLines(path, equalTo(3L));
    }

    @Test
    public void fileNotExists() throws Throwable {
        assertReadingPathLinesFailsWith(get("<unknown path>"), is(instanceOf(NoSuchFileException.class)));
    }

    private void assertReadingPathLinesFailsWith(Path path, Matcher<Throwable> errorMatcher) throws InterruptedException {
        assertReadingPathLinesMatching(path, is(nullValue()), errorMatcher);
    }

    private void assertPathLines(Path path, Matcher<Long> linesMatcher) throws InterruptedException {
        assertReadingPathLinesMatching(path, linesMatcher, is(nullValue()));
    }

    private void assertReadingPathLinesMatching(Path path,
                                                Matcher<? super Long> linesMatcher,
                                                Matcher<? super Throwable> errorsMatcher) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch blocking = new CountDownLatch(1);

        executor.execute(new Producer(path, matching(linesMatcher, errorsMatcher, blocking)));

        assertTrue(blocking.await(1, TimeUnit.SECONDS), "producer is running");
    }

    private BiConsumer<Long, Throwable> matching(Matcher<? super Long> linesMatcher, Matcher<? super Throwable> errorMatcher, CountDownLatch blocking) {
        return (lines, error) -> {
            try {
                assertThat(lines, linesMatcher);
                assertThat(error, errorMatcher);
            } finally {
                blocking.countDown();
            }
        };
    }

    private class Producer implements Runnable {
        private Path path;
        private BiConsumer<Long, Throwable> action;

        public Producer(Path path, BiConsumer<Long, Throwable> action) {
            this.path = path;
            this.action = action;
        }


        @Override
        public void run() {
            try (Stream<String> lines = Files.lines(path)) {
                action.accept(lines.count(), null);
            } catch (IOException e) {
                action.accept(null, e);
            }
        }
    }
}