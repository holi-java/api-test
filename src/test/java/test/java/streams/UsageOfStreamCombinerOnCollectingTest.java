package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by holi on 5/12/17.
 */
public class UsageOfStreamCombinerOnCollectingTest {
    @Test
    public void didNotUseCombinerInSequentialStream() {
        assertUsageOfCombinerOnCollecting(IntStream.range(0, 2), false);
    }

    @Test
    public void usesCombinerInParallelStreamIfStreamContainsMoreThanOneElement() {
        assertUsageOfCombinerOnCollecting(IntStream.range(0, 2).parallel(), true);
    }

    @Test
    public void didNotUseCombinerInParallelStreamIfStreamContainsOnlyOneElement() {
        assertUsageOfCombinerOnCollecting(IntStream.of(1).parallel(), false);
    }

    private void assertUsageOfCombinerOnCollecting(IntStream it, boolean used) {
        AtomicBoolean usage = new AtomicBoolean(false);

        it.collect(ArrayList::new, List::add, (left, right) -> usage.set(true));

        assertThat(usage.get(), is(used));
    }
}
