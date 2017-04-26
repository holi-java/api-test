package test.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        String json = client(stringify(date("2017-04-26 18:29:33")), using(SERVER_DATE_TIME_FORMAT_PATTERN));

        LocalDateTime it = server(parse(json, LocalDateTime.class), using(SERVER_DATE_TIME_FORMAT_PATTERN));

        assertThat(it.toString(), equalTo("2017-04-26T18:29:33"));
    }

    @Test
    void changesServerSideLocalDateTimeFormatPatternToFitClientSide() throws Throwable {
        String json = client(stringify(date("2017-04-26 18:29:33")), using(CLIENT_DATE_FORMAT_PATTERN));

        LocalDateTime it = server(parse(json, LocalDateTime.class), using(CLIENT_DATE_FORMAT_PATTERN));

        assertThat(it.toString(), equalTo("2017-04-26T18:29:33"));
    }

    private <R> R client(Processor<ObjectMapper, R> processor, SimpleDateFormat dateFormat) throws Throwable {
        return processor.process(clientObjectMapper(dateFormat));
    }

    private Processor<ObjectMapper, String> stringify(Date date) {
        return mapper -> mapper.writer().writeValueAsString(date);
    }

    private <R> R server(Processor<ObjectMapper, R> processor, SimpleDateFormat dateFormat) throws Throwable {
        return processor.process(serverObjectMapper(dateFormat));
    }

    private <R> Processor<ObjectMapper, R> parse(String json, Class<R> type) {
        return mapper -> mapper.readerFor(type).readValue(json);
    }

    private ObjectMapper clientObjectMapper(DateFormat dateFormat) {
        return objectMapperFactory.get().setDateFormat(dateFormat);
    }


    private ObjectMapper serverObjectMapper(SimpleDateFormat dateFormat) {
        return objectMapperFactory.get().registerModule(javaTimeModule(dateFormat));
    }

    private SimpleModule javaTimeModule(SimpleDateFormat dateFormat) {
        return new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(mapping(dateFormat)));
    }

    private DateTimeFormatter mapping(SimpleDateFormat dateFormat) {
        return DateTimeFormatter.ofPattern(dateFormat.toPattern());
    }

    private Date date(String date) throws ParseException {
        return using(CLIENT_DATE_FORMAT_PATTERN).parse(date);
    }

    private SimpleDateFormat using(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    interface Processor<T, R> {
        R process(T value) throws Throwable;
    }
}
