package com.izforge.izpack.util.os;

import java.io.File;
import java.util.List;
import java.util.Vector;

public class WinSetupDefaultCallbackHandler implements WinSetupQueueCallbackInterface
{

    private List<SystemErrorException> exceptions;

    public int handleNeedMedia(String tagfile, String description, String sourcePath,
                               String sourceFile)
    {
        File f = new File(sourcePath, sourceFile);
        if (f.exists() && f.canRead())
        {
            return FILEOP_RETRY;
        }
        addException("Source file " + f.getPath() + " not found, aborting.");
        return FILEOP_ABORT;
    }

    public int handleCopyError(String source, String target, int errCode, String errMsg)
    {
        addException(errCode, "Aborting copying " + source + " -> " + target + ": " + errMsg);
        return FILEOP_ABORT;
    }

    public int handleDeleteError(String target, int errCode, String errMsg)
    {
        addException(errCode, "Skipping deleting " + target + ": " + errMsg);
        // Skip any file delete errors
        return FILEOP_SKIP;
    }

    public int handleRenameError(String source, String target, int errCode, String errMsg)
    {
        addException(errCode, "Aborting renaming " + source + " -> " + target + ": " + errMsg);
        return FILEOP_ABORT;
    }

    public List<SystemErrorException> getExceptions()
    {
        return exceptions;
    }

    private void addException(String errMsg)
    {
        addException(0, errMsg);
    }

    private void addException(int errCode, String errMsg)
    {
        if (exceptions == null)
        {
            exceptions = new Vector<SystemErrorException>();
        }
        exceptions.add(new SystemErrorException(errCode, errMsg));
    }

}
