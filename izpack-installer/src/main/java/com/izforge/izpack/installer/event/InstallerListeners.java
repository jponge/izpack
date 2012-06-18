package com.izforge.izpack.installer.event;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.FileListener;
import com.izforge.izpack.api.event.InstallListener;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.PackListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.handler.ProgressHandler;


/**
 * A container for {@link InstallerListener}s that supports notifying each registered listener.
 *
 * @author Tim Anderson
 */
public class InstallerListeners
{

    /**
     * The pack listeners.
     */
    private final List<PackListener> packListeners = new ArrayList<PackListener>();

    /**
     * The file listeners.
     */
    private final List<FileListener> fileListeners = new ArrayList<FileListener>();

    /**
     * The installer listeners.
     */
    private final List<InstallerListener> listeners = new ArrayList<InstallerListener>();

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The prompt.
     */
    private final Prompt prompt;


    /**
     * Constructs an {@code Installer:Listeners}.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
    public InstallerListeners(AutomatedInstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void add(InstallListener listener)
    {
        if (listener instanceof InstallerListener)
        {
            InstallerListener l = (InstallerListener) listener;
            packListeners.add(new PackInstallerListener(l));
            listeners.add(l);
            if (l.isFileListener())
            {
                fileListeners.add(new FileInstallerListener(l));
            }
        }
        else
        {
            if (listener instanceof PackListener)
            {
                packListeners.add((PackListener) listener);
            }
            if (listener instanceof FileListener)
            {
                fileListeners.add((FileListener) listener);
            }
        }
    }

    /**
     * Returns the number of registered listeners.
     *
     * @return the number of registered listeners
     */
    public int size()
    {
        return packListeners.size();
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
    public InstallListener get(int index)
    {
        return packListeners.get(index);
    }

    /**
     * Returns the installer listeners.
     *
     * @return the installer listeners
     */
    public List<InstallerListener> getInstallerListeners()
    {
        return listeners;
    }

    /**
     * Initialises the listeners.
     *
     * @throws IzPackException if a listener throws an exception
     */
    public void initialise()
    {
        // Initialise each listener only once. Some listeners will implement both interfaces,
        // so add to a set to remove duplicates
        Set<InstallListener> listeners = new LinkedHashSet<InstallListener>(packListeners);
        listeners.addAll(fileListeners);
        for (InstallListener listener : listeners)
        {
            // be nice if initialise() was a member of InstallListener, but doing so would break InstallerListener
            if (listener instanceof PackListener)
            {
                ((PackListener) listener).initialise();
            }
            else
            {
                ((FileListener) listener).initialise();
            }
        }
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs    the packs to install
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforePacks(List<Pack> packs, ProgressListener listener)
    {
        for (PackListener packListener : packListeners)
        {
            if (packListener instanceof PackInstallerListener) {
                ((PackInstallerListener) packListener).setProgressListener(listener);
            }
            packListener.beforePacks(packs);
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforePack(Pack pack, int i, ProgressListener listener)
    {
        for (PackListener packListener : packListeners)
        {
            if (packListener instanceof PackInstallerListener) {
                ((PackInstallerListener) packListener).setProgressListener(listener);
            }
            packListener.beforePack(pack, i);
        }
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise <tt>false</tt>
     */
    public boolean isFileListener()
    {
        return !fileListeners.isEmpty();
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeDir(File dir, PackFile packFile)
    {
        for (FileListener l : fileListeners)
        {
            l.beforeDir(dir, packFile);
        }
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @throws IzPackException if a listener throws an exception
     */
    public void afterDir(File dir, PackFile packFile)
    {
        for (FileListener l : fileListeners)
        {
            l.afterDir(dir, packFile);
        }
    }

    /**
     * Invoked before a file is installed.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeFile(File file, PackFile packFile)
    {
        for (FileListener l : fileListeners)
        {
            l.beforeFile(file, packFile);
        }
    }

    /**
     * Invoked after a file is installed.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     * @throws IzPackException if a listener throws an exception
     */
    public void afterFile(File file, PackFile packFile)
    {
        for (FileListener l : fileListeners)
        {
            l.afterFile(file, packFile);
        }
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack     current pack object
     * @param i        current pack number
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterPack(Pack pack, int i, ProgressListener listener)
    {
        for (PackListener packListener : packListeners)
        {
            if (packListener instanceof PackInstallerListener) {
                ((PackInstallerListener) packListener).setProgressListener(listener);
            }
            packListener.afterPack(pack, i);
        }
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        for (PackListener packListener : packListeners)
        {
            packListener.afterPacks(packs, listener);
        }
    }

    /**
     * Enables {@link InstallerListener}s to be treated as {@link PackListener}s.
     */
    private class PackInstallerListener implements PackListener
    {

        /**
         * The listener to delegate to.
         */
        private final InstallerListener installerListener;

        /**
         * The progress listener.
         */
        private ProgressListener progressListener;

        /**
         * Constructs a {@code PackInstallerListener}.
         *
         * @param installerListener the listener to delegate to
         */
        public PackInstallerListener(InstallerListener installerListener)
        {
            this.installerListener = installerListener;
        }

        /**
         * Sets the progress listener.
         *
         * @param listener the progress listener
         */
        public void setProgressListener(ProgressListener listener)
        {
            progressListener = listener;
        }

        /**
         * Initialises the listener.
         *
         * @throws IzPackException for any error
         */
        @Override
        public void initialise()
        {
            try
            {
                installerListener.afterInstallerInitialization(installData);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to initialise " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked before packs are installed.
         *
         * @param packs the packs to be installed
         * @throws IzPackException for any error
         */
        @Override
        public void beforePacks(List<Pack> packs)
        {
            try
            {
                installerListener.beforePacks(installData, packs.size(), new ProgressHandler(progressListener, prompt));
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked before a pack is installed.
         *
         * @param pack the pack
         * @param i    the pack number
         * @throws IzPackException for any error
         */
        @Override
        public void beforePack(Pack pack, int i)
        {
            try
            {
                installerListener.beforePack(pack, i, new ProgressHandler(progressListener, prompt));
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked after a pack is installed.
         *
         * @param pack the pack
         * @param i    the pack number
         * @throws IzPackException for any error
         */
        @Override
        public void afterPack(Pack pack, int i)
        {
            try
            {
                installerListener.afterPack(pack, i, new ProgressHandler(progressListener, prompt));
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked after packs are installed.
         *
         * @param packs    the installed packs
         * @param listener the progress listener
         */
        @Override
        public void afterPacks(List<Pack> packs, ProgressListener listener)
        {
            try
            {
                installerListener.afterPacks(installData, new ProgressHandler(listener, prompt));
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }
    }

    /**
     * Enables {@link InstallerListener}s to be treated as {@link FileListener}s.
     */
    private class FileInstallerListener implements FileListener
    {

        /**
         * The listener to delegate to.
         */
        private final InstallerListener installerListener;

        /**
         * Constructs a {@code FileInstallerListener}.
         *
         * @param installerListener the listener to delegate to
         */
        public FileInstallerListener(InstallerListener installerListener)
        {
            this.installerListener = installerListener;
        }

        /**
         * Initialises the listener.
         *
         * @throws IzPackException for any error
         */
        @Override
        public void initialise()
        {
            // no-op. The installer listener will be initialised by the PackInstallerListener.
        }

        /**
         * Invoked before a directory is created.
         *
         * @param dir      the directory
         * @param packFile the corresponding pack file
         * @throws IzPackException for any error
         */
        @Override
        public void beforeDir(File dir, PackFile packFile)
        {
            try
            {
                installerListener.beforeDir(dir, packFile);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked after a directory is created.
         *
         * @param dir      the directory
         * @param packFile the corresponding pack file
         * @throws IzPackException for any error
         */
        @Override
        public void afterDir(File dir, PackFile packFile)
        {
            try
            {
                installerListener.afterDir(dir, packFile);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked before a file is installed.
         *
         * @param file     the file
         * @param packFile the corresponding pack file
         * @throws IzPackException for any error
         */
        @Override
        public void beforeFile(File file, PackFile packFile)
        {
            try
            {
                installerListener.beforeFile(file, packFile);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

        /**
         * Invoked after a file is installed.
         *
         * @param file     the file
         * @param packFile the corresponding pack file
         * @throws IzPackException for any error
         */
        @Override
        public void afterFile(File file, PackFile packFile)
        {
            try
            {
                installerListener.afterFile(file, packFile);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException("Failed to notify " + installerListener.getClass().getSimpleName(),
                                          exception);
            }
        }

    }

}
