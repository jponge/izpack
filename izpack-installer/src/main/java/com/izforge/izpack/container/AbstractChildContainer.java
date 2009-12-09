package com.izforge.izpack.container;

import org.picocontainer.MutablePicoContainer;

/**
 * Abstract container for child of application container
 */
public class AbstractChildContainer implements IInstallerContainer {
    protected MutablePicoContainer pico;

    public AbstractChildContainer(IApplicationContainer parent) {
        pico = parent.makeChildContainer();
    }

    public <T> void addComponent(Class<T> componentType) {
        pico.addComponent(componentType);
    }

    public void addComponent(Object componentType, Object implementation) {
        pico.addComponent(componentType, implementation);
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }

    public Object getComponent(Object componentKeyOrType) {
        return pico.getComponent(componentKeyOrType);
    }
}
