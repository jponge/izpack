package com.izforge.izpack.util.os;

import com.izforge.izpack.util.Debug;

import java.io.File;
import java.io.IOException;

/*
 * File queue delete operation (Windows Setup API)
 */
public class FileQueueDelete implements FileQueueOperation
{

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
                    Debug.log("Directory " + file.getAbsolutePath()
                            + " cannot be removed in a file queue");
                }
                else
                {
                    Debug.log("Enqueueing deletion of " + file.getAbsolutePath());
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
                Debug.log("Could not find file " + file.getAbsolutePath() + " to delete.");
            }
        }
    }
}
