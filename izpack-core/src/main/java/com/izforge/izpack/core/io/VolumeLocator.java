package com.izforge.izpack.core.io;


import java.io.File;
import java.io.IOException;

/**
 * Locates the next volume for an {@link FileSpanningInputStream}.
 *
 * @author Tim Anderson
 */
public interface VolumeLocator
{

    /**
     * Returns the next volume.
     *
     * @param path    the expected volume path
     * @param corrupt if <tt>true</tt> the previous attempt detected a corrupt or invalid volume
     * @return the next volume
     * @throws IOException if the volume cannot be found
     */
    File getVolume(String path, boolean corrupt) throws IOException;
}