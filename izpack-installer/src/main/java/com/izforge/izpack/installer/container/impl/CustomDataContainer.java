package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.core.container.AbstractChildContainer;
import com.izforge.izpack.installer.container.IApplicationContainer;

/**
 * Container for custom data
 */
public class CustomDataContainer extends AbstractChildContainer
{

    public CustomDataContainer(IApplicationContainer parent) throws ClassNotFoundException
    {
        super(parent);
    }

}
