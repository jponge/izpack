package com.izforge.izpack.installer.unpacker;


import java.io.File;

import com.izforge.izpack.util.os.FileQueue;

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
     * @param queue     the file queue. May be {@code null}
     * @return a new unpacker
     */
    protected FileUnpacker createUnpacker(File sourceDir, FileQueue queue)
    {
        return new LooseFileUnpacker(sourceDir, getCancellable(), queue, getHandler());
    }

}
