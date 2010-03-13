package com.izforge.izpack.test.testng;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.test.Container;
import org.testng.IObjectFactory;

import java.lang.reflect.Constructor;

/**
 * Object factory using picoContainer to create test instance in TestNG
 *
 * @author Anthonin Bonnefoy
 */
public class PicoNGFactory implements IObjectFactory
{
    public Object newInstance(Constructor constructor, Object... params)
    {
        try
        {
            Class klass = constructor.getDeclaringClass();
            Class<? extends BindeableContainer> containerClass = ((Container) klass.getAnnotation(Container.class)).value();
            BindeableContainer installerContainer = containerClass.newInstance();
            installerContainer.initBindings();
            installerContainer.addComponent(klass);
            return installerContainer.getComponent(klass);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
