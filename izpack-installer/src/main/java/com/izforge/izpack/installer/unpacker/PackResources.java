package com.izforge.izpack.installer.unpacker;


import java.io.InputStream;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceInterruptedException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;

/**
 * Provides access to installation packs.
 *
 * @author Tim Anderson
 */
public interface PackResources
{

    /**
     * Returns the stream to a pack.
     *
     * @param name the pack name
     * @return a stream to the pack
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     * @throws ResourceException            for any other resource error
     */
    InputStream getPackStream(String name);

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     * @throws ResourceException            for any other resource error
     */
    InputStream getInputStream(String name);
}