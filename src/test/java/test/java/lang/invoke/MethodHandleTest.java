package test.java.lang.invoke;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 4/7/17.
 */
public class MethodHandleTest {

    private final MethodHandle substring;

    public MethodHandleTest() throws Throwable {
        MethodType methodType = methodType(String.class, int.class, int.class);
        substring = lookup().findVirtual(String.class, "substring", methodType);
    }

    @Test
    void invokeExact() throws Throwable {
        String result = (String) substring.invokeExact("foo", 0, 2);
        assertThat(result, equalTo("fo"));
    }

    @Test
    void throwsExceptionWhenInvokeExactAssignToOtherType() throws Throwable {
        WrongMethodTypeException error = assertThrows(WrongMethodTypeException.class, () -> {
            Object result = substring.invokeExact("foo", 0, 2);
        });

        assertThat(error, hasProperty("message", containsString("(String,int,int)Object")));
    }

    @Test
    void invoke() throws Throwable {
        Object result = substring.invoke("foo", 0, 2);

        assertThat(result, equalTo("fo"));
    }

    @Test
    void binding() throws Throwable {
        MethodHandle binding = substring.bindTo("foo");

        assertThat(binding, is(not(sameInstance(substring))));
        assertThat(binding.invoke(0, 2), equalTo("fo"));
    }

    @Test
    void asType() throws Throwable {
        MethodHandle adapter = substring.asType(substring.type().changeReturnType(Object.class));

        Object result = adapter.invokeExact("foo", 0, 2);

        assertThat(adapter, is(not(sameInstance(substring))));
        assertThat(result, equalTo("fo"));
    }

    @Test
    void asFixedArity() throws Throwable {
        MethodHandle toList = lookup().findStatic(Arrays.class, "asList", methodType(List.class, Object[].class));
        MethodHandle toList2 = toList.asFixedArity();

        assertThat(toList2, is(not(sameInstance(toList))));
        assertThat(substring.asFixedArity(), is(sameInstance(substring)));

        assertThat(toList.invoke(1, 2), equalTo(asList(1, 2)));
        assertThrows(ClassCastException.class, () -> toList2.invoke((Object) 1));
        assertThrows(WrongMethodTypeException.class, () -> toList2.invoke(1, 2));

        assertThat(toList.invoke(new Object[]{1, 2}), equalTo(asList(1, 2)));
        assertThat(toList2.invoke(new Object[]{1, 2}), equalTo(asList(1, 2)));
    }
}
