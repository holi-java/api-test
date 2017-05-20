package test.java.lambda;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Created by holi on 5/3/17.
 */
public class LambdaCreationTest {

    private final Set<Runnable> lambdas = new HashSet<>();

    @Test
    void createsLambdaInstanceOnceInLoop() throws Throwable {
        for (int i = 0; i < 2; i++) {
            lambdas.add(() -> {/**/});
        }

        assertThat(lambdas, hasSize(1));
    }

    @Test
    void createsLambdaInstanceOnceInMethod() throws Throwable {
        lambdas.add(lambda());
        lambdas.add(lambda());

        assertThat(lambdas, hasSize(1));
    }

    @Test
    void createsLambdaInstanceOnceWhichDeclaredOnFieldThatNotReferenceNonStaticMembers() throws Throwable {
        lambdas.add(new Lambdas().statically);
        lambdas.add(new Lambdas().statically);

        assertThat(lambdas, hasSize(1));
    }


    @Test
    void createsMultiLambdaInstancesWhichDeclaredOnFieldThatReferenceNonStaticMembers() throws Throwable {
        lambdas.add(new Lambdas().instance);
        lambdas.add(new Lambdas().instance);

        assertThat(lambdas, hasSize(2));
    }

    @Test
    void createsLambdaInstancePerLambdaExpressionEvenIfTheyAreSame() throws Throwable {
        lambdas.add(() -> {/**/});
        lambdas.add(() -> {/**/});

        assertThat(lambdas, hasSize(2));
    }

    private Runnable lambda() {
        return () -> {/**/};
    }

    class Lambdas {

        public Runnable statically = () -> {/**/};
        public Runnable instance = this::toString;
    }
}