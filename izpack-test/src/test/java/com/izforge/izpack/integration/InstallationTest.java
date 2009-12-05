package com.izforge.izpack.integration;

import com.izforge.izpack.bootstrap.IPanelContainer;
import com.izforge.izpack.installer.base.GuiId;
import com.izforge.izpack.installer.base.LanguageDialog;
import com.izforge.izpack.installer.data.GUIInstallData;
import org.apache.commons.io.FileUtils;
import org.fest.swing.exception.ScreenLockException;
import org.junit.After;
import org.junit.Test;

import java.awt.*;
import java.io.File;

/**
 * Test for an installation using mock data
 */
public class InstallationTest extends AbstractInstallationTest {

    @After
    public void tearBinding() {
        applicationContainer.dispose();
        try {
            if (dialogFrameFixture != null) {
                dialogFrameFixture.cleanUp();
                dialogFrameFixture = null;
            }
            if (installerFrameFixture != null) {
                installerFrameFixture.cleanUp();
                installerFrameFixture = null;
            }
        } catch (ScreenLockException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHelloAndFinishPanels() throws Exception {
        compileAndUnzip("helloAndFinish.xml", getWorkingDirectory("samples"));
        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        panelContainer.getComponent(LanguageDialog.class).initLangPack();
        prepareFrameFixture();

        // Hello panel
        installerFrameFixture.requireSize(new Dimension(640, 480));
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel
    }


    @Test
    public void testBasicInstall() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        GUIInstallData installData = applicationContainer.getComponent(GUIInstallData.class);

        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        FileUtils.deleteDirectory(new File(installData.getInstallPath()));
        // Lang picker
        prepareDialogFixture();
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        // Seems necessary to unlock window
        dialogFrameFixture.cleanUp();
        dialogFrameFixture = null;

        prepareFrameFixture();
        // Hello panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Info Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Licence Panel
        installerFrameFixture.radioButton(GuiId.LICENCE_NO_RADIO.id).requireSelected();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();
        // Packs Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install Panel
//        installerFrameFixture.optionPane().requireEnabled();
        // Finish panel
    }

}
