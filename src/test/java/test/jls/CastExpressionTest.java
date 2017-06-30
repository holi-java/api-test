package test.jls;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CastExpressionTest {

    @Test
    void castExceptionOccursInRuntimeIfTheReferenceTypeIsACastingType() {
        Object item = 1;

        //noinspection Convert2MethodRef,ResultOfMethodCallIgnored
        assertThrows(ClassCastException.class, () -> ((String) item).isEmpty());
    }


    @Test
    void reportsErrorCastExceptionInCompileTimeIfTheReferenceTypeIsNotACastingType() {
        //noinspection unused
        Integer item = 1;

        //String value = (String) item;
    }

    @Test
    void castExpressionReferenceTypeFollowedByAdditionalBoundsMustBePairwiseDifferent() throws Throwable {
        /*additional bounds pairwise with same reference type*/
        //Supplier<String> failed= (Supplier<String> & Supplier<String>) () -> "foo";

        Supplier<String> serialized = (Serializable & Supplier<String>) () -> "foo";

        assertThat(serialized, instanceOf(Supplier.class));
        assertThat(serialized, instanceOf(Serializable.class));
        assertThat(serialized.get(), equalTo("foo"));
    }

    @Test
    @SuppressWarnings("UnnecessaryLocalVariable")
    void arrayCastExpression() throws Throwable {
        Integer[] integers = {1, 2, 3};

        Number[] numbers = integers;
        Object[] objects = numbers;

        Integer[] reverted = (Integer[]) objects;

        assertThat(reverted, sameInstance(integers));
    }
}
