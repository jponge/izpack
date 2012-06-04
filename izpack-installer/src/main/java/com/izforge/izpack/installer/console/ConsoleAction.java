package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;

/**
 * Console installer action.
 *
 * @author Tim Anderson
 */
abstract class ConsoleAction
{
    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * Constructs a <tt>ConsoleAction</tt>.
     *
     * @param installData the installation data
     */
    public ConsoleAction(AutomatedInstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Runs the action for the panel.
     *
     * @param panel the panel
     * @return {@code true} if the action was successful, otherwise {@code false}
     */
    public abstract boolean run(ConsolePanelView panel);

    /**
     * Invoked after the action has been successfully run for each panel.
     * <p/>
     * Performs any necessary clean up.
     *
     * @return {@code true} if the operation succeeds; {@code false} if it fails
     */
    public abstract boolean complete();

    /**
     * Determines if this is an installation action.
     * <p/>
     * An installation action is any action that performs installation. Installation actions need to be distinguished
     * from other actions as they may subsequently require a reboot.
     * <p/>
     * This default implementation always returns  <tt>true</tt>.
     *
     * @return <tt>true</tt>
     */
    public boolean isInstall()
    {
        return true;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected AutomatedInstallData getInstallData()
    {
        return installData;
    }


}
