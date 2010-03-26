package com.izforge.izpack.test.junit;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.test.Container;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Custom runner for getting dependencies injected in test with PicoContainer
 *
 * @author Anthonin Bonnefoy
 */
public class PicoRunner extends BlockJUnit4ClassRunner
{
    private Class<?> klass;
    private FrameworkMethod method;

    public PicoRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
        this.klass = klass;
    }

    @Override
    protected void validateConstructor(List<Throwable> errors)
    {
    }


    @Override
    protected Statement methodBlock(FrameworkMethod method)
    {
        this.method = method;
        return super.methodBlock(method);
    }

    @Override
    protected Object createTest() throws Exception
    {
        Class<? extends BindeableContainer> containerClass = getTestClass().getJavaClass().getAnnotation(Container.class).value();
        BindeableContainer installerContainer = getContainerInstance(containerClass);
        installerContainer.initBindings();
        installerContainer.addComponent(klass);
        Object component = installerContainer.getComponent(klass);
        return component;
    }

    private BindeableContainer getContainerInstance(Class<? extends BindeableContainer> containerClass) throws InvocationTargetException, IllegalAccessException, InstantiationException
    {
        Constructor<? extends BindeableContainer> constructor = getUniqueConstructor(containerClass);
        if (constructor.getParameterTypes().length == 1)
        {
            return constructor.newInstance(klass);
        }
        if (constructor.getParameterTypes().length == 2)
        {
            return constructor.newInstance(klass, method);
        }
        return constructor.newInstance();
    }

    private Constructor<? extends BindeableContainer> getUniqueConstructor(Class<? extends BindeableContainer> containerClass)
    {
        Constructor<?>[] constructors = containerClass.getConstructors();
        if (constructors.length > 1)
        {
            throw new IllegalArgumentException("There should be only one constructor for " + containerClass);
        }
        return (Constructor<? extends BindeableContainer>) constructors[0];
    }
}