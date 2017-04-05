package test.java.functions;

import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static test.java.functions.InlineMethodReferenceExpressionTest.Customer.DEFAULT_CUSTOMER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/4/17.
 */
public class InlineMethodReferenceExpressionTest {

    @Test
    void directly() throws Throwable {
        Supplier<String> customerName = () -> new Customer().getName();

        assertThat(customerName.get(), equalTo(DEFAULT_CUSTOMER_NAME));
    }

    @Test
    void inline() throws Throwable {
        Supplier<String> customerName = ((CustomerSupplier) Customer::new)::getName;

        assertThat(customerName.get(), equalTo(DEFAULT_CUSTOMER_NAME));
    }

    @Test
    void chainThroughCustomType() throws Throwable {
        Supplier<String> name = Reference.of(Customer::new).of(Customer::getName);

        assertThat(name.get(), equalTo(DEFAULT_CUSTOMER_NAME));
    }

    @Test
    void chainThroughMethod() throws Throwable {
        Supplier<String> name = map(Customer::new, Customer::getName);

        assertThat(name.get(), equalTo(DEFAULT_CUSTOMER_NAME));
    }

    private <T, R> Supplier<R> map(Supplier<T> target, Function<T, R> mapper) {
        return () -> mapper.apply(target.get());
    }

    class Customer {
        static final String DEFAULT_CUSTOMER_NAME = "bob";

        private String name = DEFAULT_CUSTOMER_NAME;

        public String getName() {
            return name;
        }
    }

    interface CustomerSupplier {

        Customer get();

        default String getName() {
            return get().getName();
        }

    }

    interface Reference<T> extends Supplier<T> {

        static <T> Reference<T> of(Supplier<T> reference) {
            return reference::get;
        }

        default <R> Reference<R> of(Function<T, R> after) {
            return () -> after.apply(get());
        }

    }
}
