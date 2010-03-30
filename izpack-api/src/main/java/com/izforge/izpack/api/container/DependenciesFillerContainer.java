package com.izforge.izpack.api.container;

import org.picocontainer.MutablePicoContainer;

/**
 * Fill dependencies in container
 *
 * @author Anthonin Bonnefoy
 */
public interface DependenciesFillerContainer
{
    void fillContainer(MutablePicoContainer picoContainer);
}
