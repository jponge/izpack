package com.izforge.izpack.test.util;


import com.izforge.izpack.util.Housekeeper;

/**
 * An {@link Housekeeper} that doesn't reboot or invoke {@link System#exit}.
 *
 * @author Tim Anderson
 */
public class TestHousekeeper extends Housekeeper
{

    private boolean shutdown = false;

    private int exitCode;

    private boolean reboot;

    public boolean hasShutdown()
    {
        return shutdown;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    public boolean getReboot()
    {
        return reboot;
    }

    @Override
    protected void terminate(int exitCode, boolean reboot)
    {
        shutdown = true;
        this.exitCode = exitCode;
        this.reboot = reboot;
    }
}
