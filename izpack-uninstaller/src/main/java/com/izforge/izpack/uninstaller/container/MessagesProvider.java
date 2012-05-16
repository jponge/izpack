package com.izforge.izpack.uninstaller.container;

import java.io.IOException;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;


/**
 * A provider of a {@link Messages} instances.
 *
 * @author Tim Anderson
 */
public class MessagesProvider implements Provider
{

    /**
     * Provides the  messages.
     *
     * @param locales the supported locales
     * @return the locale database
     * @throws IOException               for any I/O error
     * @throws ResourceNotFoundException if <em>langpack.xml</em> cannot be found
     */
    public Messages provide(Locales locales) throws IOException
    {
        return locales.getMessages("langpack.xml");
    }
}
