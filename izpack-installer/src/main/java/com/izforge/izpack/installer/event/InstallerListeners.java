package com.izforge.izpack.installer.event;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
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
     * Determines if any of the listeners should be notified of file and directory events.
     */
    private boolean fileListener;

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
            if (!fileListener && l.isFileListener())
            {
                fileListener = true;
            }
        }
        else if (listener instanceof PackListener)
        {
            packListeners.add((PackListener) listener);
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
        for (PackListener listener : packListeners)
        {
            listener.initialise();
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
            packListener.beforePacks(packs, listener);
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
            packListener.beforePack(pack, i, listener);
        }
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise <tt>false</tt>
     */
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
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeDir(final File dir, final PackFile packFile)
    {
        if (fileListener)
        {
            forEachListener(new Runner()
            {
                @Override
                public void run(InstallerListener listener) throws Exception
                {
                    if (listener.isFileListener())
                    {
                        listener.beforeDir(dir, packFile);
                    }
                }
            });
        }
    }

    /**
     * Invoked after a directory is created.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @throws IzPackException if a listener throws an exception
     */
    public void afterDir(final File dir, final PackFile packFile)
    {
        if (fileListener)
        {
            forEachListener(new Runner()
            {
                @Override
                public void run(InstallerListener listener) throws Exception
                {
                    if (listener.isFileListener())
                    {
                        listener.afterDir(dir, packFile);
                    }
                }
            });
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
    public void beforeFile(final File file, final PackFile packFile)
    {
        if (fileListener)
        {
            forEachListener(new Runner()
            {
                @Override
                public void run(InstallerListener listener) throws Exception
                {
                    if (listener.isFileListener())
                    {
                        listener.beforeFile(file, packFile);
                    }
                }
            });
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
    public void afterFile(final File file, final PackFile packFile)
    {
        if (fileListener)
        {
            forEachListener(new Runner()
            {
                @Override
                public void run(InstallerListener listener) throws Exception
                {
                    if (listener.isFileListener())
                    {
                        listener.afterFile(file, packFile);
                    }
                }
            });
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
            packListener.afterPack(pack, i, listener);
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
     * Executes {@code runner} for each registered listener.
     *
     * @param runner the runner
     * @throws IzPackException if the listener throws an exception
     */
    private void forEachListener(Runner runner)
    {
        for (InstallerListener listener : listeners)
        {
            try
            {
                runner.run(listener);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException(exception);
            }
        }
    }

    private static interface Runner
    {
        void run(InstallerListener listener) throws Exception;
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
         * Constructs a {@code PackInstallerListener}.
         *
         * @param installerListener the listener to delegate to
         */
        public PackInstallerListener(InstallerListener installerListener)
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
         * @param packs    the packs to be installed
         * @param listener the progress listener
         * @throws IzPackException for any error
         */
        @Override
        public void beforePacks(List<Pack> packs, ProgressListener listener)
        {
            try
            {
                installerListener.beforePacks(installData, packs.size(), new ProgressHandler(listener, prompt));
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
         * @param pack     the pack
         * @param i        the pack number
         * @param listener the progress listener
         * @throws IzPackException for any error
         */
        @Override
        public void beforePack(Pack pack, int i, ProgressListener listener)
        {
            try
            {
                installerListener.beforePack(pack, i, new ProgressHandler(listener, prompt));
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
         * @param pack     the pack
         * @param i        the pack number
         * @param listener the progress listener
         * @throws IzPackException for any error
         */
        @Override
        public void afterPack(Pack pack, int i, ProgressListener listener)
        {
            try
            {
                installerListener.afterPack(pack, i, new ProgressHandler(listener, prompt));
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

}
