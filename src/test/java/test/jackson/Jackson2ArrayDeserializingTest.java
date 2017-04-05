package test.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.jackson.extension.JSONLooselyExtension;
import test.jackson.stubs.User;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static test.jackson.stubs.Country.arrayOf;

/**
 * Created by holi on 3/30/17.
 */
@ExtendWith(JSONLooselyExtension.class)
public class Jackson2ArrayDeserializingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsingSingleValueAsArray() throws Throwable {
        String json = "{countries:'US'}";
        mapper.enable(ACCEPT_SINGLE_VALUE_AS_ARRAY);

        User user = mapper.readerFor(User.class).readValue(json);

        assertThat(user.countries, equalTo(arrayOf("US")));
    }

    @Test
    void parsingMultiValues() throws Throwable {
        String json = "{'countries':['US','EN']}";

        User user = mapper.readerFor(User.class).readValue(json);

        assertThat(user.countries, equalTo(arrayOf("US", "EN")));
    }

}
