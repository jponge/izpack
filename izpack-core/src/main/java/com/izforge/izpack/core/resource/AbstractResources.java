package com.izforge.izpack.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import javax.swing.ImageIcon;

import org.apache.tools.ant.util.FileUtils;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;


/**
 * Abstract implementation of {@link Resources}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractResources implements Resources
{

    /**
     * The class loader.
     */
    private final ClassLoader loader;


    /**
     * Constructs an {@code AbstractResources} using the default class loader.
     */
    public AbstractResources()
    {
        this(AbstractResources.class.getClassLoader());
    }

    /**
     * Constructs an {@code AbstractResources} with the specified class loader.
     *
     * @param loader the loader to load resources
     */
    public AbstractResources(ClassLoader loader)
    {
        this.loader = loader;
    }

    /**
     * Returns the stream to a resource.
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
     * Returns the URL to a resource.
     *
     * @param name the resource name
     * @return the URL to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public URL getURL(String name)
    {
        URL result = loader.getResource(name);
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
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         if the resource cannot be retrieved
     */
    @Override
    public String getString(String name)
    {
        try
        {
            return readString(name, "UTF-8");
        }
        catch (IOException exception)
        {
            throw new ResourceException("Failed to read string resource: " + name, exception);
        }
    }

    /**
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name         the resource name
     * @param defaultValue the default value, if the resource cannot be found or retrieved
     * @return the resource as a string, or {@code defaultValue} if cannot be found or retrieved
     */
    @Override
    public String getString(String name, String defaultValue)
    {
        return getString(name, "UTF-8", defaultValue);
    }

    /**
     * Returns a resource as a string.
     *
     * @param name         the resource name
     * @param encoding     the resource encoding. May be {@code null}
     * @param defaultValue the default value, if the resource cannot be found or retrieved
     * @return the resource as a string, or {@code defaultValue} if cannot be found or retrieved
     */
    @Override
    public String getString(String name, String encoding, String defaultValue)
    {
        String result;
        try
        {
            result = readString(name, encoding);
        }
        catch (Exception exception)
        {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns an {@code ImageIcon} resource.
     *
     * @param name         the resource name
     * @param alternatives alternative resource names, if {@code name} is not found
     * @return the corresponding {@code ImageIcon}
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public ImageIcon getImageIcon(String name, String... alternatives)
    {
        URL location = loader.getResource(name);
        if (location != null)
        {
            return new ImageIcon(location);
        }
        for (String fallback : alternatives)
        {
            location = loader.getResource(fallback);
            if (location != null)
            {
                return new ImageIcon(location);
            }
        }
        StringBuilder message = new StringBuilder("Image icon resource not found in ");
        message.append(name);
        if (alternatives.length != 0)
        {
            message.append(" or ");
            message.append(Arrays.toString(alternatives));
        }
        throw new ResourceNotFoundException(message.toString());
    }

    /**
     * Reads a string resource.
     *
     * @param name     the resource name
     * @param encoding the resource encoding. May be {@code null}
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws java.io.IOException       for any I/O error
     */
    protected String readString(String name, String encoding) throws IOException
    {
        String result;
        InputStream in = getInputStream(name);
        InputStreamReader reader = null;
        try
        {
            reader = (encoding != null) ? new InputStreamReader(in, encoding) : new InputStreamReader(in);
            result = FileUtils.readFully(reader);
        }
        finally
        {
            FileUtils.close(reader);
            FileUtils.close(in);
        }
        return result;
    }

}
