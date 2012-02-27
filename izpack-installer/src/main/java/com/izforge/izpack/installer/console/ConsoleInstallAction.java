package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallDataWriter;

/**
 * Performs interactive console installation.
 *
 * @author Tim Anderson
 */
class ConsoleInstallAction extends AbstractInstallAction
{

    /**
     * Constructs a <tt>ConsoleInstallAction</tt>.
     *
     * @param container   the container
     * @param installData the installation date
     * @param substituter the variable substituter
     * @param rules       the rules engine
     * @param writer      the uninstallation data writer
     */
    public ConsoleInstallAction(BindeableContainer container, AutomatedInstallData installData,
                                VariableSubstitutor substituter, RulesEngine rules, UninstallDataWriter writer)
    {
        super(container, installData, substituter, rules, writer);
    }

    /**
     * Runs the action for the console panel associated with the specified panel.
     *
     * @param panel        the panel
     * @param panelConsole the console implementation of the panel
     * @param console      the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     * @throws com.izforge.izpack.api.exception.InstallerException
     *          for any installer error
     */
    @Override
    protected boolean run(Panel panel, PanelConsole panelConsole, Console console) throws InstallerException
    {
        boolean result = panelConsole.runConsole(getInstallData(), console);
        if (result)
        {
            result = validatePanel(panel, console);
        }
        return result;
    }
}
