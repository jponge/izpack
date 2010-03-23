package com.izforge.izpack.core.container;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.container.DependenciesFillerContainer;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;

/**
 * Abstract container for commons methods
 */
public abstract class AbstractContainer implements BindeableContainer
{
    protected MutablePicoContainer pico;

    public <T> void addComponent(Class<T> componentType)
    {
        pico.as(Characteristics.USE_NAMES).addComponent(componentType);
    }

    public void addComponent(Object componentType, Object implementation)
    {
        pico.addComponent(componentType, implementation);
    }

    public <T> T getComponent(Class<T> componentType)
    {
        return pico.getComponent(componentType);
    }

    public Object getComponent(Object componentKeyOrType)
    {
        return pico.getComponent(componentKeyOrType);
    }

    public void addConfig(String name, Object val)
    {
        pico.addConfig(name, val);
    }

    public void dispose()
    {
        pico.dispose();
    }

    public MutablePicoContainer fillContainer(DependenciesFillerContainer fillerContainer)
    {
        return fillerContainer.fillContainer(pico);
    }

    public MutablePicoContainer makeChildContainer()
    {
        return pico.makeChildContainer();
    }

    public MutablePicoContainer getContainer()
    {
        return pico;
    }
}
