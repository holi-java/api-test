package test.java.utils;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.util.comparator.Comparators.nullsLow;

/**
 * Created by holi on 5/24/17.
 */
public class ComparingNullTest {


    @Test
    void comparingNullObjects() throws Throwable {
        final Comparator<Integer> it = nullsLow();

        assertThat(it.compare(null, anything()), equalTo(-1));
        assertThat(it.compare(anything(), null), equalTo(1));
        assertThat(it.compare(null, null), equalTo(0));
    }

    @Test
    void comparingNullKeys() throws Throwable {
        final Comparator<AtomicReference<Integer>> it = Comparator.comparing(AtomicReference::get, nullsLow());

        assertThat(it.compare(ref(null), ref(anything())), equalTo(-1));
        assertThat(it.compare(ref(anything()), ref(null)), equalTo(1));
        assertThat(it.compare(ref(null), ref(null)), equalTo(0));
    }


    private int anything() {
        return 1;
    }

    private AtomicReference<Integer> ref(Integer value) {
        return new AtomicReference<>(value);
    }
}
