package test.java.io;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 4/5/17.
 */
public class ObjectOutputStreamTest {


    @Test
    void failsToWriteNonSerializableObject() throws Throwable {
        assertThrows(NotSerializableException.class, () -> write(new EmptyObject()));
    }

    @Test
    void failsToWriteNonSerializableObjectEvenIfDeclaredWriteObjectMethod() throws Throwable {
        assertThrows(NotSerializableException.class, () -> write(new NonSerializableClass()));
    }

    @Test
    void writeSerializeObject() throws Throwable {
        SerializableClass serialized = read(write(new SerializableClass("bar")), SerializableClass.class);

        assertThat(serialized.foo, equalTo("serialized:bar"));
    }

    @Test
    void writeSerializeObjectContainingNonSerializableFieldWithinNullValue() throws Throwable {
        NonSerializableClass value = null;

        NonSerializableField serialized = read(write(new NonSerializableField(value)), NonSerializableField.class);

        assertThat(serialized.field, is(nullValue()));
    }

    @Test
    void failsToWriteSerializeObjectContainingNonSerializableFieldWithinValue() throws Throwable {
        NonSerializableClass value = new NonSerializableClass();

        assertThrows(NotSerializableException.class, () -> write(new NonSerializableField(value)));
    }

    private <T> T read(byte[] bytes, Class<T> type) throws IOException, ClassNotFoundException {
        return type.cast(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
    }

    private byte[] write(Object obj) throws IOException {
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bb);
        out.writeObject(obj);
        return bb.toByteArray();
    }

    static class EmptyObject {
    }

    static class NonSerializableClass {
        private void writeObject(ObjectOutputStream out) throws IOException {
        }

        private void readObject(ObjectInputStream in) throws IOException {
        }
    }

    static class NonSerializableField implements Serializable {
        private NonSerializableClass field;

        public NonSerializableField(NonSerializableClass field) {
            this.field = field;
        }
    }

    static class SerializableClass implements Serializable {
        private String foo;

        public SerializableClass(String foo) {
            this.foo = foo;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF("serialized:" + foo);
        }

        private void readObject(ObjectInputStream in) throws IOException {
            foo = in.readUTF();
        }
    }
}
