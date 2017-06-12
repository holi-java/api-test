package test.java.utils;


import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.time.Duration.ofMillis;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

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
    public void initializingOnceInMultiThreads() throws Throwable {
        int delay = 100;
        Supplier<Integer> it = sync(once(delay(initializer, delay)));

        assertTimeoutPreemptively(ofMillis(delay * 2), () -> {
            assertThat(parallelRepeat(it::get, MAX_THREADS), equalTo(singleton(1)));
        });
    }

    @Test
    public void disableLockWhenValueWasEvaluated() throws Throwable {
        AtomicInteger synchronizations = new AtomicInteger(0);
        Supplier<Integer> it = sync(onLock(synchronizations::incrementAndGet), once(initializer));

        parallelRepeat(it::get, MAX_THREADS * 100);

        assertThat(synchronizations.get(), lessThanOrEqualTo(MAX_THREADS));
    }

    static <T> Supplier<T> sync(Supplier<T> target) {
        return sync(new ReentrantLock(), target);
    }

    static <T> Supplier<T> sync(Lock lock, Supplier<T> target) {
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
            private Supplier<T> result = () -> {
                T evaluated = target.get();
                //v--- return the evaluated value in turn
                result = () -> evaluated;
                return evaluated;
            };

            @Override
            public T get() {
                return result.get();
            }
        };
    }

    private Lock onLock(Runnable action) {
        return new ReentrantLock() {
            @Override
            public void lock() {
                action.run();
                super.lock();
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
        return distinct(startRepeating(task, times));
    }

    private <T> Set<T> distinct(List<Future<T>> futures) throws InterruptedException, ExecutionException {
        Set<T> result = new HashSet<>();
        for (Future<T> future : futures) {
            result.add(future.get());
        }
        return result;
    }

    private <T> List<Future<T>> startRepeating(Callable<T> task, int times) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        return blocking(it -> IntStream.range(0, times).mapToObj(i -> executor.submit(await(task, it))).collect(toList()));
    }

    private <R> R blocking(Function<CountDownLatch, R> action) {
        CountDownLatch blocking = new CountDownLatch(1);
        R result = action.apply(blocking);
        blocking.countDown();
        return result;
    }

    <T> Callable<T> await(Callable<T> task, CountDownLatch blocking) {
        return () -> {
            blocking.await();
            return task.call();
        };
    }


}
