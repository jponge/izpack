package com.izforge.izpack.installer.container;

/**
 * Interface for panel level component
 */
public interface IInstallerContainer
{

    <T> void addComponent(Class<T> componentType);

    <T> T getComponent(final Class<T> componentType);

    void addComponent(Object componentType, Object implementation);

    Object getComponent(Object componentKeyOrType);

}
