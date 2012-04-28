package com.izforge.izpack.installer.event;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;


/**
 * A container for {@link InstallerListener}s that supports notifying each registered listener.
 *
 * @author Tim Anderson
 */
public class InstallerListeners implements InstallerListener
{
    /**
     * The listeners.
     */
    private final List<InstallerListener> listeners = new ArrayList<InstallerListener>();

    /**
     * Determines if any of the listeners should be notified of file and directory events.
     */
    private boolean fileListener;


    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void add(InstallerListener listener)
    {
        listeners.add(listener);
        if (!fileListener && listener.isFileListener())
        {
            fileListener = true;
        }
    }

    /**
     * Returns the number of registered listeners.
     *
     * @return the number of registered listeners
     */
    public int size()
    {
        return listeners.size();
    }

    /**
     * Determines if there are no registered listeners.
     *
     * @return <tt>true</tt> if there are no registered listeners
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Returns the listener at the specified index in the collection.
     *
     * @param index the index into the collection
     * @return the corresponding listener
     */
    public InstallerListener get(int index)
    {
        return listeners.get(index);
    }

    /**
     * Invoked before packs are installed.
     *
     * @param data    the installation data
     * @param packs   number of packs which are defined for this installation
     * @param handler the UI progress handler
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforePacks(AutomatedInstallData data, Integer packs, AbstractUIProgressHandler handler)
            throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.beforePacks(data, packs, handler);
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.beforePack(pack, i, handler);
        }
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise <tt>false</tt>
     */
    @Override
    public boolean isFileListener()
    {
        return fileListener;
    }

    /**
     * Invoked before a directory is created.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforeDir(File dir, PackFile packFile) throws Exception
    {
        if (fileListener)
        {
            for (InstallerListener listener : listeners)
            {
                if (listener.isFileListener())
                {
                    listener.beforeDir(dir, packFile);
                }
            }
        }
    }

    /**
     * Invoked after a directory is created.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void afterDir(File dir, PackFile packFile) throws Exception
    {
        if (fileListener)
        {
            for (InstallerListener listener : listeners)
            {
                if (listener.isFileListener())
                {
                    listener.afterDir(dir, packFile);
                }
            }
        }
    }

    /**
     * Invoked before a file is installed.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforeFile(File file, PackFile packFile) throws Exception
    {
        if (fileListener)
        {
            for (InstallerListener listener : listeners)
            {
                if (listener.isFileListener())
                {
                    listener.beforeFile(file, packFile);
                }
            }
        }
    }

    /**
     * Invoked after a file is installed.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void afterFile(File file, PackFile packFile) throws Exception
    {
        if (fileListener)
        {
            for (InstallerListener listener : listeners)
            {
                if (listener.isFileListener())
                {
                    listener.afterFile(file, packFile);
                }
            }
        }
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack    current pack object
     * @param i       current pack number
     * @param handler the UI progress handler
     * @throws Exception if a listener throws an exception
     */
    @Override
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.afterPack(pack, i, handler);
        }
    }

    /**
     * Invoked after packs are installed.
     *
     * @param data    the installation data
     * @param handler the UI progress handler
     * @throws Exception if a listener throws an exception
     */
    @Override
    public void afterPacks(AutomatedInstallData data, AbstractUIProgressHandler handler) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.afterPacks(data, handler);
        }
    }

    /**
     * Invoked when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @param installData the installation data
     * @throws Exception if a listener throws an exception
     */
    @Override
    public void afterInstallerInitialization(AutomatedInstallData installData) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.afterInstallerInitialization(installData);
        }
    }


}
