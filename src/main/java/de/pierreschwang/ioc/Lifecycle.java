package de.pierreschwang.ioc;

import de.pierreschwang.ioc.error.IocResolveException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public enum Lifecycle {

    SINGLETON() {
        @Override
        public <T> Supplier<T> supply(IocRegistry registry, Class<T> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, IocResolveException {
            T instance = registry.getSingletonInstance(clazz);
            if (instance != null) {
                return () -> instance;
            }
            T constructed = new ConstructorResolver<T>(clazz, registry).construct();
            if (constructed == null) {
                throw new NullPointerException("Failed to construct instance for " + clazz.getName());
            }
            registry.registerSingletonInstance(clazz, constructed);
            return () -> constructed;
        }
    }, SCOPED() {
        @Override
        public <T> Supplier<T> supply(IocRegistry registry, Class<T> clazz) {
            return () -> registry.provideInstance(clazz);
        }
    };

    public abstract <T> Supplier<T> supply(IocRegistry registry, Class<T> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, IocResolveException;

}
