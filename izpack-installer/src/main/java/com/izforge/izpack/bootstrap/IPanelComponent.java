package com.izforge.izpack.bootstrap;

/**
 * Interface for panel level component
 */
public interface IPanelComponent extends IApplicationComponent{

    <T> void addComponent(Class<T> componentType);
}
