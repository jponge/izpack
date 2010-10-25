package com.izforge.izpack.test.junit;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.test.Container;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.*;

import javax.swing.*;
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
    private Object currentTestInstance;
    private Class<? extends BindeableContainer> containerClass;
    private BindeableContainer containerInstance;

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
        Statement statement = super.methodBlock(method);
        List<FrameworkField> methodRules = new TestClass(containerClass).getAnnotatedFields(Rule.class);
        try
        {
            for (FrameworkField methodField : methodRules)
            {
                MethodRule methodRule = (MethodRule) methodField.get(containerInstance);
                statement = methodRule.apply(statement, method, currentTestInstance);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new IzPackException(e);
        }
        return statement;
    }


    @Override
    protected Object createTest() throws Exception
    {
        containerClass = getTestClass().getJavaClass().getAnnotation(Container.class).value();
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    containerInstance = getContainerInstance(containerClass);
                    containerInstance.initBindings();
                    containerInstance.addComponent(klass);
                    currentTestInstance = containerInstance.getComponent(klass);
                }
                catch (Exception e)
                {
                    throw new IzPackException(e);
                }
            }
        });
        return currentTestInstance;
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