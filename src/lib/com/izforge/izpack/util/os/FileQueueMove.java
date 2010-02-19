package com.izforge.izpack.util.os;

import com.izforge.izpack.util.Debug;

import java.io.File;
import java.io.IOException;


/**
 * File queue move operation (Windows Setup API)
 */
public class FileQueueMove extends FileQueueCopy
{

    public FileQueueMove(File fromFile, File toFile)
    {
        this(fromFile, toFile, false);
    }

    public FileQueueMove(File fromFile, File toFile, boolean forceInUse)
    {
        super(fromFile, toFile, true, forceInUse);
    }

    @Override
    public void addTo(WinSetupFileQueue filequeue) throws IOException
    {
        try
        {
            Debug.log("Enqueueing moving " + fromFile + " to " + toFile
                    + " (0x" + Integer.toHexString(copyStyle) + ")");
            filequeue.addMove(fromFile, toFile);
        }
        catch (IOException e)
        {
            throw new IOException(
                    "Failed to enqueue moving " + fromFile + " to " + toFile
                            + " due to " + e.getMessage());
        }
    }

}
