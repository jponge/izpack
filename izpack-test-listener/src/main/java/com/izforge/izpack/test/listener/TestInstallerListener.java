package com.izforge.izpack.test.listener;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

import java.io.File;

public class TestInstallerListener implements InstallerListener
{
    /**
     * This method will be called from the unpacker before the installation of all packs will be
     * performed.
     *
     * @param idata   object containing the current installation data
     * @param npacks  number of packs which are defined for this installation
     * @param handler a handler to the current used UIProgressHandler
     */
    public void beforePacks(AutomatedInstallData idata, Integer npacks, AbstractUIProgressHandler handler) throws Exception
    {
        log("beforePacks");
    }

    /**
     * This method will be called from the unpacker before the installation of one pack will be
     * performed.
     *
     * @param pack    current pack object
     * @param i       current pack number
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        log("beforePack");
    }

    /**
     * Returns true if this listener would be informed at every file and directory installation,
     * else false. If it is true, the listener will be called two times (before and after) for every
     * action. Handle carefully, else performance problems are possible.
     *
     * @return true if this listener would be informed at every file and directory installation,
     *         else false
     */
    public boolean isFileListener()
    {
        return false;
    }

    /**
     * This method will be called from the unpacker before one directory should be created. If
     * parent directories should be created also, this method will be called for every directory
     * beginning with the base.
     *
     * @param dir current File object of the just directory which should be created
     * @param pf  corresponding PackFile object
     * @throws Exception
     */
    public void beforeDir(File dir, PackFile pf) throws Exception
    {
        log("beforeDir");
    }

    /**
     * This method will be called from the unpacker after one directory was created. If parent
     * directories should be created, this method will be called for every directory beginning with
     * the base.
     *
     * @param dir current File object of the just created directory
     * @param pf  corresponding PackFile object
     * @throws Exception
     */
    public void afterDir(File dir, PackFile pf) throws Exception
    {
        log("afterDir");
    }

    /**
     * This method will be called from the unpacker before one file should be installed.
     *
     * @param file current File object of the file which should be installed
     * @param pf   corresponding PackFile object
     * @throws Exception
     */
    public void beforeFile(File file, PackFile pf) throws Exception
    {
        log("beforeFile");
    }

    /**
     * This method will be called from the unpacker after one file was installed.
     *
     * @param file current File object of the just installed file
     * @param pf   corresponding PackFile object
     * @throws Exception
     */
    public void afterFile(File file, PackFile pf) throws Exception
    {
        log("afterFile");
    }

    /**
     * This method will be called from the unpacker after the installation of one pack was
     * performed.
     *
     * @param pack    current pack object
     * @param i       current pack number
     * @param handler a handler to the current used UIProgressHandler
     */
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        log("afterPack");
    }

    /**
     * This method will be called from the unpacker after the installation of all packs was
     * performed.
     *
     * @param idata   object containing the current installation data
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler) throws Exception
    {
        log("afterPacks");
    }

    /**
     * Called when the installer creates the listener instance, immediately
     * after the install data is parsed.
     *
     * @param data
     */
    public void afterInstallerInitialization(AutomatedInstallData data) throws Exception
    {
        log("afterInstallerInitialization");
    }

    private void log(String message)
    {
        System.out.println("TestUninstallerListener: " + message);
    }

}
