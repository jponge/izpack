package com.izforge.izpack.container;

/**
 * Abstract container for child of application container
 */
public class AbstractChildContainer extends AbstractContainer implements IInstallerContainer {

    public AbstractChildContainer(IApplicationContainer parent) {
        pico = parent.makeChildContainer();
    }
}
