package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.util.Console;

/**
 * Performs interactive console installation.
 *
 * @author Tim Anderson
 */
class ConsoleInstallAction extends AbstractInstallAction
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * Constructs a <tt>ConsoleInstallAction</tt>.
     *
     * @param installData the installation date
     * @param writer      the uninstallation data writer
     */
    public ConsoleInstallAction(Console console, AutomatedInstallData installData, UninstallDataWriter writer)
    {
        super(installData, writer);
        this.console = console;
    }


    /**
     * Runs the action for the panel.
     *
     * @param panel the panel
     * @return {@code true} if the action was successful, otherwise {@code false}
     */
    @Override
    public boolean run(ConsolePanelView panel)
    {
        PanelConsole view = panel.getView();
        return view.runConsole(getInstallData(), console);
    }

}
