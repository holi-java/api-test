package test.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Consumer;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/26/17.
 */
public class Jackson2JavaTimeModuleTest {
    private static final String SERVER_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String CLIENT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Test
    void changesClientSideDateFormatPatternToFitServerSide() throws Throwable {
        String json = client(stringify(date("2017-04-26 18:29:33")), using(dateFormat(SERVER_DATE_TIME_FORMAT_PATTERN)));

        LocalDateTime it = server(parse(json, LocalDateTime.class), using(timeModule()));

        assertThat(it.toString(), equalTo("2017-04-26T18:29:33"));
    }

    @Test
    void changesServerSideLocalDateTimeFormatPatternToFitClientSide() throws Throwable {
        String json = client(stringify(date("2017-04-26 18:29:33")), using(dateFormat(CLIENT_DATE_FORMAT_PATTERN)));

        LocalDateTime it = server(parse(json, LocalDateTime.class), using(timeModule(ofPattern(CLIENT_DATE_FORMAT_PATTERN))));

        assertThat(it.toString(), equalTo("2017-04-26T18:29:33"));
    }

    private <R> R client(Processor<ObjectMapper, R> processor, Consumer<ObjectMapper> configurer) throws Throwable {
        return process(processor, configurer);
    }

    private <R> R server(Processor<ObjectMapper, R> processor, Consumer<ObjectMapper> configurer) throws Throwable {
        return process(processor, configurer);
    }

    private <R> R process(Processor<ObjectMapper, R> processor, Consumer<ObjectMapper> configurer) throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        configurer.accept(mapper);
        return processor.process(mapper);
    }

    private Processor<ObjectMapper, String> stringify(Date date) {
        return mapper -> mapper.writer().writeValueAsString(date);
    }

    private <R> Processor<ObjectMapper, R> parse(String json, Class<R> type) {
        return mapper -> mapper.readerFor(type).readValue(json);
    }

    private Consumer<ObjectMapper> using(DateFormat dateFormat) {
        return it -> it.setDateFormat(dateFormat);
    }

    private Consumer<ObjectMapper> using(Module module) {
        return it -> it.registerModule(module);
    }

    private JavaTimeModule timeModule(DateTimeFormatter formatter) {
        JavaTimeModule it = timeModule();
        it.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        return it;
    }

    private JavaTimeModule timeModule() {
        return new JavaTimeModule();
    }

    private Date date(String date) throws ParseException {
        return dateFormat(CLIENT_DATE_FORMAT_PATTERN).parse(date);
    }

    private SimpleDateFormat dateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    interface Processor<T, R> {
        R process(T value) throws Throwable;
    }
}
