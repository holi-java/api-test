package test.java.streams;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/19/17.
 */
public class JSONObjectFlattenTest {

    @Test
    void example() throws Throwable {
        JSONArray array = JSONArray.fromObject(
            /**/"[  " +
            /**/"  {  " +
            /**/"     obj1  : [  " +
            /**/"          {  " +
            /**/"              ID  : 12,  " +
            /**/"              NAME  :  'Whatever'  ,  " +
            /**/"              XY  :[1,2]  " +
            /**/"          },  " +
            /**/"          {  " +
            /**/"              ID  : 34,  " +
            /**/"              NAME  :  'Again'  ,  " +
            /**/"              XY  :[23,43]  " +
            /**/"          }  " +
            /**/"    ]  " +
            /**/"  }  " +
            /**/"]"
        );
        JSONArray result = JSONArray.fromObject(
            /**/"[  " +
            /**/"  {  " +
            /**/"     obj1  : [  " +
            /**/"          [12,'Whatever',1,2],  " +
            /**/"          [34,'Again',23,43]  " +
            /**/"    ]  " +
            /**/"  }  " +
            /**/"]"
        );

        assertThat(flatten(array), equalTo(result));
    }

    @Test
    void arrayContainingNumber() throws Throwable {
        JSONArray array = JSONArray.fromObject("[1]");
        assertThat(flatten(array), equalTo(array));
    }

    @Test
    void arrayContainingNullValue() throws Throwable {
        JSONArray array = JSONArray.fromObject("[null]");
        assertThat(flatten(array), equalTo(array));
    }

    @Test
    void arrayNestedWithAnotherArray() throws Throwable {
        JSONArray array = JSONArray.fromObject("[[1]]");
        JSONArray expected = JSONArray.fromObject("[1]");
        assertThat(flatten(array), equalTo(expected));
    }


    @Test
    void objectContainingStringValue() throws Throwable {
        JSONObject json = JSONObject.fromObject("{foo:'bar'}");

        assertThat(flatten(json), equalTo(json));
    }

    @Test
    void objectContainingNullValue() throws Throwable {
        JSONObject json = JSONObject.fromObject("{foo:null}");

        assertThat(flatten(json), equalTo(json));
    }

    @Test
    void objectNestedWithAnotherObject() throws Throwable {
        JSONObject json = JSONObject.fromObject("{item:{foo:'bar'}}");
        JSONObject expected = JSONObject.fromObject("{item:['bar']}");

        assertThat(flatten(json), equalTo(expected));
    }

    @Test
    void objectNestedWithAnotherObjectContainingNullValue() throws Throwable {
        JSONObject json = JSONObject.fromObject("{item:{foo:null}}");
        JSONObject expected = JSONObject.fromObject("{item:[null]}");

        assertThat(flatten(json), equalTo(expected));
    }

    @Test
    void objectNestedWithJSONArray() throws Throwable {
        JSONObject json = JSONObject.fromObject("{items:[{foo:'bar'}]}");
        JSONObject expected = JSONObject.fromObject("{items:[['bar']]}");

        assertThat(flatten(json), equalTo(expected));
    }

    @Test
    void arrayNestedWithJSONObjectContainingStringProperty() throws Throwable {
        JSONArray array = JSONArray.fromObject("[{foo:'bar'}]");

        assertThat(flatten(array), equalTo(array));
    }

    @Test
    void arrayNestedWithJSONObjectTakesAnotherJSONObject() throws Throwable {
        JSONArray array = JSONArray.fromObject("[{item:{foo:'bar'}}]");
        JSONArray expected = JSONArray.fromObject("[{item:['bar']}]");

        assertThat(flatten(array), equalTo(expected));
    }

    public JSONArray flatten(Collection<?> array) {
        return array.stream().flatMap(this::flatting).collect(toJSONArray());
    }

    private Stream<?> flatting(Object it) {
        if (it instanceof Collection) {
            return ((Collection<?>) it).stream();
        }
        if (it instanceof Map) {
            return Stream.of(flatten((Map<?, ?>) it));
        }
        return Stream.of(it);
    }

    public JSONObject flatten(Map<?, ?> map) {
        return map.entrySet().stream().collect(
                JSONObject::new,
                (it, field) -> it.put(field.getKey(), flatten(field.getValue())),
                JSONObject::putAll
        );
    }

    private Object flatten(Object it) {
        if (it instanceof Collection) {
            return ((Collection<?>) it).stream().map(this::flatten)
                                                .collect(toJSONArray());
        }
        if (it instanceof Map) {
            return flatten(((Map<?, ?>) it).values());
        }
        return it;
    }

    private <T> Collector<T, ?, JSONArray> toJSONArray() {
        return toCollection(JSONArray::new);
    }
}
