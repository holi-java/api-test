package test.java.utils.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CompletableFuturePatternTest {

    @Test
    @DisplayName("Two-to-One Selecting Pattern")
    void selectingManyToOne() throws Throwable {
        String user = select("select user from User", String.class)
                .from(availableServers())
                .getFirstResult();

        assertThat(user, equalTo("Joe"));
    }

    @Test
    @DisplayName("Two-to-One Combining Pattern")
    void combiningManyToOne() throws Throwable {
        List<String> users = select("select user from User", String.class)
                .from(availableServers())
                .list();

        assertThat(users, equalTo(asList("Bob", "Joe", "Doe")));
    }

    @Test
    @DisplayName("One-to-One Pattern")
    void waitUntilUpstreamCompleted() throws Throwable {
        String user = select("select user from User", String.class)
                .from(availableServers())
                .to(String::toUpperCase);

        assertThat(user, equalTo("JOE"));
    }

    private CompletableFuture<String>[] availableServers() {
        return new CompletableFuture[]{
                server(returnValueLater("Bob")),
                server(returnValue("Joe")),
                server(returnValueLater("Doe")),
        };
    }

    private <T> CompletableFuture<T> server(Supplier<T> supplier) {
        return supplyAsync(supplier);
    }

    private <T> Supplier<T> returnValue(T value) {
        return returnValue(() -> value);
    }


    private <T> Supplier<T> returnValueLater(T value) {
        return returnValue(() -> {
            Thread.sleep(500);
            return value;
        });
    }

    private <T> Supplier<T> returnValue(Callable<T> value) {
        return () -> {
            try {
                return value.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T> Query<T> select(String query, Class<T> type) {
        return new Query<T>() {
            private CompletableFuture<T>[] upstreams;

            @Override
            public Query<T> from(CompletableFuture<T>... upstreams) {
                this.upstreams = upstreams;
                return this;
            }

            @Override
            public T getFirstResult() throws Exception {
                return selecting().get();
            }

            @Override
            public <R> R to(Function<T, R> mapper) throws Exception {
                return selecting().thenApply(mapper).get();
            }

            private CompletableFuture<T> selecting() {
                return upstreams(blocked(), this::selecting);
            }

            private CompletableFuture<T> selecting(CompletableFuture<T> primary,
                                                   CompletableFuture<T> upstream) {
                return primary.applyToEitherAsync(upstream, Function.identity());
            }

            private CompletableFuture<T> blocked() {
                return new CompletableFuture<>();
            }

            @Override
            public List<T> list() throws Exception {
                return upstreams(collector(), this::combine, this::combiner).get();
            }

            private CompletableFuture<List<T>> collector() {
                return completedFuture(new ArrayList<>());
            }

            private CompletableFuture<List<T>> combine(CompletableFuture<List<T>> primary,
                                                       CompletableFuture<T> upstream) {
                return primary.thenCombineAsync(upstream, this::concat);
            }

            private List<T> concat(List<T> result, T value) {
                result.add(value);
                return result;
            }

            private CompletableFuture<List<T>> combiner(CompletableFuture<List<T>> primary
                    , CompletableFuture<List<T>> secondary) {

                return primary.thenCombineAsync(secondary, this::concat);
            }

            private <T> List<T> concat(List<T> primary, List<T> secondary) {
                primary.addAll(secondary);
                return primary;
            }

            private CompletableFuture<T> upstreams(CompletableFuture<T> identity,
                                                   BinaryOperator<CompletableFuture<T>> accumulator) {
                return upstreams(identity, accumulator, accumulator);
            }

            private <U> CompletableFuture<U> upstreams(CompletableFuture<U> identity
                    , BiFunction<CompletableFuture<U>, CompletableFuture<T>, CompletableFuture<U>> accumulator
                    , BinaryOperator<CompletableFuture<U>> combiner) {
                return Stream.of(upstreams).reduce(identity, accumulator, combiner);
            }

        };
    }

    interface Query<T> {
        Query<T> from(CompletableFuture<T>... upstreams);

        T getFirstResult() throws Exception;

        <R> R to(Function<T, R> mapper) throws Exception;

        List<T> list() throws Exception;
    }
}