package test.java.functions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 3/30/17.
 */
public class FunctionCombinationTest {
    private List<Integer> staticBag = new ArrayList<>();
    private List<Integer> shuffledBag = new ArrayList<>();
    private List<Integer> randomAccessBag = new ArrayList<>();

    @Test
    void peek() throws Throwable {
        IntStream.rangeClosed(1, 2)
                .peek(staticBag::add)
                .peek(shuffledBag::add)
                .forEach(randomAccessBag::add);

        assertAllBagsEqualTo(asList(1, 2));
    }

    @Test
    void chain() throws Throwable {
        IntStream.rangeClosed(1, 2).forEach(accept(staticBag::add)
                                       /**/.andThen(shuffledBag::add)
                                       /**/.andThen(randomAccessBag::add));

        assertAllBagsEqualTo(asList(1, 2));
    }

    @Test
    void compose() throws Throwable {
        IntStream.rangeClosed(1, 2).forEach(accept(staticBag::add
                                             /**/, shuffledBag::add
                                             /**/, randomAccessBag::add));

        assertAllBagsEqualTo(asList(1, 2));
    }

    private IntConsumer accept(IntConsumer consumer) {
        return consumer;
    }

    private static final IntConsumer DO_NOTHING = (value) -> {
    };

    private IntConsumer accept(IntConsumer... consumers) {
        return Arrays.stream(consumers).reduce(IntConsumer::andThen).orElse(DO_NOTHING);
    }

    private void assertAllBagsEqualTo(List<Integer> expected) {
        assertThat(staticBag, equalTo(expected));
        assertThat(shuffledBag, equalTo(staticBag));
        assertThat(randomAccessBag, equalTo(staticBag));
    }
}
