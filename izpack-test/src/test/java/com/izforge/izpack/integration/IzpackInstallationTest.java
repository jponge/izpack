package com.izforge.izpack.integration;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.compiler.container.TestIntegrationContainer;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.OsVersion;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Timeout;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Test for an installation
 */

@RunWith(PicoRunner.class)
@Container(TestIntegrationContainer.class)
public class IzpackInstallationTest
{
    private DialogFixture dialogFrameFixture;
    private FrameFixture installerFrameFixture;
    private LanguageDialog languageDialog;
    private InstallerFrame installerFrame;
    private GUIInstallData installData;
    private InstallerController installerController;

    public IzpackInstallationTest(LanguageDialog languageDialog, InstallerFrame installerFrame, GUIInstallData installData, InstallerController installerController)
    {
        this.installerController = installerController;
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
    @InstallFile("samples/izpack/install.xml")
    public void testIzpackInstallation() throws Exception
    {
        File installPath = HelperTestMethod.prepareInstallation(installData);
        HelperTestMethod.clickDefaultLang(dialogFrameFixture, languageDialog);

        installerFrameFixture = HelperTestMethod.prepareFrameFixture(installerFrame, installerController);
        java.util.List panelList = installData.getPanels();
        // Hello panel
        Thread.sleep(600);
//        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Chack Panel
        Thread.sleep(600);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Licence Panel
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane(Timeout.timeout(1000)).focus();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();

        // Packs
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Summary
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Install
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Shortcut
        // Deselect shortcut creation
        if (!OsVersion.IS_MAC)
        {
            Thread.sleep(1000);
            installerFrameFixture.checkBox(GuiId.SHORTCUT_CREATE_CHECK_BOX.id).click();
            installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        }
        // Finish
//        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();


    }
}