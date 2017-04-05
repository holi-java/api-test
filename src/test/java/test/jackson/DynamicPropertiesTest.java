package test.jackson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/5/17.
 */
public class DynamicPropertiesTest {
    private final ObjectMapper jackson2 = new ObjectMapper();

    @Test
    void serializing() throws Throwable {
        Dynamic dynamic = new Dynamic();
        dynamic.foo = "bar";
        dynamic.set("fuzz", "buzz");

        String json = jackson2.writer().writeValueAsString(dynamic);

        JsonAssert.with(json)
                .assertEquals("foo", "bar")
                .assertNotDefined("properties")
                .assertEquals("fuzz", "buzz");
    }


    @Test
    void deserializing() throws Throwable {
        jackson2.enable(ALLOW_SINGLE_QUOTES, ALLOW_UNQUOTED_FIELD_NAMES);
        String json = "{foo:'bar', fuzz:'buzz'}";

        Dynamic dynamic = jackson2.readerFor(Dynamic.class).readValue(json);

        assertThat(dynamic.foo, equalTo("bar"));
        assertThat(dynamic.properties, equalTo(singletonMap("fuzz", "buzz")));
    }

    static class Dynamic {
        public String foo;

        private Map<String, Object> properties = new LinkedHashMap<>();

        @JsonAnySetter
        public void set(String key, Object value) {
            properties.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        public Object get(String key) {
            return properties.get(key);
        }
    }
}
