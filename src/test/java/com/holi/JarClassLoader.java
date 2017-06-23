package com.holi;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JarClassLoader extends URLClassLoader {
    private static final ClassLoader INSTANCE = new JarClassLoader();
    private static final String EXTERNAL = "jar:";
    private Map<String, Class<?>> cache = new ConcurrentHashMap<>();

    private JarClassLoader() {
        super(externalJars());
    }

    private static URL[] externalJars() {
        return Arrays.stream(((URLClassLoader) getSystemClassLoader()).getURLs())
                .filter(it -> it.getFile().endsWith(".jar"))
                .toArray(URL[]::new);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return name.startsWith(EXTERNAL) ? loadExtClass(name, resolve) : super.loadClass(name, resolve);
    }

    private Class<?> loadExtClass(String name, boolean resolve) throws ClassNotFoundException {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        synchronized (getClassLoadingLock(name)) {
            if (cache.containsKey(name)) {
                return cache.get(name);
            }
            Class<?> klass = findClass(name.substring(EXTERNAL.length()));
            if (resolve) {
                resolveClass(klass);
            }
            cache.put(name, klass);
            return klass;
        }
    }

    public static Class<?> loadExtClass(String name) throws ClassNotFoundException {
        return INSTANCE.loadClass(EXTERNAL + name);
    }
}
