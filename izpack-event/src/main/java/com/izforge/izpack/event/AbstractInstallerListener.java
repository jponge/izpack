package com.izforge.izpack.event;

import java.io.File;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;


/**
 * Abstract implementation of {@link InstallerListener}.
 * <p/>
 * This provides no-op versions of each of the methods, to simplify implementation of listeners that only need
 * some methods.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInstallerListener implements InstallerListener
{

    /**
     * Invoked when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @param data the installation data
     * @throws Exception for any error
     */
    @Override
    public void afterInstallerInitialization(AutomatedInstallData data) throws Exception
    {
    }

    /**
     * Invoked before packs are installed.
     *
     * @param data    the installation data
     * @param packs   the number of packs which are defined for this installation
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void beforePacks(AutomatedInstallData data, Integer packs, AbstractUIProgressHandler handler)
            throws Exception
    {
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return <tt>false</tt>
     */
    @Override
    public boolean isFileListener()
    {
        return false;
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     */
    @Override
    public void beforeDir(File dir, PackFile packFile) throws Exception
    {
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     */
    @Override
    public void afterDir(File dir, PackFile packFile) throws Exception
    {
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    @Override
    public void beforeFile(File file, PackFile packFile) throws Exception
    {
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     */
    @Override
    public void afterFile(File file, PackFile packFile) throws Exception
    {
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
    }

    /**
     * Invoked after packs are installed.
     *
     * @param data    the installation data
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void afterPacks(AutomatedInstallData data, AbstractUIProgressHandler handler) throws Exception
    {
    }
}
