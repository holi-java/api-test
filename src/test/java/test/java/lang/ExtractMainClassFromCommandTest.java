package test.java.lang;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 5/22/17.
 */
public class ExtractMainClassFromCommandTest {

    @Test
    void runsWithNoArgs() throws Throwable {
        String[] args = run(Application.class, withNoArgs());

        assertThat(getMainClass(args), equalTo(Application.class));
    }

    @Test
    void runsWith1Arg() throws Throwable {
        String[] args = run(Application.class, withArgs("foo"));

        assertThat(getMainClass(args), equalTo(Application.class));
    }

    @Test
    void runsWithManyArgs() throws Throwable {
        String[] args = run(Application.class, withArgs("foo", "bar"));

        assertThat(getMainClass(args), equalTo(Application.class));
    }

    @Test
    void runsByIDE() throws Throwable {
        String[] args = run(Application.class, by("AppMain"), withNoArgs());

        assertThat(getMainClass(args), equalTo(Application.class));
    }

    private static Class<?> getMainClass(String... args) throws ClassNotFoundException {
        String command = commandWithoutArgs(args);
        String[] classes = command.split("\\s+");
        return Class.forName(classes[classes.length - 1]);
    }

    private static String commandWithoutArgs(String[] args) {
        String command = System.getProperty("sun.java.command");
        return command.substring(0, command.length() - argsLength(args)).trim();
    }

    private static int argsLength(String[] args) {
        if (args.length == 0) {
            return 0;
        }
        return Stream.of(args).collect(Collectors.joining(" ")).length() + 1;
    }

    private String[] run(Class<?> mainClass, Optional<String> launcherClass, String[] args) {
        System.setProperty("sun.java.command", Stream.concat(command(mainClass, launcherClass), Stream.of(args))
                .collect(Collectors.joining(" ")));
        return args;
    }

    private Stream<String> command(Class<?> mainClass, Optional<String> launcherClass) {
        return launcherClass.map(it -> Stream.of(it, mainClass.getName())).orElseGet(() -> Stream.of(mainClass.getName()));
    }

    private String[] run(Class<?> mainClass, String[] args) {
        return run(mainClass, Optional.empty(), args);
    }

    private String[] withNoArgs() {
        return withArgs();
    }


    private Optional<String> by(String launcherClass) {
        return Optional.of(launcherClass);
    }

    private String[] withArgs(String... args) {
        return args;
    }

    static class Application {
    }
}
