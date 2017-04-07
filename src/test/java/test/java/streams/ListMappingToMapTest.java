package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/7/17.
 */
public class ListMappingToMapTest {

    @Test
    void mapping() throws Throwable {
        Item item1 = item(1);
        Item item2 = item(2);
        Item item3 = item(3);
        List<Item> items = Arrays.asList(item1, item2, item3);

        assertThat(foreachMapping(items), equalTo(map(1, item1, 3, item2, 6, item3)));
        assertThat(streamMapping(items), equalTo(foreachMapping(items)));
        assertThat(streamMapping2(items), equalTo(foreachMapping(items)));
    }

    private <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        HashMap<K, V> it = new HashMap<>();
        it.put(key1, value1);
        it.put(key2, value2);
        it.put(key3, value3);
        return it;
    }

    private Item item(int key) {
        return new Item(key);
    }

    public Map<Integer, Item> foreachMapping(List<Item> list) {
        Map<Integer, Item> map = new HashMap<>();
        int prevKey = 0;
        for (Item item : list.stream().filter(it -> it.getKey() != 0).collect(toList())) {
            map.put(prevKey += Math.abs(item.getKey()), item);
        }
        return map;
    }

    public Map<Integer, Item> streamMapping(List<Item> list) {
        AtomicInteger sum = new AtomicInteger(0);
        return list.stream().filter(it -> it.getKey() != 0)
                .collect(Collectors.toMap(curry(sum::addAndGet, Item::getKey), identity()));
    }

    private <T, U, R> Function<T, R> curry(Function<U, R> target, Function<T, U> mapper) {
        return (it) -> target.apply(mapper.apply(it));
    }

    public Map<Integer, Item> streamMapping2(List<Item> list) {
        return list.stream().filter(it -> it.getKey() != 0)
                .collect(Stack::new, this::calculateKey, List::addAll).stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private void calculateKey(Stack<Entry<Integer, Item>> stack, Item item) {
        Integer prevKey = stack.isEmpty() ? 0 : stack.peek().getKey();

        Integer key = prevKey + item.getKey();

        stack.push(new SimpleEntry<>(key, item));
    }

    class Item {
        private int key;

        public Item(int key) {
            this.key = key;
        }

        int getKey() {
            return key;
        }

        @Override
        public String toString() {
            return format("<%d>", key);
        }
    }
}
