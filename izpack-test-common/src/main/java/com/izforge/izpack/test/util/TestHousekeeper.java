package com.izforge.izpack.test.util;


import com.izforge.izpack.util.Housekeeper;

/**
 * An {@link Housekeeper} that doesn't reboot or invoke {@link System#exit}.
 *
 * @author Tim Anderson
 */
public class TestHousekeeper extends Housekeeper
{

    /**
     * Determines if the installer has shut down.
     */
    private boolean shutdown = false;

    /**
     * The installer exit code.
     */
    private int exitCode;

    /**
     * Indicates if the installer tried to reboot.
     */
    private boolean reboot;

    /**
     * Waits at most <tt>timeout</tt> milliseconds for the installer to shut down.
     *
     * @param timeout the time to wait, in milliseconds
     */
    public void waitShutdown(long timeout)
    {
        synchronized (this)
        {
            if (!shutdown)
            {
                try
                {
                    wait(timeout);
                }
                catch (InterruptedException ignore)
                {
                    // do nothing
                }
            }
        }
    }

    /**
     * Determines if the installer has shut down.
     *
     * @return <tt>true</tt> if the installer has shut down
     */
    public boolean hasShutdown()
    {
        return shutdown;
    }

    /**
     * The installer exit code.
     *
     * @return the exit code. The value is undefined until the installer has shut down
     */
    public int getExitCode()
    {
        return exitCode;
    }

    /**
     * Determines if the installer tried to reboot.
     *
     * @return <tt>true</tt> if the installer tried to reboot. The value is undefined until the installer has shut down
     */
    public boolean getReboot()
    {
        return reboot;
    }

    @Override
    protected void terminate(int exitCode, boolean reboot)
    {
        this.exitCode = exitCode;
        this.reboot = reboot;
        synchronized (this)
        {
            shutdown = true;
            notifyAll();
        }
    }
}
