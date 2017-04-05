package com.holi.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by holi on 4/6/17.
 */
public class ReflectionStreams {
    public static <T> Stream<T> allOf(Class<T> type, Object target) throws Exception {
        return allOfInstanceFields(type, target).map(valueIn(target, type));
    }

    public static <T> Stream<Field> allOfInstanceFields(Class<T> type, Object target) {
        return allOfInstanceFields(target, typeOf(type));
    }

    public static <T> Stream<Field> allOfInstanceFields(Object target, Predicate<Field> condition) {
        return allOfFields(target, condition).filter(isNotStatic());
    }

    public static <T> Stream<Field> allOfFields(Object target, Predicate<Field> condition) {
        return allOfFields(target.getClass(), condition);
    }

    public static Stream<Field> allOfFields(Class<?> type, Predicate<Field> condition) {
        return Stream.of(type.getDeclaredFields()).filter(condition);
    }

    private static <T> Function<Field, T> valueIn(Object target, Class<T> type) {
        return (it) -> {
            boolean accessible = it.isAccessible();
            it.setAccessible(true);
            try {
                return type.cast(it.get(target));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } finally {
                it.setAccessible(accessible);
            }
        };
    }

    private static Predicate<Field> typeOf(Class<?> type) {
        return it -> type.isAssignableFrom(it.getType());
    }

    private static Predicate<? super Member> isNotStatic() {
        return it -> !Modifier.isStatic(it.getModifiers());
    }
}
