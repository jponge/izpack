package com.izforge.izpack.core.container;


import org.picocontainer.injectors.Provider;

import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;


/**
 * Injection provider for the current {@link Platform}.
 *
 * @author Tim Anderson
 */
public class PlatformProvider implements Provider
{

    /**
     * Provides the current platform.
     *
     * @param platforms the platform factory
     * @return the current platform
     */
    public Platform provide(Platforms platforms)
    {
        return platforms.getCurrentPlatform();
    }
}
