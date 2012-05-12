package com.izforge.izpack.uninstaller.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.file.FileUtils;


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

    /**
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name the resource name
     * @return the resource as a string
     * @throws com.izforge.izpack.api.exception.ResourceNotFoundException
     *                             if the resource cannot be found
     * @throws java.io.IOException if the resource cannot be read
     */
    @Override
    public String getString(String name) throws IOException
    {
        return getString(name, "UTF-8");
    }

    /**
     * Returns a resource as a string.
     *
     * @param name     the resource name
     * @param encoding the resource encoding. May be <tt>null</tt>
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws IOException               if the resource cannot be read
     */
    @Override
    public String getString(String name, String encoding) throws IOException
    {
        InputStream in = getInputStream(name);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            byte[] buffer = new byte[5120];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
            if (encoding != null)
            {
                return out.toString(encoding);
            }
            else
            {
                return out.toString();
            }
        }
        finally
        {
            FileUtils.close(in);
            FileUtils.close(out);
        }
    }
}
