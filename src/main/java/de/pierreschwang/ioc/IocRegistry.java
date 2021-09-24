package de.pierreschwang.ioc;

import com.cookingfox.guava_preconditions.Preconditions;
import de.pierreschwang.ioc.error.IocResolveException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class IocRegistry {

    private final Map<Class<?>, Object> singletonHolder = new HashMap<>();
    private final Map<Class<?>, Supplier<Object>> scopedHolder = new HashMap<>();

    @Nullable
    <T> T getSingletonInstance(@NotNull Class<T> clazz) {
        Preconditions.checkNotNull(clazz);
        if (!singletonHolder.containsKey(clazz)) {
            return null;
        }
        return (T) singletonHolder.get(clazz);
    }

    <T> void registerSingletonInstance(@NotNull Class<T> clazz, Class<? extends T> implementationClass) {
        try {
            T implementation = new ConstructorResolver<>(implementationClass, this).construct();
            if (implementation == null) {
                throw new IocResolveException("ConstructorResolver#construct returned null for " + implementationClass.getName());
            }
            registerSingletonInstance(clazz, implementation);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IocResolveException e) {
            e.printStackTrace();
        }
    }

    <T> void registerSingletonInstance(@NotNull Class<T> clazz, @NotNull T instance) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(instance);
        this.singletonHolder.put(clazz, instance);
    }

    <T> void registerScoped(@NotNull Class<T> clazz, Class<? extends T> implementationClass) {
        this.scopedHolder.put(clazz, () -> {
            try {
                T implementation = new ConstructorResolver<>(implementationClass, this).construct();
                if (implementation == null) {
                    throw new IocResolveException("ConstructorResolver#construct returned null for " + implementationClass.getName());
                }
                return implementation;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IocResolveException e) {
                e.printStackTrace();
            }
            return null;
        });
        this.scopedHolder.get(clazz).get();
    }

    public boolean doesProvide(@NotNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);
        return singletonHolder.containsKey(clazz) || scopedHolder.containsKey(clazz);
    }

    @Nullable
    public <T> T provideInstance(@NotNull Class<T> clazz) {
        if (singletonHolder.containsKey(clazz)) {
            return (T) singletonHolder.get(clazz);
        }
        if (scopedHolder.containsKey(clazz)) {
            return (T) scopedHolder.get(clazz).get();
        }
        return null;
    }

    public void clear() {
        this.scopedHolder.clear();
        this.singletonHolder.clear();
    }
}
