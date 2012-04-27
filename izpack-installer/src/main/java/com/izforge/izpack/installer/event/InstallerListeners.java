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
    private List<InstallerListener> listeners = new ArrayList<InstallerListener>();

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
     * @param installData the installation data
     * @param packs       number of packs which are defined for this installation
     * @param handler     the UI progress handler
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforePacks(AutomatedInstallData installData, Integer packs, AbstractUIProgressHandler handler)
            throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.beforePacks(installData, packs, handler);
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
     *
     * @param dir the directory
     * @param pf  corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforeDir(File dir, PackFile pf) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            if (listener.isFileListener())
            {
                listener.beforeDir(dir, pf);
            }
        }
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir the directory
     * @param pf  corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void afterDir(File dir, PackFile pf) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            if (listener.isFileListener())
            {
                listener.afterDir(dir, pf);
            }
        }
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file the file
     * @param pf   corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforeFile(File file, PackFile pf) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            if (listener.isFileListener())
            {
                listener.beforeFile(file, pf);
            }
        }
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file the file
     * @param pf   corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void afterFile(File file, PackFile pf) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            if (listener.isFileListener())
            {
                listener.afterFile(file, pf);
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
     * @param installData the installation data
     * @param handler     the UI progress handler
     * @throws Exception if a listener throws an exception
     */
    @Override
    public void afterPacks(AutomatedInstallData installData, AbstractUIProgressHandler handler) throws Exception
    {
        for (InstallerListener listener : listeners)
        {
            listener.afterPacks(installData, handler);
        }
    }

    /**
     * Called when the installer creates the listener instance, immediately after the install data is parsed.
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
