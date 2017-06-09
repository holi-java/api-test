package test.javax.script;

import org.junit.jupiter.api.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JSToJavaFunctionalInterfaceTest {

    @Test
    void functionTakesSimpleObject() throws Throwable {
        Predicate<String> empty = eval("function(s){return !s;}", Predicate.class);

        assertThat(empty.test(""), is(true));
        assertThat(empty.test(null), is(true));
        assertThat(empty.test("foo"), is(false));
    }


    @Test
    void functionExpressionTakesSimpleObject() throws Throwable {
        Predicate<String> empty = eval("function(s) !s", Predicate.class);

        assertThat(empty.test(""), is(true));
        assertThat(empty.test(null), is(true));
        assertThat(empty.test("foo"), is(false));
    }

    @Test
    void functionTakesArray() {
        Predicate<String[]> equal = eval("function(array){return array[0]==array[1];}", Predicate.class);

        assertThat(equal.test(arrayOf("foo", "foo")), is(true));
        assertThat(equal.test(arrayOf(null, null)), is(true));
        assertThat(equal.test(arrayOf("foo", "bar")), is(false));
    }


    @Test
    void functionWithList() {
        Predicate<List<String>> equal = eval("function(array){return array[0]==array[1]}", Predicate.class);

        assertThat(equal.test(asList("foo", "foo")), is(true));
        assertThat(equal.test(asList(null, null)), is(true));
        assertThat(equal.test(asList("foo", "bar")), is(false));
    }

    @SafeVarargs
    private final <T> T[] arrayOf(T... items) {
        return items;
    }

    private <T> T eval(String function, Class<T> type) {
        String newInstanceExpression = String.format("new %s(%s)", type.getName(), function);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            return type.cast(engine.eval(newInstanceExpression));
        } catch (Throwable e) {
            throw new IllegalArgumentException("constructor is bad:" + newInstanceExpression, e);
        }
    }
}
