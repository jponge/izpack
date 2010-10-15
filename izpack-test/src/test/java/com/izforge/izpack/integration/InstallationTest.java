package com.izforge.izpack.integration;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;

import java.awt.*;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation
 */

@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class InstallationTest
{
    @Rule
    public MethodRule globalTimeout = new org.junit.rules.Timeout(HelperTestMethod.TIMEOUT);
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
        installerFrameFixture = HelperTestMethod.prepareFrameFixture(installerFrame, installerController);

        // Hello panel
//        installerFrameFixture.requireSize(new Dimension(640, 480));
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel
    }

    @Test
    @InstallFile("samples/silverpeas/silverpeas.xml")
    public void testSilverpeas() throws Exception
    {
        languageDialog.initLangPack();
        installerFrameFixture = HelperTestMethod.prepareFrameFixture(installerFrame, installerController);
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
        File installPath = HelperTestMethod.prepareInstallation(installData);
        // Lang picker
        HelperTestMethod.clickDefaultLang(dialogFrameFixture, languageDialog);

        installerFrameFixture = HelperTestMethod.prepareFrameFixture(installerFrame, installerController);
        Thread.sleep(600);
        // Hello panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(600);
        // Info Panel
        installerFrameFixture.textBox(GuiId.INFO_PANEL_TEXT_AREA.id).requireText("A readme file ...");
        installerFrameFixture.button(GuiId.BUTTON_PREV.id).requireVisible();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.button(GuiId.BUTTON_PREV.id).requireEnabled();
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
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

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


}
