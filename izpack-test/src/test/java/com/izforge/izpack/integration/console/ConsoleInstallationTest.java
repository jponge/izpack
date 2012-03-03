package com.izforge.izpack.integration.console;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.container.TestConsoleInstallationContainer;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.Console;
import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.TestConsole;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link com.izforge.izpack.installer.console.ConsoleInstaller}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsoleInstallationContainer.class)
public class ConsoleInstallationTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The console installer.
     */
    private final TestConsoleInstaller installer;

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * Constructs a <tt>ConsoleInstallationTest</tt>
     *
     * @param container       the container
     * @param installData     the installation date
     * @param rules           the rules engine
     * @param resourceManager the resource manager
     * @param requirements    the installation requirements
     * @param writer          the uninstallation data writer
     * @param console console
     * @throws Exception for any error
     */
    public ConsoleInstallationTest(BindeableContainer container, AutomatedInstallData installData,
                                   RulesEngine rules, ResourceManager resourceManager,
                                   RequirementsChecker requirements, UninstallDataWriter writer, Console console)
            throws Exception
    {
        installer = new TestConsoleInstaller(container, installData, rules, resourceManager, requirements, writer, console);
        this.installData = installData;
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testInstallation() throws Exception
    {
        TestConsole console = new TestConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");

        checkInstall(console);
    }

    /**
     * Verifies that nothing is installed if the licence is rejected.
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testRejectLicence()
    {
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");

        TestConsole console = new TestConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "2");

        installer.setConsole(console);
        installData.setInstallPath(installPath.getAbsolutePath());
        installer.run(Installer.CONSOLE_INSTALL, null);

        assertFalse(installData.isInstallSuccess());
        assertFalse(installPath.exists());

        // make sure the script has completed
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());
    }

    /**
     * Verifies that the licence can be redisplayed and accepted.
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testRedisplayAndAcceptLicence()
    {

        TestConsole console = new TestConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "3", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");

        checkInstall(console);
    }

    /**
     * Runs the console installer to generate properties.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testGenerateProperties() throws Exception
    {
        File file = new File(temporaryFolder.getRoot(), "IZPackInstall.properties");
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());

        installer.run(Installer.CONSOLE_GEN_TEMPLATE, file.getPath());

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // check the properties file matches that expected
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey(AutomatedInstallData.INSTALL_PATH));
        assertEquals("", properties.getProperty(AutomatedInstallData.INSTALL_PATH));
    }

    /**
     * Runs the console installer, installing from a properties file.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testInstallFromProperties() throws Exception
    {
        File file = new File(temporaryFolder.getRoot(), "IZPackInstall.properties");
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        Properties properties = new Properties();
        properties.put(AutomatedInstallData.INSTALL_PATH, installPath.getPath());
        properties.store(new FileOutputStream(file), "IZPack installation properties");

        TestConsole console = new TestConsole();
        installer.setConsole(console);
        installer.run(Installer.CONSOLE_FROM_TEMPLATE, file.getPath());

        // make sure there were no attempts to read from the console, as no prompting should occur
        assertEquals(0, console.getReads());

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "Licence.txt").exists());
        assertTrue(new File(installPath, "Readme.txt").exists());
        assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
    }

    /**
     * Verifies that an installer with panels that have no corresponding {@link PanelConsole} doesn't install.
     */
    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testUnsupportedInstaller()
    {
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());

        // verify installation isn't supported
        assertFalse(installer.canInstall());

        // try it anyway
        installer.run(Installer.CONSOLE_INSTALL, null);

        // verify installation failed
        assertFalse(installData.isInstallSuccess());
        assertFalse(installPath.exists());
    }

    /**
     * Verifies that console installation completes successfully.
     *
     * @param console the console
     */
    private void checkInstall(TestConsole console)
    {
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");

        installer.setConsole(console);
        installData.setInstallPath(installPath.getAbsolutePath());
        installer.run(Installer.CONSOLE_INSTALL, null);

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // make sure the script has completed
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "Licence.txt").exists());
        assertTrue(new File(installPath, "Readme.txt").exists());
        assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
    }

}
