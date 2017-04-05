package test.java.utils;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/5/17.
 */
public class HashMapTest {
    class Key implements Comparable<Key> {
        private int i;

        public Key(int i) {

            this.i = i;
        }

        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(Key that) {
            return i - that.i;
        }
    }

    @Test
    void thereIsOnlyOneBucketAvailableIfAllKeysHashCodeAreIdentical() throws Throwable {
        HashMap<Key, String> map = new HashMap<>();

        for (int i = 0; i < 1000; i++)
            map.put(new Key(i), null);

        assertThat(map, bucketSize(1));
    }

    private Matcher<HashMap<?, ?>> bucketSize(long expectedSize) {
        return new FeatureMatcher<HashMap<?, ?>, Long>(equalTo(expectedSize), "bucket size is", "") {
            @Override
            protected Long featureValueOf(HashMap<?, ?> actual) {
                try {
                    return stream(table(actual)).filter(Objects::nonNull).count();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }

            private Object[] table(HashMap<?, ?> actual) throws NoSuchFieldException, IllegalAccessException {
                Field table = HashMap.class.getDeclaredField("table");
                table.setAccessible(true);
                return (Object[]) table.get(actual);
            }
        };
    }
}
