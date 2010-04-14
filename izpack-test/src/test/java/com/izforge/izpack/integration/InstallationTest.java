package com.izforge.izpack.integration;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.compiler.container.TestIntegrationContainer;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation
 */

@RunWith(PicoRunner.class)
@Container(TestIntegrationContainer.class)
public class InstallationTest
{
    private DialogFixture dialogFrameFixture;
    private FrameFixture installerFrameFixture;
    private ResourceManager resourceManager;
    private LanguageDialog languageDialog;
    private InstallerFrame installerFrame;
    private GUIInstallData installData;
    private InstallerController installerController;

    public InstallationTest(ResourceManager resourceManager, LanguageDialog languageDialog, InstallerFrame installerFrame, GUIInstallData installData, InstallerController installerController)
    {
        this.installerController = installerController;
        this.resourceManager = resourceManager;
        this.languageDialog = languageDialog;
        this.installData = installData;
        this.installerFrame = installerFrame;
    }

    @After
    public void tearBinding() throws NoSuchFieldException, IllegalAccessException
    {
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
    @InstallFile("samples/helloAndFinish.xml")
    public void testHelloAndFinishPanels() throws Exception
    {
        Image image = resourceManager.getImageIconResource("/img/JFrameIcon.png").getImage();
        assertThat(image, IsNull.<Object>notNullValue());

        languageDialog.initLangPack();
        installerFrameFixture = prepareFrameFixture();

        // Hello panel
        installerFrameFixture.requireSize(new Dimension(640, 480));
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel

    }


    @Test
    @Ignore
    @InstallFile("samples/helloAndFinish.xml")
    public void testHelloAndFinishPanelsCompressed() throws Exception
    {
//        System.out.println("Using file " + out.getName());
//        File workingDirectory = getWorkingDirectory("samples");
//        File out = new File("out.jar");
//        File installerFile = new File(workingDirectory, "helloAndFinish.xml");
//        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), workingDirectory.getAbsolutePath(), out.getAbsolutePath());
//        data.setComprFormat("bzip2");
//        data.setComprLevel(2);
//        compileInstallJar(data);
//        applicationContainer.getComponent(LanguageDialog.class).initLangPack();
//        installerFrameFixture = prepareFrameFixture();
//
//        // Hello panel
//        installerFrameFixture.requireSize(new Dimension(640, 480));
//        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
//        installerFrameFixture.requireVisible();
//        // Finish panel
//        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();
    }

    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testBasicInstall() throws Exception
    {
        File installPath = prepareInstallation(installData);
        // Lang picker
        clickDefaultLang();

        installerFrameFixture = prepareFrameFixture();
        Thread.sleep(600);
        // Hello panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(600);
        // Info Panel
        installerFrameFixture.textBox(GuiId.INFO_PANEL_TEXT_AREA.id).requireText("A readme file ...");
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(300);
        // Licence Panel
        installerFrameFixture.textBox(GuiId.LICENCE_TEXT_AREA.id).requireText("(Consider it as a licence file ...)");
        installerFrameFixture.radioButton(GuiId.LICENCE_NO_RADIO.id).requireSelected();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        Thread.sleep(300);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(300);
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();
        // Packs Panel
        Thread.sleep(300);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install Panel
        waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Finish panel
        installerFrameFixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).click();
        Thread.sleep(800);
        installerFrameFixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).fileNameTextBox().enterText("auto.xml");
        Thread.sleep(300);
        installerFrameFixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).approve();
        assertThat(new File(installPath, "auto.xml").exists(), Is.is(true));
//        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();
    }


    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testIzpackInstallation() throws Exception
    {
        File installPath = prepareInstallation(installData);
        clickDefaultLang();

        installerFrameFixture = prepareFrameFixture();
        java.util.List panelList = installData.getPanels();
        // Hello panel
        Thread.sleep(600000);
//        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Chack Panel
        Thread.sleep(600);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Licence Panel
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();
        // Packs
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Summary
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install
        waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Shortcut
        // Deselect shortcut creation
        Thread.sleep(400);
        installerFrameFixture.checkBox(GuiId.SHORTCUT_CREATE_CHECK_BOX.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Finish
//        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();
    }


    private File prepareInstallation(GUIInstallData installData) throws IOException
    {
        File installPath = new File(installData.getInstallPath());
        FileUtils.deleteDirectory(installPath);
        assertThat(installPath.exists(), Is.is(false));
        return installPath;
    }


    private void clickDefaultLang()
    {
        dialogFrameFixture = prepareDialogFixture();
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        // Seems necessary to unlock window
        dialogFrameFixture.cleanUp();
        dialogFrameFixture = null;
    }

    private void waitAndCheckInstallation(GUIInstallData installData, File installPath) throws InterruptedException
    {
        while (!installData.isCanClose())
        {
            Thread.sleep(500);
        }
        assertThat(installPath.exists(), Is.is(true));
        UninstallData uninstallData = new UninstallData();
        for (String p : uninstallData.getInstalledFilesList())
        {
            File f = new File(p);
            assertThat(f.exists(), Is.is(true));
        }
    }


    /**
     * Prepare fest fixture for installer frame
     *
     * @throws Exception
     */
    protected FrameFixture prepareFrameFixture() throws Exception
    {
        FrameFixture installerFrameFixture = new FrameFixture(installerFrame);
        installerController.buildInstallation();
        installerFrameFixture.show();
        installerFrame.sizeFrame();
        // wait center
        return installerFrameFixture;
    }

    /**
     * Prepare fest fixture for lang selection
     */
    protected DialogFixture prepareDialogFixture()
    {
        DialogFixture dialogFixture = new DialogFixture(languageDialog);
        dialogFixture.show();
        return dialogFixture;
    }
}
