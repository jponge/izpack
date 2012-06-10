package com.izforge.izpack.installer.event;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
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
     * The listeners.
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
     * @param prompt the prompt
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
     * Invoked when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @throws IzPackException if a listener throws an exception
     */
    public void afterInstallerInitialization()
    {
        forEachListener(new Runner()
        {
            @Override
            public void run(InstallerListener listener) throws Exception
            {
                listener.afterInstallerInitialization(installData);
            }
        });
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs    number of packs which are defined for this installation
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforePacks(final int packs, ProgressListener listener)
    {
        final ProgressHandler adapter = new ProgressHandler(listener, prompt);
        forEachListener(new Runner()
        {
            @Override
            public void run(InstallerListener listener) throws Exception
            {
                listener.beforePacks(installData, packs, adapter);
            }
        });
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforePack(final Pack pack, final int i, ProgressListener listener) throws Exception
    {
        final ProgressHandler adapter = new ProgressHandler(listener, prompt);
        forEachListener(new Runner()
        {
            @Override
            public void run(InstallerListener listener) throws Exception
            {
                listener.beforePack(pack, i, adapter);
            }
        });
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
    public void afterPack(final Pack pack, final int i, ProgressListener listener) throws Exception
    {
        final ProgressHandler adapter = new ProgressHandler(listener, prompt);
        forEachListener(new Runner()
        {
            @Override
            public void run(InstallerListener listener) throws Exception
            {
                listener.afterPack(pack, i, adapter);
            }
        });
    }

    /**
     * Invoked after packs are installed.
     *
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterPacks(ProgressListener listener)
    {
        final ProgressHandler adapter = new ProgressHandler(listener, prompt);
        forEachListener(new Runner()
        {
            @Override
            public void run(InstallerListener listener) throws Exception
            {
                listener.afterPacks(installData, adapter);
            }
        });
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

}
