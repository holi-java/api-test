package test.java.utils;

import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 4/10/17.
 */
public class ScannerTest {
    @Test
    void nextNotIncludingWhitespaces() throws Throwable {
        Scanner it = scanner("1\n2 3");

        assertThat(it.next(), equalTo("1"));
        assertThat(it.next(), equalTo("2"));
        assertThat(it.next(), equalTo("3"));
    }

    @Test
    void nextNotSkippingWhitespaces() throws Throwable {
        Scanner it = scanner("1\n2 3");

        it.next();
        assertThat(it.nextLine(), equalTo(""));

        it.next();
        assertThat(it.nextLine(), equalTo(" 3"));
    }

    @Test
    void throwsNoSuchElementExceptionWhenCallingNextSinceReaderAtEOF() throws Throwable {
        assertThrows(NoSuchElementException.class, () -> scanner("").next());
    }

    private Scanner scanner(String input) {
        return new Scanner(input);
    }
}
