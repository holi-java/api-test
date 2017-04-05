package test.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.jackson.stubs.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static org.hamcrest.Matchers.*;
import static test.jackson.stubs.Country.arrayOf;

/**
 * Created by holi on 3/30/17.
 */
public class Jackson2ArraySerializingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Throwable {
        mapper.enable(ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(ALLOW_SINGLE_QUOTES);
    }

    @Test
    void treatArrayContainingSingleItemAsObjectNorArray() throws Throwable {
        mapper.enable(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        User bob = new User(arrayOf("US"));

        String json = mapper.writeValueAsString(bob);

        JsonAssert.with(json)
                .assertThat("countries", is(not(instanceOf(List.class))))
                .assertThat("countries", is(instanceOf(Map.class)))
                .assertThat("countries", equalTo(Collections.singletonMap("country", "US")));
    }


}
