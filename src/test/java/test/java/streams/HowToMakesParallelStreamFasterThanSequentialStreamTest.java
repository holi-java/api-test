package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

/**
 * Created by holi on 6/1/17.
 */
public class HowToMakesParallelStreamFasterThanSequentialStreamTest {

    @Test
    void parallelStreamIsFasterThanSequentialStreamWhenTheirOperationIsTimeConsuming() throws Throwable {
        assertThat(timing(timeConsumingStream().parallel()::count), is(lessThan(timing(timeConsumingStream()::count))));
    }

    private Stream<Object> timeConsumingStream() {
        return Stream.of(50, 100).map(this::pause);
    }

    private long timing(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - start;
    }

    private Object pause(Integer mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
