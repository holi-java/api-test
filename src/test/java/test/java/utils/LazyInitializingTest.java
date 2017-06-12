package test.java.utils;


import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class LazyInitializingTest {
    static final int MAX_THREADS = 20;
    final Supplier<Integer> initializer = new AtomicInteger(0)::incrementAndGet;

    @Test
    public void initializingOnce() throws Throwable {
        Supplier<Integer> supplier = once(initializer);

        assertThat(supplier.get(), equalTo(1));
        assertThat(supplier.get(), equalTo(1));
    }

    @Test
    public void returnValueDirectlyIfValueIsNotNull() throws Throwable {
        Supplier<?> supplier = once(-1, initializer);

        assertThat(supplier.get(), equalTo(-1));
        assertThat(supplier.get(), equalTo(-1));
    }

    @Test
    public void disableLockWhenValueWasEvaluated() throws Throwable {
        int delay = 100;
        Supplier<Integer> it = sync(once(delay(initializer, delay)));

        assertTimeout(Duration.ofMillis(delay * MAX_THREADS), () -> {
            assertThat(parallelRepeat(it::get, delay * 1000), equalTo(singleton(1)));
        });
    }


    static <T> Supplier<T> sync(Supplier<T> target) {
        return sync(new ReentrantLock(), target);
    }

    static <T> Supplier<T> sync(ReentrantLock lock, Supplier<T> target) {
        //     v--- synchronizing for multi-threads once
        return once(() -> {
            lock.lock();
            try {
                return target.get();
            } finally {
                lock.unlock();
            }
        });
    }

    static <T> Supplier<T> once(T value, Supplier<T> defaults) {
        return once(() -> value != null ? value : defaults.get());
    }

    static <T> Supplier<T> once(Supplier<T> target) {
        return new Supplier<T>() {
            private Supplier<T> delegate = () -> {
                T it = target.get();
                //v--- return the evaluated value in turn
                delegate = () -> it;
                return it;
            };

            @Override
            public T get() {
                return delegate.get();
            }
        };
    }

    private <T> Supplier<T> delay(Supplier<T> target, long millis) {
        return () -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {/**/ }
            return target.get();
        };
    }

    private <T> Set<T> parallelRepeat(Callable<T> task, int times) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(MAX_THREADS);
        CountDownLatch blocking = new CountDownLatch(1);
        Set<T> result = new HashSet<>();

        List<Future<T>> futures = IntStream.range(0, times).mapToObj(i -> service.submit(await(task, blocking))).collect(toList());

        blocking.countDown();

        for (Future<T> future : futures) {
            result.add(future.get());
        }

        return result;
    }

    <T> Callable<T> await(Callable<T> task, CountDownLatch blocking) {
        return () -> {
            blocking.await();
            return task.call();
        };
    }


}
