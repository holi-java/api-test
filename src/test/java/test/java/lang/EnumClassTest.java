package test.java.lang;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static test.java.lang.EnumClassTest.Constant.BODY;
import static test.java.lang.EnumClassTest.Constant.NOBODY;

/**
 * Created by holi on 5/11/17.
 */
public class EnumClassTest {
    enum Constant {
        BODY {},
        NOBODY
    }

    @Test
    void typeofEnumConstantWithoutBodyIsEnumClass() throws Throwable {
        assertThat(NOBODY.getClass(), equalTo(Constant.class));
    }

    @Test
    void definesAnonymousEnumClassWhenEnumConstantWithBody() throws Throwable {
        Class<? extends Constant> it = BODY.getClass();

        assertThat(it, not(equalTo(Constant.class)));
        assertThat(it.isAnonymousClass(), is(true));
        assertThat(it, typeCompatibleWith(Constant.class));
    }
}
