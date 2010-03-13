package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.core.container.AbstractContainer;

/**
 * Container for custom data
 */
public class CustomDataContainer extends AbstractContainer
{

    public CustomDataContainer(InstallerContainer parent) throws ClassNotFoundException
    {
        pico = parent.makeChildContainer();
    }

    public void initBindings() throws Exception
    {
    }
}
