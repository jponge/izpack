package com.izforge.izpack.integration.datavalidator;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.integration.HelperTestMethod;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.install.InstallPanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestHousekeeper;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests that {@link DataValidator}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class DataValidatorTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Install data.
     */
    private final GUIInstallData installData;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * The house-keeper.
     */
    private final TestHousekeeper housekeeper;

    /**
     * The frame fixture.
     */
    private FrameFixture frameFixture;

    /**
     * Constructs an <tt>PanelActionValidatorTest</tt>.
     *
     * @param installData the install data
     * @param frame       the installer frame
     * @param controller  the installer controller
     * @param housekeeper the house-keeper
     */
    public DataValidatorTest(GUIInstallData installData, InstallerFrame frame, InstallerController controller,
                             TestHousekeeper housekeeper)
    {
        this.installData = installData;
        this.frame = frame;
        this.controller = controller;
        this.housekeeper = housekeeper;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp()
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        assertTrue(installPath.mkdirs());
        installData.setInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown()
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Verifies that {@link DataValidator}s associated with panels are invoked.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/datavalidators.xml")
    public void testDataValidators() throws Exception
    {
        assertEquals(3, installData.getPanelsOrder().size());
        Panel hello = installData.getPanelsOrder().get(0);
        Panel install = installData.getPanelsOrder().get(1);
        Panel finish = installData.getPanelsOrder().get(2);

        // verify that all class names are fully qualified
        assertEquals(HelloPanel.class.getName(), hello.getClassName());
        assertEquals(TestDataValidator.class.getName(), hello.getValidator());

        assertEquals(InstallPanel.class.getName(), install.getClassName());
        assertEquals(TestDataValidator.class.getName(), install.getValidator());

        assertEquals(SimpleFinishPanel.class.getName(), finish.getClassName());
        assertEquals(TestDataValidator.class.getName(), finish.getValidator());

        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);

        // HelloPanel
        Thread.sleep(2000);
        checkCurrentPanel(HelloPanel.class);
        installData.setVariable("HelloPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("HelloPanel.error");
        checkCurrentPanel(HelloPanel.class);

        installData.setVariable("HelloPanel.status", "WARNING");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("HelloPanel.warning");
        assertEquals(2, TestDataValidator.getValidate("HelloPanel", installData));

        // InstallPanel
        Thread.sleep(1000);
        checkCurrentPanel(InstallPanel.class);
        installData.setVariable("InstallPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("InstallPanel.error");

        installData.setVariable("InstallPanel.status", "OK");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        assertEquals(2, TestDataValidator.getValidate("InstallPanel", installData));

        // SimpleFinishPanel
        Thread.sleep(1000);
        checkCurrentPanel(SimpleFinishPanel.class);

        // Validators not invoked on last panel, so this should be a no-op
        installData.setVariable("SimpleFinishPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_QUIT.id).click();
        assertEquals(0, TestDataValidator.getValidate("SimpleFinishPanel", installData));

        // verify the installer has terminated, and an uninstaller has been written
        housekeeper.waitShutdown(2 * 60 * 1000);
        assertTrue(housekeeper.hasShutdown());
        assertEquals(0, housekeeper.getExitCode());
        assertFalse(housekeeper.getReboot());
    }

    /**
     * Verifies that the current panel is an instance of the specified type.
     *
     * @param type the expected panel type
     */
    private void checkCurrentPanel(Class<? extends IzPanel> type)
    {
        Panel panel = installData.getPanelsOrder().get(installData.getCurPanelNumber());
        assertEquals(type.getName(), panel.getClassName());
    }

    /**
     * Verifies that a dialog is being displayed with the specified text.
     * <p/>
     * This closes the dialog.
     *
     * @param text the expected text
     */
    private void checkDialog(String text)
    {
        DialogFixture dialog = frameFixture.dialog(Timeout.timeout(10000));
        assertEquals(text, dialog.label("OptionPane.label").text());
        dialog.button(JButtonMatcher.withText("OK")).click();
    }
}
