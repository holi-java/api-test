package com.holi.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by holi on 4/6/17.
 */
public class Lambda {

    public static <T, U> Consumer<T> curry(BiConsumer<T, U[]> lambda, U... varArgs) {
        return it -> lambda.accept(it, varArgs);
    }

}
