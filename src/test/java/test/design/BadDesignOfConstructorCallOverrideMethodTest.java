package test.design;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Created by holi on 5/11/17.
 */
public class BadDesignOfConstructorCallOverrideMethodTest {
    @Test
    void callingOverrideMethodsInSuperConstructorWillResultToWrongResult() throws Throwable {
        class Bar extends Foo {
            Bar() {
                this.value = "baz";
            }

            @Override
            public void init() {
                this.value = "bar";
            }
        }
        Foo foo = new Bar();

        assertThat(foo.get(), not(equalTo("bar")));

        foo.init();

        assertThat(foo.get(), equalTo("bar"));
    }

    static class Foo {
        protected Object value;

        public Foo() {
            init();
        }

        public Object get() {
            return value;
        }

        public void init() {

        }
    }
}
