package test.java.lang;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by holi on 5/16/17.
 */
public class DefaultMethodsTest {

    @Test
    void intercepts() throws Throwable {
        AtomicBoolean intercepted = new AtomicBoolean(false);
        Foo it = proxy(Foo.class, () -> intercepted.set(true));

        it.bar();

        assertTrue(intercepted.get());
    }

    private <T> T proxy(Class<T> type, Runnable advice) {
        return type.cast(Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{type}, (proxy, method, args) -> {
            advice.run();
            return null;
        }));
    }


    private interface Foo {
        default void bar() {/**/}
    }
}
