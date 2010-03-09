package com.izforge.izpack.installer.container;

import com.izforge.izpack.core.container.AbstractContainer;

/**
 * Abstract container for child of application container
 */
public class AbstractChildContainer extends AbstractContainer implements IInstallerContainer {

    public AbstractChildContainer(IApplicationContainer parent) {
        pico = parent.makeChildContainer();
    }
}
