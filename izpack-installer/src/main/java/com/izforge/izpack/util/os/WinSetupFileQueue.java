package com.izforge.izpack.util.os;

import com.izforge.izpack.util.Debug;

import java.io.File;
import java.io.IOException;

public class WinSetupFileQueue extends WinSetupAPIBase
{
    private int reboot = 0;

    /**
     * The Windows handle (HSPFILEQ) of the opened file queue.
     */
    private int /* HSPFILEQ */handle = INVALID_HANDLE_VALUE;

    /**
     * Creates a new file queue which uses the default setup callback handler from the Windows Setup
     * API.
     */
    public WinSetupFileQueue() throws Exception
    {
        this(null);
    }

    /**
     * Creates a new file queue and defines a Java callback handler for it that is used instead of
     * the default setup callback handler from the Windows Setup API.
     *
     * @param handler Java callback handler
     */
    public WinSetupFileQueue(WinSetupQueueCallbackInterface handler) throws Exception
    {
        super();
        this.handle = SetupOpenFileQueue((Object) handler);
    }

    /**
     * Places an individual file copy operation on a setup file queue.
     *
     * @param sourcefile Copy source file
     * @param targetfile Copy target file
     * @param style      A bitwise 'or-ed' combination of copy styles
     */
    protected void addCopy(File sourcefile, File targetfile, int /* DWORD */copyStyle) throws IOException
    {
        SetupQueueCopy(this.handle, sourcefile.getParent(), null, sourcefile.getName(), null, null,
                targetfile.getParent(), targetfile.getName(), copyStyle);
    }

    /**
     * Places an individual file copy operation on a setup file queue.
     *
     * @param sourcefile Copy source file
     * @param targetfile Copy target file
     */
    public void addCopy(File sourcefile, File targetfile) throws IOException
    {
        addCopy(sourcefile, targetfile, false);
    }

    /**
     * Places an individual file copy operation on a setup file queue.
     *
     * @param sourcefile Copy source file
     * @param targetfile Copy target file
     * @param forceInUse Whether to force target-in-use behavior
     */
    public void addCopy(File sourcefile, File targetfile, boolean forceInUse) throws IOException
    {
        int style = 0 /*SP_COPY_IN_USE_NEEDS_REBOOT*/;
        if (forceInUse)
        {
            style |= SP_COPY_FORCE_IN_USE;
        }
        addCopy(sourcefile, targetfile, style);
    }

    /**
     * Places an individual file delete operation on a setup file queue.
     *
     * @param file File to delete.
     */
    public void addDelete(File file) throws IOException
    {
        SetupQueueDelete(this.handle, file.getParent(), file.getName());
    }

    /**
     * Places an individual file rename operation on a setup file queue. Note: The target file must
     * not exist, otherwise committing the queue will fail. For moving a file to an existing target
     * use addMove.
     *
     * @param sourcefile Rename source file
     * @param targetfile Rename target file
     */
    public void addRename(File sourcefile, File targetfile) throws IOException
    {
        SetupQueueRename(this.handle, sourcefile.getParent(), sourcefile.getName(), targetfile
                .getParent(), targetfile.getName());
    }

    /**
     * Places an individual file move operation on a setup file queue. This is done by a copy/delete
     * operation since we want to be sure whether the move really happened.
     *
     * @param sourcefile Move source file
     * @param targetfile Move target file
     */
    public void addMove(File sourcefile, File targetfile) throws IOException
    {
        addCopy(sourcefile, targetfile, false);
    }

    /**
     * Places an individual file move operation on a setup file queue. This is done by a copy/delete
     * operation since we want to be sure whether the move really happened.
     *
     * @param sourcefile Move source file
     * @param targetfile Move target file
     * @param forceInUse Whether to force target-in-use behavior
     */
    public void addMove(File sourcefile, File targetfile, boolean forceInUse) throws IOException
    {
        int style = SP_COPY_DELETESOURCE /* | SP_COPY_IN_USE_NEEDS_REBOOT*/;
        if (forceInUse)
        {
            style |= SP_COPY_FORCE_IN_USE;
        }
        addCopy(sourcefile, targetfile, style);
    }

    /**
     * Commits the enqueued operations in the file queue.
     */
    public boolean commit() throws Exception
    {
        boolean result = SetupCommitFileQueue(this.handle);

        if (result)
        {
            reboot = SetupPromptReboot(this.handle, true);

            if ((reboot & SPFILEQ_FILE_IN_USE) != 0)
            {
                Debug.log("There are file operations pending");
            }
            if ((reboot & SPFILEQ_REBOOT_RECOMMENDED) != 0)
            {
                Debug.log("System reboot is recommended");
            }
            if ((reboot & SPFILEQ_REBOOT_IN_PROGRESS) != 0)
            {
                Debug.log("System shutdown is already in progress");
            }
        }

        return result;
    }

    /**
     * Closes the file queue.
     */
    public void close()
    {
        SetupCloseFileQueue(this.handle);
        this.handle = INVALID_HANDLE_VALUE;
    }


    /**
     * Check whether reboot is necessary to apply committed changes.
     * Valid only after committing the file queue, otherwise always false.
     *
     * @return true - if reboot is necessary to apply committed changes
     */
    public boolean isRebootNecessary()
    {
        return reboot > 0;
    }

}
