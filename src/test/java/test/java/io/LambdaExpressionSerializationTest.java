package test.java.io;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class LambdaExpressionSerializationTest implements Serializable {
    private final AtomicInteger number = new AtomicInteger(1);

    @Test
    void serializeVariableOutOfItsScope() throws Throwable {
        Supplier<Integer> source = (Supplier<Integer> & Serializable) number::get;
        Supplier<Integer> serialized = serialized(source);

        number.set(2);

        assertThat(source.get(), equalTo(2));
        assertThat(serialized.get(), equalTo(1));
    }


    @Test
    @SuppressWarnings("ConstantConditions")
    void donNotSerializeItsReturnType() throws Throwable {
        Supplier<Optional<Integer>> source = (Supplier<Optional<Integer>> & Serializable) () -> Optional.of(number.get());
        Supplier<Optional<Integer>> serialized = serialized(source);

        number.set(2);

        assertThat(source.get().get(), equalTo(2));
        assertThat(serialized.get().get(), equalTo(1));
    }

    private <T> T serialized(T source) throws IOException, ClassNotFoundException {
        return deserializing(serializing(source));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializing(InputStream in) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(in).readObject();
    }

    private InputStream serializing(Object source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);

        oos.writeObject(source);

        return new ByteArrayInputStream(out.toByteArray());
    }
}
