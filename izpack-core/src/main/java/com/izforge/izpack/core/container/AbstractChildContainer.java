package com.izforge.izpack.core.container;

/**
 * Abstract container for child of application container
 */
public class AbstractChildContainer extends AbstractContainer
{

    public AbstractChildContainer(AbstractContainer parent)
    {
        pico = parent.makeChildContainer();
    }
}
