package com.izforge.izpack.api.resource;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;


/**
 * IzPack resources.
 *
 * @author Tim Anderson
 */
public interface Resources
{

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    InputStream getInputStream(String name);

    /**
     * Returns the URL to a resource.
     *
     * @param name the resource name
     * @return the URL to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    URL getURL(String name);

    /**
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name the resource name
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         if the resource cannot be retrieved
     */
    String getString(String name);

    /**
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name         the resource name
     * @param defaultValue the default value, if the resource cannot be found or retrieved
     * @return the resource as a string, or {@code defaultValue} if cannot be found or retrieved
     */
    String getString(String name, String defaultValue);

    /**
     * Returns a resource as a string.
     *
     * @param name         the resource name
     * @param encoding     the resource encoding. May be {@code null}
     * @param defaultValue the default value, if the resource cannot be found or retrieved
     * @return the resource as a string, or {@code defaultValue} if cannot be found or retrieved
     */
    String getString(String name, String encoding, String defaultValue);

    /**
     * Returns an {@code ImageIcon} resource.
     *
     * @param name         the resource name
     * @param alternatives alternative resource names, if {@code name} is not found
     * @return the corresponding {@code ImageIcon}
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    ImageIcon getImageIcon(String name, String... alternatives);
}