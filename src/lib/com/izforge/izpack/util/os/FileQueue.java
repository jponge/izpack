package com.izforge.izpack.util.os;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


public class FileQueue
{

    private Vector<FileQueueOperation> operations = new Vector<FileQueueOperation>();

    protected WinSetupFileQueue filequeue;


    /**
     * Add a file queue operation.
     *
     * @param op the operation (copy/move/delete)
     */
    public void add(FileQueueOperation op)
    {
        operations.addElement(op);
    }

    public void execute() throws Exception
    {
        WinSetupDefaultCallbackHandler handler = new WinSetupDefaultCallbackHandler();
        try
        {
            filequeue = new WinSetupFileQueue(handler);
        }
        catch (IOException ioe)
        {
            throw new IOException("Failed to open a file queue due to " + ioe.getMessage());
        }
        try
        {
            Enumeration<FileQueueOperation> ops = operations.elements();
            while (ops.hasMoreElements())
            {
                Object op = ops.nextElement();
                ((FileQueueOperation) op).addTo(filequeue);
            }
            filequeue.commit();

            List<SystemErrorException> exceptions = handler.getExceptions();
            if (exceptions != null)
            {
                StringBuffer buf = new StringBuffer();
                buf.append(
                        "The following system errors occured during committing the file queue:\n");
                Iterator<SystemErrorException> it = exceptions.iterator();
                while (it.hasNext())
                {
                    buf.append('\t');
                    buf.append(((SystemErrorException) it.next()).toString());
                    buf.append('\n');
                }
                throw new IOException(buf.toString());
            }

        }
        catch (IOException ioe)
        {
            throw new IOException("File queue operation failed due to " + ioe.getMessage());
        }
        finally
        {
            filequeue.close();
        }
    }

    public boolean isRebootNecessary()
    {
        if (filequeue != null)
        {
            return filequeue.isRebootNecessary();
        }

        return false;
    }

}
