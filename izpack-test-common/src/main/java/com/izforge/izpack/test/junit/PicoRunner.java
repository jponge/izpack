package com.izforge.izpack.test.junit;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

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
    private Class<? extends Container> containerClass;
    private Container containerInstance;

    public PicoRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
        this.klass = klass;
    }

    @Override
    protected void validateConstructor(List<Throwable> errors)
    {
    }

    /**
     * Runs the test corresponding to {@code method}, unless it is ignored, or is not intended to be run on
     * the current platform.
     *
     * @param method   the test method
     * @param notifier the run notifier
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier)
    {
        RunOn runOn = method.getAnnotation(RunOn.class);
        if (runOn == null) {
            runOn = method.getMethod().getDeclaringClass().getAnnotation(RunOn.class);
        }
        boolean ignore = false;
        if (runOn != null)
        {
            Platform platform = new Platforms().getCurrentPlatform();
            boolean found = false;
            for (Platform.Name name : runOn.value())
            {
                if (platform.isA(name))
                {
                    found = true;
                    break;
                }
            }
            ignore = !found;
        }

        if (!ignore)
        {
            super.runChild(method, notifier);
        }
        else
        {
            notifier.fireTestIgnored(describeChild(method));
        }
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
        containerClass = getTestClass().getJavaClass().getAnnotation(com.izforge.izpack.test.Container.class).value();
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    containerInstance = getContainerInstance(containerClass);
                    containerInstance.addComponent(klass);
                    currentTestInstance = containerInstance.getComponent(klass);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new IzPackException(e);
                }
            }
        });
        return currentTestInstance;
    }

    private Container getContainerInstance(Class<? extends Container> containerClass)
            throws InvocationTargetException, IllegalAccessException, InstantiationException
    {
        Constructor<? extends Container> constructor = getUniqueConstructor(containerClass);
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

    private Constructor<? extends Container> getUniqueConstructor(Class<? extends Container> containerClass)
    {
        Constructor<?>[] constructors = containerClass.getConstructors();
        if (constructors.length > 1)
        {
            throw new IllegalArgumentException("There should be only one constructor for " + containerClass);
        }
        return (Constructor<? extends Container>) constructors[0];
    }
}