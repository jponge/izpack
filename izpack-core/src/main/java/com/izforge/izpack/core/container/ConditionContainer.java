package com.izforge.izpack.core.container;

import org.picocontainer.MutablePicoContainer;

/**
 * Condition container
 *
 * @author Anthonin Bonnefoy
 */
public class ConditionContainer extends AbstractContainer
{

    public ConditionContainer(MutablePicoContainer parent)
    {
        pico = parent.makeChildContainer();
    }

    public void fillContainer(MutablePicoContainer picoContainer)
    {
    }
}
