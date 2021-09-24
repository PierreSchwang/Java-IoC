package de.pierreschwang.ioc.test;

import de.pierreschwang.ioc.Ioc;
import de.pierreschwang.ioc.Lifecycle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IocTest {

    @Test
    public void testSingleton() {
        Ioc.register(ISingletonDefinition.class, SingletonImplementation.class, Lifecycle.SINGLETON);

        Assert.assertNotNull(Ioc.getInstance(ISingletonDefinition.class));
        Assert.assertEquals(Ioc.getInstance(ISingletonDefinition.class), Ioc.getInstance(ISingletonDefinition.class));
    }

    @Test()
    public void testInvalidConstructor() {
        Ioc.register(IConstructorDefinition.class, ConstructorImplementation.class);
        Assert.assertNull(Ioc.getInstance(IConstructorDefinition.class));
    }

    @Test
    public void testValidConstructor() {
        Ioc.register(ISingletonDefinition.class, SingletonImplementation.class, Lifecycle.SINGLETON);
        Ioc.register(IConstructorDefinition.class, ConstructorImplementation.class);
    }

    @Before
    public void before() {
        Ioc.clear();
    }

    private interface ISingletonDefinition {
    }

    private static class SingletonImplementation implements ISingletonDefinition {
    }

    private interface IConstructorDefinition {
    }

    private static class ConstructorImplementation implements IConstructorDefinition {
        public ConstructorImplementation(ISingletonDefinition singletonDefinition) {
        }
    }

}
