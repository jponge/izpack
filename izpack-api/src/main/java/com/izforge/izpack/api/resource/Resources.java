package com.izforge.izpack.api.resource;

import java.io.IOException;
import java.io.InputStream;

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
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name the resource name
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws IOException               if the resource cannot be read
     */
    String getString(String name) throws IOException;

    /**
     * Returns a resource as a string.
     *
     * @param name     the resource name
     * @param encoding the resource encoding. May be {@code null}
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws IOException               if the resource cannot be read
     */
    String getString(String name, String encoding) throws IOException;
}