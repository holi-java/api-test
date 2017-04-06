package test.java.lang;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/6/17.
 */
public class WorkedCommentsTest {

    @Test
    void commentBeExecutedAsCode() throws Throwable {
        assertThat(foo(), equalTo("bar"));
    }

    private String foo() {
        String foo = "foo";
        //\u000d foo = "bar";
        return foo;
    }
}
