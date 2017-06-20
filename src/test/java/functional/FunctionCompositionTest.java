package functional;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static functional.FunctionCompositionTest.Interval.*;
import static functional.FunctionCompositionTest.Interval.both;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunctionCompositionTest {

    @Test
    void date2() throws Throwable {
        assertThat(parse("2007-03-01T13:00:00Z/2008-05-11T15:30:00Z"), equalTo(between(datetime("2007-03-01T13:00:00"), datetime("2008-05-11T15:30:00"))));
    }

    @Test
    public void start() throws Throwable {
        assertThat(parse("2007-03-01T13:00:00Z/P1Y"), equalTo(between(datetime("2007-03-01T13:00:00Z"), datetime("2008-03-01T13:00:00Z"))));
    }

    @Test
    public void end() throws Throwable {
        assertThat(parse("P1Y/2007-03-01T13:00:00Z"), equalTo(between(datetime("2006-03-01T13:00:00Z"), datetime("2007-03-01T13:00:00Z"))));
    }

    @Test
    public void duration() throws Throwable {
        assertThat(since(datetime("2007-03-01T13:00:00Z"), "P1Y"), equalTo(between(datetime("2007-03-01T13:00:00Z"), datetime("2008-03-01T13:00:00Z"))));
    }

    @Test
    void rejectsBadIntervalFormat() throws Throwable {
        Throwable expected = assertThrows(IllegalArgumentException.class, () -> parse("P1Y"));

        assertThat(expected, hasProperty("message", equalTo("Bad interval: \"P1Y\"")));
    }

    @Nested
    class IntervalFunctionsTest {

        @SuppressWarnings("ConstantConditions")
        <T, R> Function<T, Optional<R>> anyOf(Function<T, Optional<R>>... functions) {
            return it -> Interval.anyOf(functions).apply(it).get();
        }

        @Test
        public void composing() throws Throwable {
            Function<String, Optional<Integer>> composed = anyOf(it -> Optional.empty(), it -> Optional.of(2));

            assertThat(composed.apply("foo").get(), equalTo(2));
        }

        @Test
        public void date2() throws Throwable {
            assertThat(both().apply("2007-03-01T13:00:00Z/2008-05-11T15:30:00Z").get(), equalTo(between(datetime("2007-03-01T13:00:00"), datetime("2008-05-11T15:30:00"))));
            assertThat(both().apply("2007-03-01T13:00:00Z/P1Y2M10DT2H30M").orElse(null), is(nullValue()));
        }

        @Test
        public void start() throws Throwable {
            assertThat(starting().apply("2007-03-01T13:00:00Z/P1Y").get(), equalTo(between(datetime("2007-03-01T13:00:00Z"), datetime("2008-03-01T13:00:00Z"))));
        }

        @Test
        public void end() throws Throwable {
            assertThat(ending().apply("P1Y/2007-03-01T13:00:00Z").get(), equalTo(between(datetime("2006-03-01T13:00:00Z"), datetime("2007-03-01T13:00:00Z"))));
        }

        @Test
        public void duration() throws Throwable {
            //                                  |
            // it supports all of your formats in a single interface
            assertThat(since(datetime("2007-03-01T13:00:00Z")).apply("P1Y").get(), equalTo(between(datetime("2007-03-01T13:00:00Z"), datetime("2008-03-01T13:00:00Z"))));
        }
    }


    @Nested
    class LocalDateTimeParsingTest {
        @Test
        public void parseDateTimeFromString() throws Throwable {
            assertThat(datetime("2008-05-11T15:30:00Z"), equalTo(LocalDateTime.of(2008, 5, 11, 15, 30)));
        }
    }

    @Nested
    class TemporalAmountTest {

        @Test
        public void minus1() throws Throwable {
            assertThat(datetime("2007-03-01T13:00:00").minus(amount("P1Y")), equalTo(datetime("2006-03-01T13:00:00")));
        }

        @Test
        public void minus2() throws Throwable {
            assertThat(datetime("2007-03-01T13:00:00").minus(amount("P1YT1H")), equalTo(datetime("2006-03-01T12:00:00")));
        }

        @Test
        public void plus1() throws Throwable {
            assertThat(datetime("2007-03-01T13:00:00").plus(amount("P1Y")), equalTo(datetime("2008-03-01T13:00:00")));
        }

        @Test
        public void plus2() throws Throwable {
            assertThat(datetime("2007-03-01T13:00:00").plus(amount("P1YT1H")), equalTo(datetime("2008-03-01T14:00:00")));
        }

    }

    static /**/
    final class Interval<T extends LocalDateTime> {
        public final T start;

        public final T end;

        public static Interval<? extends LocalDateTime> parse(String interval) {
            //noinspection ConstantConditions,unchecked
            return possibly(anyOf(both(), starting(), ending()).apply(interval), interval).get();
        }

        public static Interval<? extends LocalDateTime> since(LocalDateTime start, String duration) {
            return possibly(since(start).apply(duration), duration);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static <T> T possibly(Optional<T> optional, String text) {
            return optional.orElseThrow(() -> new IllegalArgumentException(format("Bad interval: \"%s\"", text)));
        }

        private Interval(T start, T end) {
            this.start = start;
            this.end = end;
        }

        public static <T, R> Function<T, Optional<Optional<R>>> anyOf(Function<T, Optional<R>>... functions) {
            return it -> Stream.of(functions).map(current -> current.apply(it)).filter(Optional::isPresent).findFirst();
        }

        static TemporalAmount amount(String text) {
            return splitting("T", (first, second) -> new TemporalAmount() {
                private Period period = first.isEmpty() ? Period.ZERO : Period.parse(first);
                private Duration duration = second.isEmpty() ? Duration.ZERO
                        : Duration.parse(format("PT%s", second));

                @Override
                public long get(TemporalUnit unit) {
                    return (period.getUnits().contains(unit) ? period.get(unit) : 0) +
                            (duration.getUnits().contains(unit) ? duration.get(unit) : 0);
                }

                @Override
                public List<TemporalUnit> getUnits() {
                    return Stream.of(period, duration).map(TemporalAmount::getUnits)
                            .flatMap(List::stream)
                            .collect(toList());
                }

                @Override
                public Temporal addTo(Temporal temporal) {
                    return period.addTo(duration.addTo(temporal));
                }

                @Override
                public Temporal subtractFrom(Temporal temporal) {
                    return period.subtractFrom(duration.subtractFrom(temporal));
                }
            }).apply(text);
        }

        static LocalDateTime datetime(String datetime) {
            return LocalDateTime.parse(
                    datetime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss['Z']")
            );
        }

        static Function<String, Optional<Interval<LocalDateTime>>> both() {
            return parsing((first, second) -> between(
                    datetime(first),
                    datetime(second)
            ));
        }

        static Function<String, Optional<Interval<LocalDateTime>>> starting() {
            return parsing((first, second) -> {
                LocalDateTime start = datetime(first);
                return between(start, start.plus(amount(second)));
            });
        }

        static Function<String, Optional<Interval<LocalDateTime>>> ending() {
            return parsing((first, second) -> {
                LocalDateTime end = datetime(second);
                return between(end.minus(amount(first)), end);
            });
        }

        static Function<String, Optional<Interval<LocalDateTime>>>
        since(LocalDateTime start) {
            return parsing((amount, __) -> between(start, start.plus(amount(amount))));
        }

        static <R> Function<String, Optional<R>>
        parsing(BiFunction<String, String, R> parser) {
            return splitting("/", exceptionally(optional(parser), Optional::empty));
        }

        static <R> BiFunction<String, String, Optional<R>>
        optional(BiFunction<String, String, R> source) {
            return (first, last) -> Optional.of(source.apply(first, last));
        }

        static <T, U, R> BiFunction<T, U, R>
        exceptionally(BiFunction<T, U, R> source, Supplier<R> exceptional) {
            return (first, second) -> {
                try {
                    return source.apply(first, second);
                } catch (Exception ex) {
                    return exceptional.get();
                }
            };
        }

        static <R> Function<String, R> splitting(String regex, BiFunction<String, String, R> source) {
            return value -> {
                String[] parts = value.split(regex);
                return source.apply(parts[0], parts.length == 1 ? "" : parts[1]);
            };
        }

        public static <T extends LocalDateTime> Interval<T> between(T start, T end) {
            return new Interval<T>(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Interval)) {
                return false;
            }
            Interval<?> that = (Interval<?>) o;

            return Objects.equals(start, that.start) && Objects.equals(end, that.end);
        }


        @Override
        public int hashCode() {
            return Objects.hash(start) * 31 + Objects.hash(end);
        }

        @Override
        public String toString() {
            return format("[%s, %s]", start, end);
        }


    }

}
