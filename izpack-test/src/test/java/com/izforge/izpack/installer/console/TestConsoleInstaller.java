package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Housekeeper;

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
     * Constructs a <tt>TestConsoleInstaller</tt>
     *
     * @param factory      the object factory
     * @param installData  the installation date
     * @param rules        the rules engine
     * @param requirements the installation requirements
     * @param writer       the uninstallation data writer
     * @param console      the console
     * @param housekeeper  the house-keeper
     * @throws Exception for any error
     */
    public TestConsoleInstaller(ObjectFactory factory, AutomatedInstallData installData,
                                RulesEngine rules, RequirementsChecker requirements, UninstallDataWriter writer,
                                TestConsole console, Housekeeper housekeeper)
            throws Exception
    {
        super(factory, installData, rules, requirements, writer, console, housekeeper);
    }

    /**
     * Returns the console.
     *
     * @return the console
     */
    public TestConsole getConsole()
    {
        return (TestConsole) super.getConsole();
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
