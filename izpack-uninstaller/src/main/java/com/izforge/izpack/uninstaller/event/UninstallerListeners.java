package com.izforge.izpack.uninstaller.event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;


/**
 * A container for {@link UninstallerListener}s that supports notifying each registered listener.
 *
 * @author Tim Anderson
 */
public class UninstallerListeners implements UninstallerListener
{

    /**
     * The listeners.
     */
    private final List<UninstallerListener> listeners = new ArrayList<UninstallerListener>();

    /**
     * Determines if any of the listeners should be notified of file and directory events.
     */
    private boolean fileListener;

    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void add(UninstallerListener listener)
    {
        listeners.add(listener);
        if (!fileListener && listener.isFileListener())
        {
            fileListener = true;
        }
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files   all files which should be deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        for (UninstallerListener listener : listeners)
        {
            listener.beforeDeletion(files, handler);
        }
    }

    /**
     * Determines if the listener should be notified of every file deletion.
     *
     * @return <tt>true</tt> if this listener would be informed at every delete operation, else <tt>false</tt>
     */
    @Override
    public boolean isFileListener()
    {
        return fileListener;
    }

    /**
     * Invoked before a file is deleted.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file    the file which will be deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        if (fileListener)
        {
            for (UninstallerListener listener : listeners)
            {
                if (listener.isFileListener())
                {
                    listener.beforeDelete(file, handler);
                }
            }
        }
    }

    /**
     * Invoked after a file is deleted.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file    the file which was deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        if (fileListener)
        {
            for (UninstallerListener listener : listeners)
            {
                if (listener.isFileListener())
                {
                    listener.afterDelete(file, handler);
                }
            }
        }
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files   the files which where deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        for (UninstallerListener listener : listeners)
        {
            listener.afterDeletion(files, handler);
        }
    }
}
