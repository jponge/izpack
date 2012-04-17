package com.izforge.izpack.installer.unpacker;


import java.io.File;

/**
 * Tests the {@link LooseFileUnpacker} class.
 *
 * @author Tim Anderson
 */
public class LooseFileUnpackerTest extends AbstractFileUnpackerTest
{

    /**
     * Helper to create an unpacker.
     *
     * @param sourceDir the source directory
     * @return a new unpacker
     */
    protected FileUnpacker createUnpacker(File sourceDir)
    {
        return new LooseFileUnpacker(sourceDir, getCancellable(), getHandler(), null, getLibrarian());
    }

}
