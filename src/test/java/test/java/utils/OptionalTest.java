package test.java.utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 6/2/17.
 */
public class OptionalTest {
    @Test
    void emptyEqualsNull() throws Throwable {
        assertThat(Optional.empty(), equalTo(Optional.ofNullable(null)));
    }
}
