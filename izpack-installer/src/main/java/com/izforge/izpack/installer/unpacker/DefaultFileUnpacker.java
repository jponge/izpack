package com.izforge.izpack.installer.unpacker;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Default file unpacker.
 *
 * @author Tim Anderson
 */
public class DefaultFileUnpacker extends FileUnpacker
{

    /**
     * Constructs a <tt>DefaultFileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param handler     the handler
     * @param queue       the file queue. May be <tt>null</tt>
     * @param platform    the current platform
     * @param librarian   the librarian
     */
    public DefaultFileUnpacker(Cancellable cancellable, AbstractUIProgressHandler handler, FileQueue queue,
                               Platform platform, Librarian librarian)
    {
        super(cancellable, handler, queue, platform, librarian);
    }

    /**
     * Unpacks a pack file.
     *
     * @param file            the pack file meta-data
     * @param packInputStream the pack input stream
     * @param target          the target
     * @return the file queue. May be <tt>null</tt>
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer exception
     */
    @Override
    public FileQueue unpack(PackFile file, ObjectInputStream packInputStream, File target)
            throws IOException, InstallerException
    {
        return copy(file, packInputStream, target);
    }
}
