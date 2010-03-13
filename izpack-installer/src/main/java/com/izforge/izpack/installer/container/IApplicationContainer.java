package com.izforge.izpack.installer.container;

import org.picocontainer.MutablePicoContainer;

/**
 * Interface for application level component container.
 */
public interface IApplicationContainer
{
    <T> T getComponent(final Class<T> componentType);

    void dispose();

    MutablePicoContainer makeChildContainer();

    void initBindings();
}
