package test.operator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ConstantConditions", "PointlessBooleanExpression"})
class BooleanBitwiseOperatorTest {

    @Test
    void alwaysTrue() throws Throwable {
        assertTrue(true | false);
        assertTrue(true | true);
        assertTrue(false | true);
    }

    @Test
    void falsely() throws Throwable {
        assertFalse(false | false);
    }

    @Test
    void donotShortCircuited() throws Throwable {
        BooleanSupplier expression = () -> true | failing();

        assertThrows(IllegalStateException.class, expression::getAsBoolean);
    }

    private boolean failing() {
        throw new IllegalStateException();
    }
}
