package com.holi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;

import java.util.function.Consumer;

import static com.holi.utils.ReflectionStreams.allOf;

/**
 * Created by holi on 4/6/17.
 */
public class FieldInitializationExtension<T> implements BeforeEachCallback {
    private final Class<T> type;
    private final Consumer<T> initialization;

    public FieldInitializationExtension(Class<T> type, Consumer<T> initialization) {
        this.type = type;
        this.initialization = initialization;
    }

    @Override
    public void beforeEach(TestExtensionContext context) throws Exception {
        allOf(type, in(context)).forEach(initialization);
    }

    private Object in(TestExtensionContext context) {
        return context.getTestInstance();
    }
}
