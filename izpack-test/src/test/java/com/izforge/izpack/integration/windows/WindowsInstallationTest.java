package com.izforge.izpack.integration.windows;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.RegistryUninstallerListener;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.integration.AbstractDestroyerTest;
import com.izforge.izpack.integration.HelperTestMethod;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.util.os.ShellLink;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test installation on Windows.
 * <p/>
 * Verifies that:
 * <ul>
 * <li>An <em>Uninstall</em> entry is added to the registry by {@link RegistryInstallerListener} during
 * installation</li>
 * <li>A shortcut is created for the uninstaller during installation</li>
 * <li>The <em>Uninstall</em> entry is removed at uninstallation by {@link RegistryUninstallerListener}</li>
 * <li>The shortcut is removed during uninstallation</li>
 * </ul>
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@RunOn(Platform.Name.WINDOWS)
@Container(TestInstallationContainer.class)
public class WindowsInstallationTest extends AbstractDestroyerTest
{

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * The librarian.
     */
    private final Librarian librarian;

    /**
     * The registry handler.
     */
    private final RegistryDefaultHandler handler;

    /**
     * The house keeper.
     */
    private final TestHousekeeper housekeeper;

    /**
     * The installer frame fixture.
     */
    private FrameFixture installerFrameFixture;

    /**
     * The dialog frame fixture.
     */
    private DialogFixture dialogFrameFixture;

    /**
     * Registry uninstallation key.
     */
    private static final String UNINSTALL_KEY = RegistryHandler.UNINSTALL_ROOT + "IzPack Windows Installation Test 1.0";


    /**
     * Constructs a <tt>WindowsRegistryTest</tt>.
     *
     * @param frame       the installer frame
     * @param controller  the installer controller
     * @param installData the installation data
     * @param librarian   the librarian
     * @param handler     the registry handler
     * @param housekeeper the house-keeper
     */
    public WindowsInstallationTest(InstallerFrame frame, InstallerController controller,
                                   AutomatedInstallData installData, Librarian librarian,
                                   RegistryDefaultHandler handler, TestHousekeeper housekeeper)
    {
        super(installData);
        this.frame = frame;
        this.controller = controller;
        this.librarian = librarian;
        this.handler = handler;
        this.housekeeper = housekeeper;
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        destroyRegistryEntries();
    }

    @After
    public void tearDown() throws Exception
    {
        destroyRegistryEntries();
        try
        {
            if (dialogFrameFixture != null)
            {
                dialogFrameFixture.cleanUp();
                dialogFrameFixture = null;
            }
        }
        finally
        {
            if (installerFrameFixture != null)
            {
                installerFrameFixture.cleanUp();
                installerFrameFixture = null;
            }
        }
    }

    @Test
    @InstallFile("samples/windows/install.xml")
    public void testInstallation() throws Exception
    {
        assertFalse("This test must be run as administrator, or with Windows UAC turned off",
                new PrivilegedRunner().isElevationNeeded());

        installerFrameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);

        // CheckedHelloPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // PacksPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // InstallPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // ShortcutPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // SimpleFinishPanel
        Thread.sleep(1200);
        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();

        // verify the installer has terminated, and an uninstaller has been written
        housekeeper.waitShutdown(2 * 60 * 1000);
        assertTrue(housekeeper.hasShutdown());
        assertEquals(0, housekeeper.getExitCode());
        assertFalse(housekeeper.getReboot());
        File uninstaller = getUninstallerJar();

        // make sure there is an Uninstall entry for the installation
        RegistryHandler registry = getRegistryHandler();
        assertTrue(registry.keyExist(UNINSTALL_KEY));

        // make sure a shortcut to the uninstaller exists
        ShellLink link = new ShellLink(ShellLink.PROGRAM_MENU, ShellLink.ALL_USERS, "IzPack Windows Installation Test",
                "Uninstaller", librarian);
        assertEquals(ShellLink.PROGRAM_MENU, link.getLinkType());
        assertEquals(uninstaller, new File(link.getTargetPath()));
        assertEquals(link.getDescription(), "This uninstalls the test");

        // verify the shortcut file exists
        File shortcut = new File(link.getFileName());
        assertTrue(shortcut.exists());

        // run the uninstaller
        runDestroyer(uninstaller);

        // make sure the Uninstall entry has been removed
        assertFalse(registry.keyExist(UNINSTALL_KEY));

        // verify the shortcut no longer exists
        assertFalse(shortcut.exists());

    }

    /**
     * Destroys registry entries that may not have been cleared out by a previous run.
     *
     * @throws NativeLibException if the entries cannot be removed
     */
    private void destroyRegistryEntries() throws NativeLibException
    {
        RegistryHandler registry = getRegistryHandler();
        if (registry.keyExist(UNINSTALL_KEY))
        {
            registry.deleteKey(UNINSTALL_KEY);
        }
    }

    /**
     * Returns the registry handler.
     *
     * @return the registry handler
     * @throws NativeLibException for any registry error
     */
    private RegistryHandler getRegistryHandler() throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        assertNotNull(registry);
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        return registry;
    }

}
