package com.izforge.izpack.integration.automation;

import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestConsoleInstallationContainer;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.integration.AbstractInstallationTest;
import com.izforge.izpack.integration.UninstallHelper;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.FileUtil;


/**
 * Tests the {@link AutomatedInstaller}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsoleInstallationContainer.class)
public class AutomatedInstallerTest extends AbstractInstallationTest
{

    /**
     * The installer.
     */
    private final AutomatedInstaller installer;

    /**
     * Constructs an {@code AutomatedInstaller}.
     *
     * @param installer   the installer
     * @param installData the installation data
     */
    public AutomatedInstallerTest(AutomatedInstaller installer, AutomatedInstallData installData)
    {
        super(installData);
        this.installer = installer;
    }

    /**
     * Tests installation and uninstallation.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testAutomatedInstaller() throws Exception
    {
        AutomatedInstallData installData = getInstallData();

        URL url = getClass().getResource("/samples/basicInstall/auto.xml");
        assertNotNull(url);
        String config = FileUtil.convertUrlToFilePath(url);
        installer.init(config, null);
        installer.doInstall();

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // verify expected files exist
        String installPath = getInstallPath();
        File dir = new File(installPath);
        assertFileExists(dir, "Licence.txt");
        assertFileExists(dir, "Readme.txt");
        assertFileExists(dir, "Uninstaller/uninstaller.jar");

        // perform uninstallation
        UninstallHelper.uninstall(installData);

        // verify the install directory no longer exists
        assertFalse(new File(installPath).exists());
    }
}
