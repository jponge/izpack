package com.izforge.izpack.api.container;

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
}
