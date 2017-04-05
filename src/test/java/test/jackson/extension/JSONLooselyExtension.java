package test.jackson.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static java.util.stream.Collectors.toList;

/**
 * Created by holi on 4/6/17.
 */
public class JSONLooselyExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(TestExtensionContext context) throws Exception {
        allOfObjectMapper(context).forEach(mapper -> mapper.enable(ALLOW_UNQUOTED_FIELD_NAMES, ALLOW_SINGLE_QUOTES));
    }

    private List<ObjectMapper> allOfObjectMapper(TestExtensionContext context) throws Exception {
        return allObjectMapperFields(context).stream().map(valueIn(context.getTestInstance())).collect(toList());
    }

    private List<Field> allObjectMapperFields(TestExtensionContext context) throws Exception {
        return Arrays.stream(context.getTestInstance().getClass().getDeclaredFields())
                .filter(it -> !Modifier.isStatic(it.getModifiers()))
                .filter(it -> ObjectMapper.class.isAssignableFrom(it.getType()))
                .collect(toList());
    }

    private Function<Field, ObjectMapper> valueIn(Object target) {
        return (field) -> {
            field.setAccessible(true);
            try {
                return (ObjectMapper) field.get(target);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        };
    }

}
