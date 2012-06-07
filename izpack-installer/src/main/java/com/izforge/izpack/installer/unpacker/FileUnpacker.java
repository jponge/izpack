package com.izforge.izpack.installer.unpacker;

import static com.izforge.izpack.util.Platform.Name.WINDOWS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.FileQueue;
import com.izforge.izpack.util.os.FileQueueMove;


/**
 * Unpacks a file from a pack.
 * <p/>
 * This manages queuing files that are blocked.
 *
 * @author Tim Anderson
 */
public abstract class FileUnpacker
{

    /**
     * Determines if unpacking should be cancelled.
     */
    private final Cancellable cancellable;

    /**
     * The current platform.
     */
    private final Platform platform;

    /**
     * The librarian.
     */
    private final Librarian librarian;

    /**
     * The target.
     */
    private File target;

    /**
     * Temporary target, used if the target file is blockable.
     */
    private File tmpTarget;

    /**
     * The file queue. If <tt>null</tt>, and queuing is required, one will be created.
     */
    private FileQueue queue;

    /**
     * Determines if the file was queued.
     */
    private boolean queued;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FileUnpacker.class.getName());


    /**
     * Constructs a <tt>FileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param queue       the file queue. May be <tt>null</tt>
     * @param platform    the current platform
     * @param librarian   the librarian
     */
    public FileUnpacker(Cancellable cancellable, FileQueue queue, Platform platform, Librarian librarian)
    {
        this.cancellable = cancellable;
        this.queue = queue;
        this.librarian = librarian;
        this.platform = platform;
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
    public abstract FileQueue unpack(PackFile file, ObjectInputStream packInputStream, File target)
            throws IOException, InstallerException;

    /**
     * Determines if the file was queued.
     *
     * @return <tt>true</tt> if the file was queued
     */
    public boolean isQueued()
    {
        return queued;
    }

    /**
     * Returns the file queue.
     *
     * @return the file queue. May be <tt>null</tt>
     */
    protected FileQueue getQueue()
    {
        return queue;
    }

    /**
     * Copies an input stream to a target, setting its timestamp to that of the pack file.
     * <p/>
     * If the target is {@link #isBlockable a blockable file}, then a temporary file will be created, and the
     * file queued.
     *
     * @param file   the pack file
     * @param in     the pack file stream
     * @param target the file to write to
     * @return the file queue. May be <tt>null</tt>
     * @throws IOException for any I/O error
     */
    protected FileQueue copy(PackFile file, InputStream in, File target) throws IOException
    {
        OutputStream out = getTarget(file, target);
        try
        {
            byte[] buffer = new byte[5120];
            long bytesCopied = 0;
            while (bytesCopied < file.length())
            {
                if (cancellable.isCancelled())
                {
                    // operation cancelled
                    return queue;
                }
                bytesCopied = copy(file, buffer, in, out, bytesCopied);
            }
        }
        finally
        {
            FileUtils.close(out);
        }
        return postCopy(file);
    }

    /**
     * Invoked after copying is complete to set the last modified timestamp, and queue blockable files.
     *
     * @param file the pack file meta-data
     * @return the file queue. May be <tt>null</tt>
     * @throws IOException for any I/O error
     */
    protected FileQueue postCopy(PackFile file) throws IOException
    {
        setLastModified(file);

        if (isBlockable(file))
        {
            queue();
        }
        return getQueue();
    }

    /**
     * Copies from the input stream to the output stream.
     *
     * @param file        the pack file
     * @param buffer      the buffer to use
     * @param in          the stream to read from
     * @param out         the stream to write to
     * @param bytesCopied the current no. of bytes copied
     * @return the bytes copied
     * @throws IOException for any I/O error
     */
    protected long copy(PackFile file, byte[] buffer, InputStream in, OutputStream out, long bytesCopied)
            throws IOException
    {
        int maxBytes = (int) Math.min(file.length() - bytesCopied, buffer.length);
        int read = read(buffer, in, maxBytes);
        if (read == -1)
        {
            throw new IOException("Unexpected end of stream (installer corrupted?)");
        }
        out.write(buffer, 0, read);
        bytesCopied += read;

        return bytesCopied;
    }

    /**
     * Reads up to <tt>maxBytes</tt> bytes to the specified buffer.
     *
     * @param buffer   the buffer
     * @param in       the input stream
     * @param maxBytes the maximum no. of bytes to read
     * @return the no. of bytes read
     * @throws IOException for any I/O error
     */
    protected int read(byte[] buffer, InputStream in, int maxBytes) throws IOException
    {
        return in.read(buffer, 0, maxBytes);
    }

    /**
     * Returns a stream to the target file.
     * <p/>
     * If the target file {@link #isBlockable is blockable}, then a temporary file will be created, and a stream to
     * this returned instead.
     *
     * @param file   the pack file meta-data
     * @param target the requested target
     * @return a stream to the actual target
     * @throws IOException
     */
    protected OutputStream getTarget(PackFile file, File target) throws IOException
    {
        this.target = target;
        OutputStream result;
        if (isBlockable(file))
        {
            // If target file might be blocked the output file must first refer to a temporary file, because
            // Windows Setup API doesn't work on streams but only on physical files
            tmpTarget = File.createTempFile("__FQ__", null, target.getParentFile());
            result = new FileOutputStream(tmpTarget);
        }
        else
        {
            result = new FileOutputStream(target);
        }
        return result;
    }

    /**
     * Sets the last-modified timestamp of a file from the pack-file meta-data.
     *
     * @param file the pack file meta-data
     */
    protected void setLastModified(PackFile file)
    {
        // Set file modification time if specified
        if (file.lastModified() >= 0)
        {
            File f = (tmpTarget != null) ? tmpTarget : target;
            if (!f.setLastModified(file.lastModified()))
            {
                logger.warning("Failed to set last modified timestamp for: " + target);
            }
        }
    }

    /**
     * Determines if a pack file is blockable.
     * <p/>
     * Blockable files must be queued using {@link #queue()}.
     *
     * @param file the pack file
     * @return <tt>true</tt> if the file is blockable, otherwise <tt>false</tt>
     */
    private boolean isBlockable(PackFile file)
    {
        return (file.blockable() != Blockable.BLOCKABLE_NONE) && platform.isA(WINDOWS);
    }

    /**
     * Queues the target file.
     *
     * @throws IOException
     */
    private void queue() throws IOException
    {
        if (queue == null)
        {
            queue = new FileQueue(librarian);
        }

        FileQueueMove move = new FileQueueMove(tmpTarget, target);
        move.setForceInUse(true);
        move.setOverwrite(true);
        queue.add(move);
        logger.fine(tmpTarget.getAbsolutePath() + " -> " + target.getAbsolutePath()
                            + " added to file queue for being copied after reboot");
        // The temporary file must not be deleted until the file queue will be committed
        tmpTarget.deleteOnExit();
        queued = true;
    }

}
