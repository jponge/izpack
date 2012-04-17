package com.izforge.izpack.installer.unpacker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.FileQueue;


/**
 * A file unpacker for pack200 files.
 *
 * @author Tim Anderson
 */
class Pack200FileUnpacker extends FileUnpacker
{
    /**
     * The resource manager.
     */
    private final ResourceManager resources;

    /**
     * The unpacker.
     */
    private final Pack200.Unpacker unpacker;

    /**
     * Constructs a <tt>Pack200FileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param handler     the handler
     * @param resources   the resources
     * @param unpacker    the unpacker
     * @param queue       the file queue. May be <tt>null</tt>
     * @param platform    the current platform
     * @param librarian   the librarian
     */
    public Pack200FileUnpacker(Cancellable cancellable, AbstractUIProgressHandler handler,
                               ResourceManager resources, Pack200.Unpacker unpacker, FileQueue queue,
                               Platform platform, Librarian librarian)
    {
        super(cancellable, handler, queue, platform, librarian);
        this.resources = resources;
        this.unpacker = unpacker;
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
        int key = packInputStream.readInt();
        InputStream in = null;
        OutputStream out = null;
        JarOutputStream jarOut = null;

        try
        {
            in = resources.getInputStream("packs/pack200-" + key);
            out = getTarget(file, target);
            jarOut = new JarOutputStream(out);
            unpacker.unpack(in, jarOut);
            jarOut.close();
        }
        finally
        {
            FileUtils.close(in);
            FileUtils.close(out);
            FileUtils.close(jarOut);
        }

        return postCopy(file);
    }

}
