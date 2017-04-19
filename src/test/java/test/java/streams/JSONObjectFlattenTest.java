package test.java.streams;

import net.sf.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by holi on 4/19/17.
 */
public class JSONObjectFlattenTest {

    @Test
    void flatten() throws Throwable {
        final String json =
        /**/"[" +
        /**/"    {" +
        /**/"        ID: 12," +
        /**/"        NAME: 'Whatever'," +
        /**/"        X: [1,2, {Y:[3,4]}]" +
        /**/"    }," +
        /**/"    {" +
        /**/"        ID: 34," +
        /**/"        NAME: 'Again'," +
        /**/"        X: [23,43]" +
        /**/"    }" +
        /**/"]";

        String expected =
        /**/"[" +
        /**/"    [12,'Whatever',1,2,3,4]," +
        /**/"    [34,'Again',23,43]," +
        /**/"]";
        JSONArray array = JSONArray.fromObject(json);

        JSONArray flatted = flatten(array);

        assertThat(flatted, equalTo(JSONArray.fromObject(expected)));
    }

    public JSONArray flatten(Collection<?> it) {
        return it.stream().map(this::flatten).collect(toJSONArray());
    }

    public JSONArray flatten(Map<?, ?> it) {
        return flat(it).collect(toJSONArray());
    }

    public Object flatten(Object it) {
        if (it instanceof Collection) {
            return flatten((Collection) it);
        }
        if (it instanceof Map) {
            return flatten((Map) it);
        }
        return it;
    }

    private Stream<Object> flat(Map<?, ?> it) {
        return it.values().stream().flatMap(this::flat);
    }

    private Stream<Object> flat(Collection<?> array) {
        return array.stream().flatMap(this::flat);
    }

    private Stream<?> flat(Object it) {
        if (it instanceof Collection) {
            return flat((Collection) it);
        }
        if (it instanceof Map) {
            return flat((Map) it);
        }
        return Stream.of(it);
    }

    private <T> Collector<T, ?, JSONArray> toJSONArray() {
        return toCollection(JSONArray::new);
    }
}
