package test.java.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.TimeZone;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_TIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/10/17.
 */
public class LegacyDateConversionTest {

    private Date date;

    @BeforeEach
    void setUp() throws Throwable {
        date = date("2014-01-02T03:04:05");
    }

    @Test
    void epochMillisToLocalDateTime() throws Throwable {
        assertThat(datetime(timestamp()).format(ISO_DATE_TIME), equalTo("2014-01-02T03:04:05"));
    }

    @Test
    void epochMillisToLocalDate() throws Throwable {
        assertThat(datetime(timestamp()).toLocalDate().format(ISO_DATE), equalTo("2014-01-02"));
    }

    @Test
    void epochMillisToLocalTime() throws Throwable {
        assertThat(datetime(timestamp()).toLocalTime().format(ISO_TIME), equalTo("03:04:05"));
    }

    @Test
    void dateToLocalDateTime() throws Throwable {
        assertThat(datetime(date()).format(ISO_DATE_TIME), equalTo("2014-01-02T03:04:05"));
    }

    private Date date() {
        return date;
    }

    @Test
    void dateToLocalDate() throws Throwable {
        assertThat(datetime(date()).toLocalDate().format(ISO_DATE), equalTo("2014-01-02"));
    }

    @Test
    void dateToLocalTime() throws Throwable {
        assertThat(datetime(date()).toLocalTime().format(ISO_TIME), equalTo("03:04:05"));
    }

    public long timestamp() {
        return date().getTime();
    }

    private Date date(String value) throws ParseException {
        return dateFormat().parse(value);
    }

    private DateFormat dateFormat() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone(gmt()));
        return formatter;
    }

    private LocalDateTime datetime(long epochMillis) {
        return datetime(Instant.ofEpochMilli(epochMillis));
    }

    private LocalDateTime datetime(Date date) {
        return datetime(date.toInstant());
    }

    private LocalDateTime datetime(Instant instant) {
        return LocalDateTime.ofInstant(instant, gmt());
    }

    private ZoneId gmt() {
        return ZoneId.of("GMT");
    }
}
