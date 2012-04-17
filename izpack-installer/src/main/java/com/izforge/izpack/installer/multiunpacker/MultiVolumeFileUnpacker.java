package com.izforge.izpack.installer.multiunpacker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.XPackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.core.io.CorruptVolumeException;
import com.izforge.izpack.core.io.FileSpanningInputStream;
import com.izforge.izpack.core.io.VolumeNotFoundException;
import com.izforge.izpack.installer.unpacker.Cancellable;
import com.izforge.izpack.installer.unpacker.FileUnpacker;
import com.izforge.izpack.installer.unpacker.IMultiVolumeUnpackerHelper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.os.FileQueue;


/**
 * A multi-volume file unpacker.
 *
 * @author Tim Anderson
 */
public class MultiVolumeFileUnpacker extends FileUnpacker
{
    /**
     * The volumes.
     */
    private final FileSpanningInputStream volumes;

    /**
     * Unpacking helper.
     */
    private final IMultiVolumeUnpackerHelper helper;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumeFileUnpacker.class.getName());

    /**
     * Constructs a <tt>MultiVolumeFileUnpacker</tt>.
     *
     * @param volumes     the input stream
     * @param helper      unpacker helper
     * @param cancellable determines if unpacking should be cancelled
     * @param handler     the handler
     * @param queue       the file queue. May be <tt>null</tt>
     * @param librarian   the librarian
     */
    public MultiVolumeFileUnpacker(FileSpanningInputStream volumes, IMultiVolumeUnpackerHelper helper,
                                   Cancellable cancellable, AbstractUIProgressHandler handler, FileQueue queue,
                                   Librarian librarian)
    {
        super(cancellable, handler, queue, librarian);
        this.volumes = volumes;
        this.helper = helper;
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
        // read in the position of this file
        long position = ((XPackFile) file).getArchivefileposition();

        while (volumes.getFilepointer() < position)
        {
            // need to skip to the correct position
            logger.fine("Skipping bytes to get to file " + target.getName()
                                + " (" + volumes.getFilepointer() + "<" + position
                                + ") target is: " + (position - volumes.getFilepointer()));
            try
            {
                skip(position - volumes.getFilepointer());
                break;
            }
            catch (VolumeNotFoundException exception)
            {
                File nextMedia = helper.enterNextMediaMessage(exception.getVolumename());
                volumes.setVolumename(nextMedia.getAbsolutePath());
            }
            catch (CorruptVolumeException exception)
            {
                logger.fine("corrupt media found. magic number is not correct");
                File nextMedia = helper.enterNextMediaMessage(exception.getVolumename(), true);
                volumes.setVolumename(nextMedia.getAbsolutePath());
            }
        }

        if (volumes.getFilepointer() > position)
        {
            logger.fine("Error, can't access file in pack.");
        }

        return copy(file, volumes, target);
    }

    /**
     * Reads up to <tt>maxBytes</tt> bytes to the specified buffer.
     * <p/>
     * This prompts to enter the next volume if the read fails.
     *
     * @param buffer   the buffer
     * @param in       the input stream
     * @param maxBytes the maximum no. of bytes to read
     * @return the no. of bytes read
     * @throws IOException for any I/O error
     */
    @Override
    protected int read(byte[] buffer, InputStream in, int maxBytes) throws IOException
    {
        try
        {
            return in.read(buffer, 0, maxBytes);
        }
        catch (VolumeNotFoundException exception)
        {
            File nextMedia = helper.enterNextMediaMessage(exception.getVolumename());
            volumes.setVolumename(nextMedia.getAbsolutePath());
        }
        catch (CorruptVolumeException exception)
        {
            logger.fine("Corrupt media found. Magic number is not correct");
            File nextMedia = helper.enterNextMediaMessage(exception.getVolumename(), true);
            volumes.setVolumename(nextMedia.getAbsolutePath());
        }

        return in.read(buffer, 0, maxBytes);
    }

    /**
     * Skips bytes in a stream.
     *
     * @param bytes the no. of bytes to skip
     * @throws IOException for any I/O error, or if the no. of bytes skipped doesn't match that expected
     */
    protected void skip(long bytes) throws IOException
    {
        long skipped = volumes.skip(bytes);
        if (skipped != bytes)
        {
            throw new IOException("Expected to skip: " + bytes + " in stream but skipped: " + skipped);
        }
    }
}
