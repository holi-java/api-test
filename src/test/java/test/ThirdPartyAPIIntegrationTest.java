package test;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.holi.utils.CardinalMatchers.never;
import static com.holi.utils.CardinalMatchers.once;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 6/4/17.
 */
public class ThirdPartyAPIIntegrationTest {
    final IntArray array = new IntArray(1, 2, 3);

    @Test
    void donotMockObjectsThatCannotChange() throws Throwable {
        assertThat(array.sum(), equalTo(6));
    }

    @Test
    void mocksObjectsToBeNotifiedByThirdPartyApi() throws Throwable {
        AtomicInteger closes = new AtomicInteger();
        Runnable mockCloseAction = closes::incrementAndGet;
        array.onClose(mockCloseAction);

        array.sum();
        assertThat(closes, never());

        array.close();
        assertThat(closes, once());
    }

    private class IntArray {
        private IntStream stream;

        public IntArray(int... items) {
            stream = IntStream.of(items);
        }

        public int sum() {
            return stream.sum();
        }

        public void onClose(Runnable action) {
            stream.onClose(action);
        }

        public void close() {
            stream.close();
        }
    }
}
