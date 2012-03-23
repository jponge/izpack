package com.izforge.izpack.integration.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.integration.AbstractDestroyerTest;
import com.izforge.izpack.test.util.TestConsole;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Base class for {@link ConsoleInstaller} test cases.
 *
 * @author Tim Anderson
 */
public class AbstractConsoleInstallationTest extends AbstractDestroyerTest
{

    /**
     * Constructs an <tt>AbstractConsoleInstallationTest</tt>.
     *
     * @param installData the installation date
     * @throws Exception for any error
     */
    public AbstractConsoleInstallationTest(AutomatedInstallData installData) throws Exception
    {
        super(installData);
    }

    /**
     * Verifies that console installation completes successfully.
     *
     * @param installer   the installer
     * @param installData the installation data
     */
    protected void checkInstall(TestConsoleInstaller installer, AutomatedInstallData installData)
    {
        checkInstall(installer, installData, true);
    }

    /**
     * Verifies that console installation completes successfully.
     *
     * @param installer         the installer
     * @param installData       the installation data
     * @param expectUninstaller whether to expect an uninstaller to be created
     */
    protected void checkInstall(TestConsoleInstaller installer, AutomatedInstallData installData,
                                boolean expectUninstaller)
    {
        String installPath = installData.getInstallPath();

        installer.run(Installer.CONSOLE_INSTALL, null);

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // make sure the script has completed
        TestConsole console = installer.getConsole();
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());

        if (expectUninstaller)
        {
            assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
        }
        else
        {
            assertFalse(new File(installPath, "Uninstaller/uninstaller.jar").exists());
        }
    }
}
