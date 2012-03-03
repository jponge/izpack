package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.RequirementsChecker;

/**
 * Test implementation of the {@link ConsoleInstaller}.
 * <p/>
 * This supports running the installer against a script of input commands.
 *
 * @author Tim Anderson
 */
public class TestConsoleInstaller extends ConsoleInstaller
{

    /**
     * A console to override the default.
     */
    private Console console;

    /**
     * Constructs a <tt>TestConsoleInstaller</tt>
     *
     * @param container       the container
     * @param installData     the installation date
     * @param rules           the rules engine
     * @param resourceManager the resource manager
     * @param requirements    the installation requirements
     * @param writer          the uninstallation data writer
     * @param console         the console
     * @throws Exception for any error
     */
    public TestConsoleInstaller(BindeableContainer container, AutomatedInstallData installData,
                                RulesEngine rules, ResourceManager resourceManager,
                                RequirementsChecker requirements, UninstallDataWriter writer, Console console)
            throws Exception
    {
        super(container, installData, rules, resourceManager, requirements, writer, console);
    }

    /**
     * Registers a console.
     *
     * @param console the console. May be <tt>null</tt>
     */
    public void setConsole(Console console)
    {
        this.console = console;
    }

    /**
     * Runs a console action.
     *
     * @param action the action to run
     * @return <tt>true</tt> if the action was sucessful, otherwise <tt>false</tt>
     */
    @Override
    protected boolean run(ConsoleAction action)
    {
        if (console != null)
        {
            return action.run(console);
        }
        return super.run(action);
    }

    /**
     * Terminates the installation process.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param reboot      if <tt>true</tt> perform a reboot
     */
    @Override
    protected void terminate(boolean exitSuccess, boolean reboot)
    {
        // Disable exit/reboot so the test can complete
    }
}
