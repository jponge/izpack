package com.izforge.izpack.container;

import org.picocontainer.MutablePicoContainer;

/**
 * Interface for panel level component
 */
public interface IInstallerContainer {

    <T> void addComponent(Class<T> componentType);

    <T> T getComponent(final Class<T> componentType);

    void dispose();

    MutablePicoContainer makeChildContainer();

    void removeComponent(Object abstractUIHandlerInContainer);

    void addComponent(Object componentType, Object implementation);

    Object getComponent(Object componentKeyOrType);

}
