package com.izforge.izpack.test.junit;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.test.Container;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * Custom runner for getting dependencies injected in test with PicoContainer
 *
 * @author Anthonin Bonnefoy
 */
public class
        PicoRunner extends BlockJUnit4ClassRunner
{
    private Class<?> klass;

    public PicoRunner(Class<?> klass)
            throws InitializationError
    {
        super(klass);
        this.klass = klass;
    }

    @Override
    protected void validateConstructor(List<Throwable> errors)
    {
    }

    @Override
    protected Object createTest() throws Exception
    {
        Class<? extends BindeableContainer> containerClass = getTestClass().getJavaClass().getAnnotation(Container.class).value();
        BindeableContainer installerContainer = containerClass.newInstance();
        installerContainer.initBindings();
        installerContainer.addComponent(klass);
        Object component = installerContainer.getComponent(klass);
        return component;
    }
}
