package com.izforge.izpack.installer.unpacker;


import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Factory for {@link FileQueue} instances, if queuing is supported by the current platform.
 *
 * @author Tim Anderson
 */
public class FileQueueFactory
{

    /**
     * Determines if the current platform supports queuing.
     */
    private final boolean supportsQueue;

    /**
     * The librarian.
     */
    private final Librarian librarian;


    /**
     * Constructs a {@code FileQueueFactory}.
     *
     * @param platform  the current platform
     * @param librarian the librarian
     */
    public FileQueueFactory(Platform platform, Librarian librarian)
    {
        supportsQueue = platform.isA(Platform.Name.WINDOWS);
        this.librarian = librarian;
    }

    /**
     * Determines if queuing is supported.
     *
     * @return {@code true} if queuing is supported
     */
    public boolean isSupported()
    {
        return supportsQueue;
    }

    /**
     * Creates a new {@link FileQueue}.
     *
     * @return a new queue or {@code null} if queuing is not supported
     */
    public FileQueue create()
    {
        return supportsQueue ? new FileQueue(librarian) : null;
    }

}
