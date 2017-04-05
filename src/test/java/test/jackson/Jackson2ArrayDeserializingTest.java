package test.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Objects;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 3/30/17.
 */
public class Jackson2ArrayDeserializingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    static class Country {
        public static Country valueOf(String country) {
            Country it = new Country();
            it.country = country;
            return it;
        }


        public String country;

        public boolean equals(Object o) {
            Country that = (Country) o;
            return Objects.equals(country, that.country);
        }

    }

    static class User {

        public Country[] countries;
    }

    @BeforeEach
    void setUp() throws Throwable {
        mapper.enable(ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(ALLOW_SINGLE_QUOTES);
    }

    @Test
    void parsingSingleValueAsArray() throws Throwable {
        String json = "{countries:'US'}";
        mapper.enable(ACCEPT_SINGLE_VALUE_AS_ARRAY);

        User user = mapper.readerFor(User.class).readValue(json);

        assertThat(user.countries, equalTo(as("US")));
    }

    @Test
    void parsingMultiValues() throws Throwable {
        String json = "{'countries':['US','EN']}";

        User user = mapper.readerFor(User.class).readValue(json);

        assertThat(user.countries, equalTo(as("US", "EN")));
    }

    private Country[] as(String... countries) {
        return Arrays.stream(countries).map(Country::valueOf).toArray(Country[]::new);
    }

}
