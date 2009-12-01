package com.izforge.izpack.bootstrap;

import org.picocontainer.MutablePicoContainer;

/**
 * Interface for panel level component
 */
public interface IPanelComponent {

    <T> void addComponent(Class<T> componentType);

    <T> T getComponent(final Class<T> componentType);

    void dispose();

    MutablePicoContainer makeChildContainer();
}
