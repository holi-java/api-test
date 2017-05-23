package test.java.basic;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 5/23/17.
 */
public class InterfaceMethodsTest {
    interface Foo<T> {
        void any(Object value);

        void typed(T value);
    }

    interface Bar extends Foo<String> {
        void any(Object value);

        void typed(String value);
    }

    @Test
    void redefinedMethodsInInterfaceAreNotBridgeAndSyntheticMethods() throws Throwable {
        Method it = Bar.class.getDeclaredMethod("any", Object.class);

        assertThat(it, not(isSynthentic()));
        assertThat(it, not(isBridge()));
    }


    @Test
    void redefinedGenericMethodsInInterfaceAreBridgeAndSyntheticMethods() throws Throwable {
        Method it = Bar.class.getDeclaredMethod("typed", Object.class);

        assertThat(it, isSynthentic());
        assertThat(it, isBridge());
    }

    private Matcher<Method> isBridge() {
        return hasProperty("bridge", is(true));
    }

    private Matcher<Method> isSynthentic() {
        return hasProperty("synthetic", is(true));
    }
}
