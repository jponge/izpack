package com.izforge.izpack.uninstaller.container;

import java.io.IOException;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.uninstaller.resource.Resources;


/**
 * A provider of a {@link LocaleDatabase} instances.
 *
 * @author Tim Anderson
 */
public class LocaleDatabaseProvider implements Provider
{

    /**
     * Provides the locale database.
     *
     * @param resources used to locate the <em>langpack.xml</em> resource
     * @return the locale database
     * @throws IOException               for any I/O error
     * @throws ResourceNotFoundException if <em>langpack.xml</em> cannot be found
     */
    public LocaleDatabase provide(Resources resources) throws IOException
    {
        return new LocaleDatabase(resources.getInputStream("langpack.xml"));
    }
}
