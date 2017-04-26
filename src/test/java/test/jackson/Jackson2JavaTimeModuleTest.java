package test.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/26/17.
 */
public class Jackson2JavaTimeModuleTest {
    private static final String SERVER_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String CLIENT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final Supplier<ObjectMapper> objectMapperFactory = ObjectMapper::new;

    @Test
    void changesClientSideDateFormatPatternToFitServerSide() throws Throwable {
        String json = client(using(SERVER_DATE_TIME_FORMAT_PATTERN)).writeValueAsString(date("2017-04-26 18:29:33"));

        LocalDateTime it = server().readValue(json);

        assertThat(it.toString(), equalTo("2017-04-26T18:29:33"));
    }



    private ObjectWriter client(DateFormat dateFormat) {
        return objectMapperFactory.get().setDateFormat(dateFormat).writerFor(Date.class);
    }

    private ObjectReader server() {
        return objectMapperFactory.get().findAndRegisterModules()
                .readerFor(LocalDateTime.class);
    }

    private Date date(String date) throws ParseException {
        return using(CLIENT_DATE_FORMAT_PATTERN).parse(date);
    }

    private SimpleDateFormat using(String pattern) {
        return new SimpleDateFormat(pattern);
    }
}
