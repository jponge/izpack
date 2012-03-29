package com.izforge.izpack.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.listener.TestInstallerListener;


/**
 * Tests that {@link InstallerListener}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class InstallerListenerTest
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
     * Frame fixture.
     */
    private FrameFixture frameFixture;

    /**
     * The install path.
     */
    private File installPath;

    /**
     * Constructs an <tt>InstallerListenerTest</tt>.
     *
     * @param installData the install data
     * @param frame       the installer frame
     * @param controller  the installer controller
     */
    public InstallerListenerTest(GUIInstallData installData, InstallerFrame frame, InstallerController controller)
    {
        this.installData = installData;
        this.frame = frame;
        this.controller = controller;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp()
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        installPath = new File(temporaryFolder.getRoot(), "izpackTest");
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
     * Verifies that {@link InstallerListener} methods are invoked the correct no. of times when registered.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/event/customlisteners.xml")
    public void testInstallListenerInvocation() throws Exception
    {
        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        frameFixture.requireVisible();

        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

        List<InstallerListener> listeners = installData.getInstallerListener();
        assertEquals(1, listeners.size());
        TestInstallerListener listener = (TestInstallerListener) listeners.get(0);
        assertEquals(1, listener.getAfterInstallerInitializationCount());
        assertEquals(1, listener.getBeforePacksCount());
        assertEquals(3, listener.getBeforePackCount());
        assertEquals(4, listener.getBeforeDirCount());
        assertEquals(4, listener.getBeforeFileCount());

        assertEquals(listener.getBeforePacksCount(), listener.getAfterPacksCount());
        assertEquals(listener.getBeforePackCount(), listener.getAfterPackCount());
        assertEquals(listener.getBeforeDirCount(), listener.getAfterDirCount());
        assertEquals(listener.getBeforeFileCount(), listener.getAfterFileCount());
    }
}
