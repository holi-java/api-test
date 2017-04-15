package test.java.time;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Created by holi on 4/12/17.
 */
public class TimeZoneTest {

    @Test
    void standardTimeZone() throws Throwable {
        assertThat(epoch("GMT"), equalTo("1970-01-01 00:00:00"));
    }

    @Test
    void localTimeZone() throws Throwable {
        assertThat(epoch("Asia/Shanghai"), not(equalTo("1970-01-01 00:00:00")));
    }


    private String epoch(String zoneId) {
        return dateFormat(TimeZone.getTimeZone(zoneId)).format(new Date(0));
    }

    private DateFormat dateFormat(TimeZone zone) {
        DateFormat it = dateFormat();
        it.setTimeZone(zone);
        return it;
    }

    private DateFormat dateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
