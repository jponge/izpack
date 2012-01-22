package com.izforge.izpack.test.listener;

import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

import java.io.File;
import java.util.List;

public class TestUninstallerListener implements UninstallerListener
{
    /**
     * This method will be called from the destroyer before the given files will be deleted.
     *
     * @param files   all files which should be deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        log("beforeDeletion");
    }

    /**
     * Returns true if this listener would be informed at every delete operation, else false. If it
     * is true, the listener will be called two times (before and after) of every action. Handle
     * carefully, else performance problems are possible.
     *
     * @return true if this listener would be informed at every delete operation, else false
     */
    public boolean isFileListener()
    {
        return false;
    }

    /**
     * This method will be called from the destroyer before the given file will be deleted.
     *
     * @param file    file which should be deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        log("beforeDelete");
    }

    /**
     * This method will be called from the destroyer after the given file was deleted.
     *
     * @param file    file which was just deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        log("afterDelete");
    }

    /**
     * This method will be called from the destroyer after the given files are deleted.
     *
     * @param files   all files which where deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        log("afterDeletion");
    }

    private void log(String message)
    {
        System.out.println("TestUninstallerListener: " + message);
    }

}
