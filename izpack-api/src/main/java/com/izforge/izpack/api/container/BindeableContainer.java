package com.izforge.izpack.api.container;

import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;

/**
 * Interface for applcation container using Pico
 *
 * @author Anthonin Bonnefoy
 */
public interface BindeableContainer
{
    <T> void addComponent(Class<T> componentType);

    <T> T getComponent(final Class<T> componentType);

    void addComponent(Object componentType, Object implementation, Parameter... parameters);

    Object getComponent(Object componentKeyOrType);

    void initBindings() throws Exception;

    void dispose();

    MutablePicoContainer getContainer();

    MutablePicoContainer makeChildContainer();

    /**
     * Returns a class given its name.
     *
     * @param className the class name
     * @param superType the super type
     * @return the corresponding class
     * @throws ClassCastException           if <tt>className</tt> does not implement or extend <tt>superType</tt>
     * @throws IzPackClassNotFoundException if the class cannot be found
     */
    <T> Class<T> getClass(String className, Class<T> superType);
}
