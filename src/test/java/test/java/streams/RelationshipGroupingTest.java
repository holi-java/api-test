package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.*;

import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/6/17.
 */
public class RelationshipGroupingTest {
    class Item {
    }

    class Result {
        String name;
        Set<Item> items;
    }

    @Test
    void grouping() throws Throwable {
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        List<Result> results = asList(result("name1", item1, item2), result("name2", item2, item3));


        Map<Item, Set<String>> it = relationships(results);

        assertThat(it.size(), equalTo(3));
        assertThat(it.get(item1), equalTo(asSet("name1")));
        assertThat(it.get(item2), equalTo(asSet("name1", "name2")));
        assertThat(it.get(item3), equalTo(asSet("name2")));
    }

    private Map<Item, Set<String>> relationships(List<Result> results) {
        return results.stream()
                //map Stream<Result> to Stream<Entry<Item>,String>
                .flatMap(it -> it.items.stream().map(item -> new SimpleEntry<>(item, it.name)))
                //group Result.name by Item
                .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toSet())));
    }

    private <T> Set<T> asSet(T... items) {
        return new HashSet<>(asList(items));
    }

    private Result result(String name, Item... items) {
        Result it = new Result();
        it.name = name;
        it.items = new HashSet<>(asList(items));
        return it;
    }
}
