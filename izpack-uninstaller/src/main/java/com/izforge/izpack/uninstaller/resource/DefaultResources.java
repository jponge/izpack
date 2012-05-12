package com.izforge.izpack.uninstaller.resource;

import java.io.InputStream;

import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;


/**
 * Default {@link} Resources} implementation.
 *
 * @author Tim Anderson
 */
public class DefaultResources implements Resources
{
    private final ClassLoader loader;

    /**
     * Constructs a <tt>DefaultResources</tt>.
     */
    public DefaultResources()
    {
        this(DefaultResources.class.getClassLoader());
    }

    /**
     * Constructs a <tt>DefaultResources</tt>.
     *
     * @param loader the class loader to load resources
     */
    public DefaultResources(ClassLoader loader)
    {
        this.loader = loader;
    }

    /**
     * Returns the stream to a resources.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public InputStream getInputStream(String name)
    {
        InputStream result = loader.getResourceAsStream(name);
        if (result == null)
        {
            throw new ResourceNotFoundException("Failed to locate resource: " + name);
        }
        return result;
    }
}
