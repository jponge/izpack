package com.izforge.izpack.api.resource;

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
}