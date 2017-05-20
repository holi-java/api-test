package test.java.lambda;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 3/30/17.
 */
public class LambdaComparisonTest {
    @Test
    void equality() throws Throwable {
        Runnable task1 = () -> {/**/};
        Runnable task2 = () -> {/**/};

        assertThat(task1, not(equalTo(task2)));
        assertThat(task1.getClass(), not(equalTo(task2.getClass())));
        assertThat(task1.getClass().isSynthetic(), is(true));
    }
}
