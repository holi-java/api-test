package test;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 5/24/17.
 */
public class BehaviorTest {
    final Stack stack = new Stack(2);

    @Test
    void pushesElementIntoStack() throws Throwable {
        assertThat(stack, is(empty()));

        stack.push(1);

        assertThat(stack, is(not(empty())));
    }

    @Test
    void popsTopElementInStack() throws Throwable {
        stack.push(1);
        stack.push(2);

        Integer top1 = stack.pop();
        Integer top2 = stack.pop();

        assertThat(top1, equalTo(2));
        assertThat(top2, equalTo(1));
        assertThat(stack, is(empty()));
    }

    @Test
    void reportsNoSuchElementExceptionWhenPoppingTopElementFromEmptyStack() throws Throwable {
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @Test
    void throwsStackOverflowExceptionWhenPushingElementIntoAStackThatHasNoEnoughCapacity() throws Throwable {
        Stack stack = new Stack(0);

        assertThrows(StackOverflowException.class, () -> stack.push(1));
    }

    private <T> Matcher<T> empty() {
        return hasProperty("empty", is(true));
    }

    public class Stack {
        private int size;
        private Integer[] elements;

        public Stack(int capacity) {
            elements = new Integer[capacity];
        }

        public void push(int element) {
            throwExceptionIf(isFully(), StackOverflowException::new);
            elements[size++] = element;
        }

        private boolean isFully() {
            return size == elements.length;
        }

        public Integer pop() {
            throwExceptionIf(isEmpty(), NoSuchElementException::new);

            int top = --size;
            Integer element = elements[top];
            elements[top] = null;
            return element;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        private <T extends Exception> void throwExceptionIf(boolean condition, Supplier<T> exception) throws T {
            if (condition) {
                throw exception.get();
            }
        }
    }

    private class StackOverflowException extends IllegalStateException {
    }
}
