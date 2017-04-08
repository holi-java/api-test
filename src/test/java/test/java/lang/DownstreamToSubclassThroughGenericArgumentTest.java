package test.java.lang;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.IntFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/8/17.
 */
public class DownstreamToSubclassThroughGenericArgumentTest {
    /**
     * @param <E> which is to append
     * @param <T> which is derived from Appendable&lt;E,T&gt;
     */
    interface Appendable<E, T extends Appendable<E, T>> {
        /**
         * @param item
         * @return subtype of Appendable&lt;E,T&gt;
         */
        T append(E item);
    }

    class Array<E> implements Appendable<E, Array<E>> {
        private java.util.List<E> list = new ArrayList<>();

        @Override
        public Array<E> append(E item) {
            list.add(item);
            return this;
        }

        public E[] toArray(IntFunction<E[]> generator) {
            return list.toArray(generator.apply(list.size()));
        }
    }

    @Test
    void downstream() throws Throwable {
        String[] results = strings().append("foo").append("bar").toArray(String[]::new);

        assertThat(results, equalTo(new String[]{"foo", "bar"}));
    }

    private Appendable<String, Array<String>> strings() {
        return new Array<>();
    }
}
