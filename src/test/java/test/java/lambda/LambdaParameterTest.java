package test.java.lambda;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 3/31/17.
 */
public class LambdaParameterTest {

    @Test
    void cannotFetchActualArgumentType() throws Throwable {
        Type type = consumer().getClass()
                .getDeclaredMethod("accept", Object.class)
                .getGenericParameterTypes()[0];

        assertThat(type, is(not(instanceOf(ParameterizedType.class))));
    }

    @Test
    void throwsClassCastExceptionWhenForcingRunLambdaWithArgumentOfOtherType() throws Throwable {
        //todo: different <? super T> between <? extends T> and <?>
        Consumer consumer = consumer();

        assertThrows(ClassCastException.class, () -> consumer.accept(1));
        assertThat(LambdaParameterTest.class.getDeclaredClasses(), is(emptyArray()));
    }

    private static Consumer<String> consumer() {
        return (string) -> {/**/};
    }

}
