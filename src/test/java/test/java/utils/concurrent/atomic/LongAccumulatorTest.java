package test.java.utils.concurrent.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongBinaryOperator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/15/17.
 */
public class LongAccumulatorTest {

    @Test
    void startingValue() throws Throwable {
        long identity = 1;

        LongAccumulator it = new LongAccumulator(unused(), identity);

        assertThat(it.get(), equalTo(identity));
        assertThat(it.getThenReset(), equalTo(identity));
    }

    @Test
    void accumulate() throws Throwable {
        LongAccumulator it = new LongAccumulator((left, right) -> left - right, 2);

        it.accumulate(3);

        assertThat(it.get(), equalTo(-1L));
    }

    private LongBinaryOperator unused() {
        return (left, right) -> {
            throw new IllegalStateException();
        };
    }
}
