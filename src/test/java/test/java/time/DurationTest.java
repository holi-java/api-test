package test.java.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 6/1/17.
 */
public class DurationTest {


    @Test
    void parsesDurationFromLiteral() throws Throwable {

        Duration duration = DateTimeFormatter.ofPattern("0DDDHHmmss").parse("0500231109").query(this::toDuration);

        assertThat(duration, equalTo(Duration.ofDays(500).plusHours(23).plusMinutes(11).plusSeconds(9)));
    }

    private Duration toDuration(TemporalAccessor temporal) {
        return Stream.of(DAY_OF_YEAR, HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE).
                reduce(Duration.ZERO, reducing(temporal), Duration::plus);
    }

    private BiFunction<Duration, ChronoField, Duration> reducing(TemporalAccessor temporal) {
        return (it, field) -> it.plus(field.getFrom(temporal), field.getBaseUnit());
    }

}
