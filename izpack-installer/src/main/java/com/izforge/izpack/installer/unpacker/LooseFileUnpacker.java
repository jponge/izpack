package com.izforge.izpack.installer.unpacker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.os.FileQueue;


/**
 * An unpacker for {@link Pack#loose loose} pack files.
 *
 * @author Tim Anderson
 */
public class LooseFileUnpacker extends FileUnpacker
{

    /**
     * The absolute source directory.
     */
    private final File sourceDir;

    /**
     * The handler to warn of missing files.
     */
    private final AbstractUIHandler handler;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(LooseFileUnpacker.class.getName());

    /**
     * Constructs a <tt>LooseFileUnpacker</tt>.
     *
     * @param sourceDir   the absolute source directory
     * @param cancellable determines if unpacking should be cancelled
     * @param queue       the file queue. May be <tt>null</tt>
     * @param platform    the current platform
     * @param librarian   the librarian
     * @param handler     the handler to warn of missing files
     */
    public LooseFileUnpacker(File sourceDir, Cancellable cancellable, FileQueue queue, Platform platform,
                             Librarian librarian, AbstractUIHandler handler)
    {
        super(cancellable, queue, platform, librarian);
        this.sourceDir = sourceDir;
        this.handler = handler;
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
        FileQueue queue = getQueue();

        // Old way of doing the job by using the (absolute) sourcepath.
        // Since this is very likely to fail and does not conform to the documentation prefer using relative
        // path's
        // pis = new FileInputStream(pf.sourcePath);

        File resolvedFile = new File(sourceDir, file.getRelativeSourcePath());
        if (!resolvedFile.exists())
        {
            // try alternative destination - the current working directory
            // user.dir is likely (depends on launcher type) the current directory of the executable or
            // jar-file...
            final File userDir = new File(System.getProperty("user.dir"));
            resolvedFile = new File(userDir, file.getRelativeSourcePath());
        }
        if (resolvedFile.exists())
        {
            InputStream stream = new FileInputStream(resolvedFile);
            // may have a different length & last modified than we had at compile time, therefore we have to
            // build a new PackFile for the copy process...
            file = new PackFile(resolvedFile.getParentFile(), resolvedFile, file.getTargetPath(),
                                file.osConstraints(), file.override(), file.overrideRenameTo(),
                                file.blockable(), file.getAdditionals());

            queue = copy(file, stream, target);
        }
        else
        {
            // file not found. Since this file was loosely bundled, continue with the installation.
            logger.warning("Could not find loosely bundled file: " + file.getRelativeSourcePath());
            if (handler.emitWarning("File not found", "Could not find loosely bundled file: "
                    + file.getRelativeSourcePath()))
            {
                throw new InstallerException("Installation cancelled");
            }
        }
        return queue;
    }
}
