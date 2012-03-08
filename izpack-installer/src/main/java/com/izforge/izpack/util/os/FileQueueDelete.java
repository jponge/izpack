package com.izforge.izpack.util.os;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/*
 * File queue delete operation (Windows Setup API)
 */
public class FileQueueDelete implements FileQueueOperation
{
    private static final Logger logger = Logger.getLogger(FileQueueDelete.class.getName());

    protected File file;

    public FileQueueDelete(File file)
    {
        this.file = file;
    }

    public FileQueueDelete(String file)
    {
        this.file = new File(file);
    }

    public void addTo(WinSetupFileQueue fileQueue) throws IOException
    {
        if (file != null)
        {
            if (file.exists())
            {
                if (file.isDirectory())
                {
                    logger.warning("Directory " + file.getAbsolutePath()
                            + " cannot be removed in a file queue");
                }
                else
                {
                    logger.fine("Enqueueing deletion of " + file.getAbsolutePath());
                    try
                    {
                        fileQueue.addDelete(file);
                    }
                    catch (IOException ioe)
                    {
                        String msg = "Failed to enqueue deletion of " + file + " due to "
                                + ioe.getMessage();
                        throw new IOException(msg);
                    }
                }
            }
            else
            {
                logger.warning("Could not find file " + file.getAbsolutePath() + " to delete.");
            }
        }
    }
}
