package com.izforge.izpack.core.container;

import org.picocontainer.MutablePicoContainer;

/**
 * Container for custom data
 */
public class CustomDataContainer extends AbstractContainer
{

    public CustomDataContainer(MutablePicoContainer parent) throws ClassNotFoundException
    {
        pico = parent.makeChildContainer();
    }

    public void fillContainer(MutablePicoContainer picoContainer)
    {
    }
}
