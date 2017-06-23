package com.holi;
// the external library source
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.function.Function;
//
//public class Library<T> {
//    private final List<T> items;
//
//    public Library(T... items) {
//        this.items = Arrays.asList(items);
//    }
//
//    public boolean exists(Function<? super T, Boolean> condition) {
//        return items.stream().anyMatch(condition::apply);
//    }
//}


import java.util.function.Function;
import java.util.function.Predicate;

public class Library<T> {

    private final LibraryAccess<T> library;

    public Library(T... items) {
        library = LibraryAccess.newInstance(items);
    }

    public boolean exists(Predicate<? super T> condition) {
        return library.exists(condition::test);
    }

    public boolean exists(Function<? super T, Boolean> condition) {
        return library.exists(condition);
    }
}