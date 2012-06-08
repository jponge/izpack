package com.izforge.izpack.installer.unpacker;

import java.io.InputStream;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Resources;


/**
 * Console-based implementation of the {@link PackResources} interface.
 * <br/>
 * This implementation does not support web-based pack resources. TODO
 *
 * @author Tim Anderson
 */
public class ConsolePackResources extends AbstractPackResources
{

    /**
     * Constructs a {@code DefaultPackResources}.
     *
     * @param resources the local resources
     */
    public ConsolePackResources(Resources resources, InstallData installData)
    {
        super(resources, installData);
    }

    /**
     * Returns the stream to a web-based pack resource.
     *
     * @param name      the resource name
     * @param webDirURL the web URL to load the resource from
     * @return a stream to the resource
     * @throws ResourceException if invoked
     */
    @Override
    protected InputStream getWebPackStream(String name, String webDirURL)
    {
        throw new ResourceException("Cannot retrieve web-based resource: " + name + " from URL: " + webDirURL
                                            + ". This operation is not supported.");
    }
}
