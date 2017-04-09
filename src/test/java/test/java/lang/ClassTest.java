package test.java.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Created by holi on 4/9/17.
 */
public class ClassTest {
    static boolean loaded = false;

    @Test
    void lazyLoad() throws Throwable {
        ClassLoader.getSystemClassLoader().loadClass("test.java.lang.ClassTest$Foo");

        assertFalse(loaded, "loaded");
    }

    public static class Foo {
        static {loaded = true;}
    }

}
