package com.izforge.izpack.bootstrap;

import com.izforge.izpack.installer.base.LanguageDialog;
import org.picocontainer.DefaultPicoContainer;

/**
 * Interface for application level component container. 
 */
public interface IApplicationComponent {
    void initBindings();

    <T> T getComponent(final Class<T> componentType);

    void dispose();
}
