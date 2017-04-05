package test.java.functions;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 3/31/17.
 */
public class EnclosingClassLambdaExpressionTest {
    private final Supplier<EnclosingClassLambdaExpressionTest> lambda = () -> this;
    private final EnclosingClassLambdaExpressionTest lambdaContext = this;
    private final Class<? extends EnclosingClassLambdaExpressionTest> lambdaContextClass = getClass();

    private Class<? extends Supplier> lambdaClass = lambda.getClass();

    @Test
    void createsASyntheticImplementationMethodInLambdaContextClass() throws Throwable {
        assertThat(implementationMethod().isSynthetic(), is(true));
        assertThat(implementationMethod().getReturnType(), equalTo(lambdaContextClass));
    }

    @Test
    void createsALambdaAdapterClassWhichHavingOnlyOneConstructorWithAParameterOfLambdaContextClass() throws Throwable {
        Object[] parameterTypes = Arrays.stream(lambdaClass.getDeclaredConstructors())
                .map(Constructor::getParameterTypes).toArray(Object[]::new);

        assertThat(parameterTypes, equalTo(new Object[][]{{lambdaContextClass}}));
    }

    @Test
    void createsALambdaAdapterClassWhichHavingAnInstanceFieldOfLambdaContextClass() throws Throwable {
        Object[] fieldTypes = Arrays.stream(lambdaClass.getDeclaredFields()).map(Field::getType).toArray(Object[]::new);

        assertThat(fieldTypes, equalTo(new Object[]{lambdaContextClass}));
    }

    @Test
    void invokeDynamicOnLambdaContextClassConstructor() throws Throwable {
        Supplier<EnclosingClassLambdaExpressionTest> reference = invokeDynamic();

        assertThat(reference.get(), equalTo(lambdaContext));
        assertThat(reference.getClass(), isALambdaClass(lambdaContextClass));
    }

    private Supplier<EnclosingClassLambdaExpressionTest> invokeDynamic() throws Throwable {
        MethodHandles.Lookup caller = MethodHandles.lookup();
        MethodType instantiatedMethodType = methodType(lambdaContextClass);
        // InvokeDynamic #0:get:(Lcom/holi/functions/EnclosingClass;)Ljava/util/function/Supplier;
        // BootstrapMethods:
        //   0: #23 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
        //     Method arguments:
        //       #24 ()Ljava/lang/Object;
        //       #25 invokespecial com/holi/functions/EnclosingClass.lambda$new$0:()Lcom/holi/functions/EnclosingClass;
        //       #26 ()Lcom/holi/functions/EnclosingClass;
        CallSite site = LambdaMetafactory.metafactory(caller,
                "get"
                , methodType(Supplier.class, lambdaContextClass)
                , methodType(Object.class)
                , caller.findVirtual(lambdaContextClass, "lambda$new$0", instantiatedMethodType)
                , instantiatedMethodType
        );
        return (Supplier<EnclosingClassLambdaExpressionTest>) site.dynamicInvoker().invokeExact(lambdaContext);
    }


    private Method implementationMethod() throws NoSuchMethodException {
        Method it = lambdaContextClass.getDeclaredMethod("lambda$new$0");
        it.setAccessible(true);
        return it;
    }

    private Matcher<Class> isALambdaClass(final Class<? extends EnclosingClassLambdaExpressionTest> enclosingClass) {
        return new FeatureMatcher<Class, String>(startsWith(format("%s$$Lambda$", enclosingClass.getName())), "class", "") {

            @Override
            protected String featureValueOf(Class actual) {
                return actual.getName();
            }
        };
    }
}
