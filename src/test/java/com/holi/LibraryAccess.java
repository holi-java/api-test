package com.holi;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;

public interface LibraryAccess<T> {
    boolean exists(Function<? super T, Boolean> condition);

    @SafeVarargs
    static <T> LibraryAccess<T> newInstance(T... items) {
        try {
            Constructor<?> constructor = JarClassLoader.loadExtClass(Library.class.getName()).getConstructor(Object[].class);
            Object target = constructor.newInstance(new Object[]{items});
            MethodHandles.Lookup lookup = MethodHandles.lookup().in(target.getClass());
            return (LibraryAccess<T>) (Proxy.newProxyInstance(
                    ClassLoader.getSystemClassLoader(),
                    new Class[]{LibraryAccess.class},
                    (proxy, method, args) -> lookup.bind(target, method.getName(), methodType(method.getReturnType(), method.getParameterTypes())).invokeWithArguments(args)
            ));
        } catch (Exception ex) {
            InstantiationError error = new InstantiationError();
            error.initCause(ex);
            throw error;
        }
    }


}
