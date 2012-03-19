package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.util.Console;

/**
 * A {@link ConsoleAction} for performing installations.
 * <p/>
 * This writes uninstallation information if required, at the end of a successful installation.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInstallAction extends ConsoleAction
{
    /**
     * The uninstallation data writer.
     */
    private final UninstallDataWriter writer;

    /**
     * Constructs an <tt>AbstractConsoleInstallAction</tt>.
     *
     * @param factory       the panel console factory
     * @param installData   the installation data
     * @param substituter   the variable substituter
     * @param objectFactory the factory for {@link com.izforge.izpack.api.installer.DataValidator}
     *                      instances
     * @param rules         the rules engine
     * @param writer        the uninstallation data writer
     */
    public AbstractInstallAction(PanelConsoleFactory factory, AutomatedInstallData installData,
                                 VariableSubstitutor substituter, ObjectFactory objectFactory, RulesEngine rules,
                                 UninstallDataWriter writer)
    {
        super(factory, installData, substituter, objectFactory, rules);
        this.writer = writer;
    }

    /**
     * Runs the action for each panel.
     *
     * @param console the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(Console console)
    {
        boolean result = super.run(console);
        if (result)
        {
            if (writer.isUninstallRequired())
            {
                result = writer.write();
            }
        }
        return result;
    }
}
