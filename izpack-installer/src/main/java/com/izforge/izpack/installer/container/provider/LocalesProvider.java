package com.izforge.izpack.installer.container.provider;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.ResourceManager;

/**
 * Provider of {@link Locales}.
 *
 * @author Tim Anderson
 */
public class LocalesProvider implements Provider
{

    public Locales provide(ResourceManager resources)
    {
        Locales locales = new DefaultLocales(resources);
        resources.setLocales(locales);
        return locales;
    }
}
