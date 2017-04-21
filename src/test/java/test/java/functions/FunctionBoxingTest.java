package test.java.functions;

import org.junit.jupiter.api.Test;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/21/17.
 */
public class FunctionBoxingTest {
    @Test
    void autoboxing() throws Throwable {
        Integer number = 1;

        IntSupplier nature = number::intValue;
        Supplier<Integer> boxed = number::intValue;

        assertThat(nature.getAsInt(), equalTo(1));
        assertThat(boxed.get(), equalTo(1));
    }
}
