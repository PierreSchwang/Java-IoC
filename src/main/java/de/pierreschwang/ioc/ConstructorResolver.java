package de.pierreschwang.ioc;

import de.pierreschwang.ioc.error.IocResolveException;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class ConstructorResolver<T> {

    private final Class<T> clazz;
    private final IocRegistry registry;

    public ConstructorResolver(Class<T> clazz, IocRegistry registry) {
        this.clazz = clazz;
        this.registry = registry;
    }

    @Nullable
    public T construct() throws InvocationTargetException, InstantiationException, IllegalAccessException, IocResolveException {
        ConstructorResolveResult result = searchConstructors();
        if (result.getPossibleConstructors().isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner("");
            stringJoiner.add("Couldn't find any matching constructors to create a new instance of " + clazz.getName());
            stringJoiner.add("\nImpossible Constructors and their cause:");
            result.getImpossibleConstructors().forEach((constructor, parameters) -> {
                stringJoiner.add("\n-> ");
                stringJoiner.add(constructor.toString());
                stringJoiner.add("[ Failed to resolve definitions of parameters: ");
                for (Parameter parameter : parameters) {
                    stringJoiner.add(parameter.toString());
                }
                stringJoiner.add(" ]");
            });
            throw new IocResolveException(stringJoiner.toString());
        }
        // Determine easiest constructor
        Constructor<?> constructor = null;
        for (Constructor<?> possibleConstructor : result.getPossibleConstructors()) {
            if (constructor == null || constructor.getParameterCount() > possibleConstructor.getParameterCount()) {
                constructor = possibleConstructor;
            }
        }
        Object[] parameters = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameters().length; i++) {
            parameters[i] = registry.provideInstance(constructor.getParameters()[i].getType());
        }
        constructor.setAccessible(true);
        return (T) constructor.newInstance(parameters);
    }

    private ConstructorResolveResult searchConstructors() {
        ConstructorResolveResult result = new ConstructorResolveResult();

        Set<Parameter> failedParams = new HashSet<>();
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            for (Parameter parameter : constructor.getParameters()) {
                if (!this.registry.doesProvide(parameter.getType())) {
                    failedParams.add(parameter);
                }
            }
            if (failedParams.isEmpty()) {
                result.addPossibleConstructor(constructor);
            } else {
                result.addImpossibleConstructor(constructor, failedParams);
            }
            failedParams.clear();
        }
        return result;
    }

    private static final class ConstructorResolveResult {
        private final Set<Constructor<?>> possibleConstructors = new HashSet<>();
        private final Map<Constructor<?>, Set<Parameter>> impossibleConstructors = new HashMap<>();

        public void addPossibleConstructor(Constructor<?> constructor) {
            this.possibleConstructors.add(constructor);
        }

        public void addImpossibleConstructor(Constructor<?> constructor, Set<Parameter> failedParams) {
            this.impossibleConstructors.put(constructor, new HashSet<>(failedParams));
        }

        public Set<Constructor<?>> getPossibleConstructors() {
            return possibleConstructors;
        }

        public Map<Constructor<?>, Set<Parameter>> getImpossibleConstructors() {
            return impossibleConstructors;
        }
    }

}