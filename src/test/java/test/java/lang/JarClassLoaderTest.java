package test.java.lang;

import com.holi.JarClassLoader;
import com.holi.Library;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class JarClassLoaderTest {

    @Test
    void loadExtClass() throws Throwable {
        Class<?> klass = JarClassLoader.loadExtClass(Library.class.getName());

        assertThat(klass, not(equalTo(Library.class)));
        assertThat(klass.getName(), equalTo(Library.class.getName()));
    }
}
