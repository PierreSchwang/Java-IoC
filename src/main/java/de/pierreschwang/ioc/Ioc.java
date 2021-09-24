package de.pierreschwang.ioc;

public final class Ioc {

    private static final IocRegistry registry = new IocRegistry();

    public static <T, I extends T> void register(Class<T> definition, Class<I> implementation) {
        register(definition, implementation, Lifecycle.SCOPED);
    }

    public static <T, I extends T> void register(Class<T> definition, Class<I> implementation, Lifecycle lifecycle) {
        if (lifecycle == Lifecycle.SINGLETON) {
            registry.registerSingletonInstance(definition, implementation);
            return;
        }
        registry.registerScoped(definition, implementation);
    }

    public static <T> T getInstance(Class<T> definition) {
        return registry.provideInstance(definition);
    }

    public static void clear() {
        registry.clear();
    }

}
