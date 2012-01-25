package com.izforge.izpack.test.listener;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

import java.io.File;

/**
 * An {@link InstallerListener} that tracks invocations for testing purposes.
 *
 * @author Tim Anderson
 */
public class TestInstallerListener implements InstallerListener
{

    /**
     * Tracks invocations of {@link #afterInstallerInitialization}.
     */
    private int afterInstallerInitializationCount;

    /**
     * Tracks invocations of {@link #beforePacks}.
     */
    private int beforePacksCount;

    /**
     * Tracks invocations of {@link #afterPacks}.
     */
    private int afterPacksCount;

    /**
     * Tracks invocations of {@link #beforePack}.
     */
    private int beforePackCount;

    /**
     * Tracks invocations of {@link #afterPack}.
     */
    private int afterPackCount;

    /**
     * Tracks invocations of {@link #beforeDir}.
     */
    private int beforeDirCount;

    /**
     * Tracks invocations of {@link #afterDir}.
     */
    private int afterDirCount;

    /**
     * Tracks invocations of {@link #beforeFile}.
     */
    private int beforeFileCount;

    /**
     * Tracks invocations of {@link #afterFile}.
     */
    private int afterFileCount;


    /**
     * Returns the no. of invocations of {@link #afterInstallerInitializationCount}.
     *
     * @return the no. of invocations
     */
    public int getAfterInstallerInitializationCount()
    {
        return afterInstallerInitializationCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforePacksCount}.
     *
     * @return the no. of invocations
     */
    public int getBeforePacksCount()
    {
        return beforePacksCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterPacks}.
     *
     * @return the no. of invocations
     */
    public int getAfterPacksCount()
    {
        return afterPacksCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforePack}.
     *
     * @return the no. of invocations
     */
    public int getBeforePackCount()
    {
        return beforePackCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterPack}.
     *
     * @return the no. of invocations
     */
    public int getAfterPackCount()
    {
        return afterPackCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforeDir}.
     *
     * @return the no. of invocations
     */
    public int getBeforeDirCount()
    {
        return beforeDirCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterDir}.
     *
     * @return the no. of invocations
     */
    public int getAfterDirCount()
    {
        return afterDirCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforeFile}.
     *
     * @return the no. of invocations
     */
    public int getBeforeFileCount()
    {
        return beforeFileCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterFile}.
     *
     * @return the no. of invocations
     */
    public int getAfterFileCount()
    {
        return afterFileCount;
    }


    /**
     * Called when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @param data object containing the current installation data
     */
    public void afterInstallerInitialization(AutomatedInstallData data)
    {
        ++afterInstallerInitializationCount;
        log("afterInstallerInitialization");
    }

    /**
     * Invoked before packs are installed.
     *
     * @param data    the installation data
     * @param packs   number of packs which are defined for this installation
     * @param handler the UI progress handler
     */
    public void beforePacks(AutomatedInstallData data, Integer packs, AbstractUIProgressHandler handler)
    {
        ++beforePacksCount;
        log("beforePacks: packs=" + packs);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param installData the install data
     * @param handler     the UI progress handler
     */
    public void afterPacks(AutomatedInstallData installData, AbstractUIProgressHandler handler)
    {
        ++afterPacksCount;
        log("afterPacks");
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     */
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    {
        ++beforePackCount;
        log("beforePack: pack=" + pack);
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack    current pack object
     * @param i       current pack number
     * @param handler the UI progress handler
     */
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    {
        ++afterPackCount;
        log("afterPack: pack=" + pack);
    }

    /**
     * Returns true if this listener would be informed at every file and directory installation,
     * else false.
     *
     * @return <tt>true</tt>
     */
    public boolean isFileListener()
    {
        return true;
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir the directory
     * @param pf  corresponding pack file
     */
    public void beforeDir(File dir, PackFile pf)
    {
        ++beforeDirCount;
        log("beforeDir: dir=" + dir);
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir the directory
     * @param pf  corresponding pack file
     */
    public void afterDir(File dir, PackFile pf)
    {
        ++afterDirCount;
        log("afterDir: dir=" + dir);
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file the file
     * @param pf   corresponding pack file
     */
    public void beforeFile(File file, PackFile pf)
    {
        ++beforeFileCount;
        log("beforeFile: file=" + file);
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file the file
     * @param pf   corresponding pack file
     */
    public void afterFile(File file, PackFile pf)
    {
        ++afterFileCount;
        log("afterFile: file=" + file);
    }

    /**
     * Logs a message.
     *
     * @param message the message
     */
    private void log(String message)
    {
        System.out.println("TestInstallerListener: " + message);
    }

}
