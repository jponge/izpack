package com.izforge.izpack.integration.console;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.TestConsole;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.language.ConditionCheck;
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
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link com.izforge.izpack.installer.console.ConsoleInstaller}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
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
     * @param check           the condition check
     * @param writer          the uninstallation data writer
     * @throws Exception for any error
     */
    public ConsoleInstallationTest(BindeableContainer container, AutomatedInstallData installData,
                                   RulesEngine rules, ResourceManager resourceManager,
                                   ConditionCheck check, UninstallDataWriter writer) throws Exception
    {
        installer = new TestConsoleInstaller(container, installData, rules, resourceManager, check, writer);
        this.installData = installData;
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testInstallation() throws Exception
    {
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");

        TestConsole console = new TestConsole();
        console.addScript("LicensePanel", "\n", "\n", "\n", "\n", "\n", "\n", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");

        installer.setConsole(console);
        installData.setInstallPath(installPath.getAbsolutePath());
        installer.run(Installer.CONSOLE_INSTALL, null);

        // make sure the script has completed
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "bin/start.sh").exists());
        assertTrue(new File(installPath, "legal/IzPack-Licence.txt").exists());
        assertTrue(new File(installPath, "lib/looks.jar").exists());
        assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
    }

    /**
     * Runs the console installer to generate properties.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testGenerateProperties() throws Exception
    {
        File file = new File(temporaryFolder.getRoot(), "IZPackInstall.properties");
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());

        installer.run(Installer.CONSOLE_GEN_TEMPLATE, file.getPath());

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
    @InstallFile("samples/izpack/install.xml")
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

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "bin/start.sh").exists());
        assertTrue(new File(installPath, "legal/IzPack-Licence.txt").exists());
        assertTrue(new File(installPath, "lib/looks.jar").exists());
        assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
    }

}
