package com.izforge.izpack.integration;

import static com.izforge.izpack.test.util.TestHelper.assertFileEquals;
import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static com.izforge.izpack.test.util.TestHelper.assertFileNotExists;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.compiler.container.TestCompilationContainer;
import com.izforge.izpack.compiler.container.TestGUIInstallerContainer;
import com.izforge.izpack.compiler.packager.impl.MultiVolumePackager;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.impl.GUIInstallerContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpacker;
import com.izforge.izpack.test.util.TestHelper;


/**
 * Tests installation when using the {@link MultiVolumePackager} and {@link MultiVolumeUnpacker}.
 *
 * @author Tim Anderson
 */
public class MultiVolumeInstallationTest
{

    /**
     * Temporary directory for installing to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The installer frame fixture.
     */
    private FrameFixture fixture;


    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown()
    {
        if (fixture != null)
        {
            fixture.cleanUp();
        }
    }

    /**
     * Packages an multi-volume installer and installs it.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultiVolume() throws Exception
    {
        // compiler output goes to targetDir
        File targetDir = new File(temporaryFolder.getRoot(), "target");
        assertTrue(targetDir.mkdir());
        TestCompilationContainer compiler = new TestCompilationContainer("samples/multivolume/multivolume.xml",
                                                                         targetDir);

        // create the pack files. These correspond to those in multivolume.xml - created here so they don't need to be
        // committed.
        File baseDir = compiler.getBaseDir();
        File file1 = TestHelper.createFile(baseDir, "file1.dat", 10000);
        File file2 = TestHelper.createFile(baseDir, "file2.dat", 20000);
        File file3 = TestHelper.createFile(baseDir, "file3.dat", 30000);
        File file4 = TestHelper.createFile(baseDir, "file4.dat", 40000);
        File file5 = TestHelper.createFile(baseDir, "file5.dat", 50000);
        File file6 = TestHelper.createFile(baseDir, "file6.dat", 60000);

        // run the compiler
        compiler.launchCompilation();

        // now run the installer
        GUIInstallerContainer installer = new TestGUIInstallerContainer();

        InstallerController controller = installer.getComponent(InstallerController.class);
        LanguageDialog languageDialog = installer.getComponent(LanguageDialog.class);
        InstallerFrame installerFrame = installer.getComponent(InstallerFrame.class);
        GUIInstallData installData = installer.getComponent(GUIInstallData.class);

        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());
        installData.setMediaPath(targetDir.getPath());

        // Lang picker
        HelperTestMethod.clickDefaultLang(languageDialog);

        fixture = HelperTestMethod.prepareFrameFixture(installerFrame, controller);
        Thread.sleep(600);
        // Hello panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(600);
        // Info Panel
        fixture.textBox(GuiId.INFO_PANEL_TEXT_AREA.id).requireText("A readme file ...");
        fixture.button(GuiId.BUTTON_PREV.id).requireVisible();
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        fixture.button(GuiId.BUTTON_PREV.id).requireEnabled();
        Thread.sleep(300);
        // Licence Panel
        fixture.textBox(GuiId.LICENCE_TEXT_AREA.id).requireText("(Consider it as a licence file ...)");
        fixture.radioButton(GuiId.LICENCE_NO_RADIO.id).requireSelected();
        fixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();
        fixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        Thread.sleep(300);
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        fixture.optionPane().requireWarningMessage();
        fixture.optionPane().okButton().click();
        // Packs Panel
        Thread.sleep(1000);
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install Panel
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

        fixture.button(GuiId.BUTTON_NEXT.id).click();
        // Finish panel
        fixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).click();
        Thread.sleep(800);
        fixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).fileNameTextBox().enterText("auto.xml");
        Thread.sleep(300);
        fixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).approve();

        // verify expected files exist
        assertFileEquals(file1, installPath, file1.getName());
        assertFileEquals(file2, installPath, file2.getName());
        assertFileEquals(file5, installPath, file5.getName());
        assertFileEquals(file6, installPath, file6.getName());

        assertFileExists(installPath, "auto.xml");

        // verify skipped pack1 files not present
        assertFileNotExists(installPath, file3.getName());
        assertFileNotExists(installPath, file4.getName());
    }


}
