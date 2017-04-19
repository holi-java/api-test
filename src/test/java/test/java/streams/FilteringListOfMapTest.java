package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/7/17.
 */
public class FilteringListOfMapTest {

    @Test
    void linear() throws Throwable {
        assertFiltering(it -> it.stream().map(filtering(key("x").or(key("z")))).collect(toList()));
    }

    private Function<Map<String, String>, Map<String, String>> filtering(Predicate<Entry<String, String>> condition) {
        return map -> map.entrySet().stream().filter(condition).collect(toMap(Entry::getKey, Entry::getValue));
    }

    @Test
    void intersection() throws Throwable {
        assertFiltering(it -> it.stream().map(intersection("x", "z")).collect(toList()));
    }

    private Function<Map<String, String>, Map<String, String>> intersection(String... keys) {
        return map -> Stream.of(keys).distinct().filter(map::containsKey).collect(toMap(identity(), map::get));
    }

    private void assertFiltering(Function<List<Map<String, String>>, List<Map<String, String>>> filter) {
        Map<String, String> primary = map("x", "123", "y", "456", "z", "789");
        Map<String, String> secondary = map("x", "000", "y", "111", "z", "222");
        List<Map<String, String>> groups = asList(primary, secondary);
        assertThat(groups, equalTo(asList(primary, secondary)));

        List<Map<String, String>> filtered = filter.apply(groups);


        assertThat(filtered, equalTo(asList(
                map("x", "123", "z", "789"),
                map("x", "000", "z", "222")
        )));
    }

    private Predicate<Entry<String, String>> key(String key) {
        return it -> Objects.equals(it.getKey(), key);
    }


    private Map<String, String> map(String... pairs) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; ) {
            map.put(pairs[i++], pairs[i++]);
        }
        return map;
    }
}
