package com.izforge.izpack.test.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

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
        if (runOn == null)
        {
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
        try
        {
            for (Field field : containerClass.getFields())
            {
                Annotation annotation = field.getAnnotation(Rule.class);
                if (annotation != null)
                {
                    TestRule rule = (TestRule) field.get(containerInstance);
                    Description description = Description.createTestDescription(
                            method.getMethod().getDeclaringClass(), method.getName());
                    statement = rule.apply(statement, description);

                }
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
                    containerInstance = createContainer(containerClass);
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

    private Container createContainer(Class<? extends Container> containerClass)
            throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
    {
        Constructor<? extends Container> constructor;
        Container result;
        try
        {
            constructor = containerClass.getConstructor(klass.getClass(), method.getClass());
            result = constructor.newInstance(klass, method);
        }
        catch (NoSuchMethodException exception)
        {
            try
            {
                constructor = containerClass.getConstructor(klass.getClass());
                result = constructor.newInstance(klass);
            }
            catch (NoSuchMethodException nested)
            {
                constructor = containerClass.getConstructor();
                result = constructor.newInstance();
            }
        }
        return result;
    }

}