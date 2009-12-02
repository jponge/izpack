package com.izforge.izpack.integration;

import com.izforge.izpack.bootstrap.IPanelContainer;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.GuiId;
import com.izforge.izpack.installer.base.LanguageDialog;
import org.fest.swing.exception.ScreenLockException;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation using mock data
 */
public class LanguageSelectionTest extends AbstractInstallationTest {

    @After
    public void tearBinding() {
        applicationContainer.dispose();
        try {
            if (dialogFrameFixture != null) {
                dialogFrameFixture.cleanUp();
                dialogFrameFixture = null;
            }
        } catch (ScreenLockException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void langpackEngShouldBeSet() throws Exception {
        compileAndUnzip("engInstaller.xml", getWorkingDirectory("langpack"));
        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        panelContainer.getComponent(LanguageDialog.class).initLangPack();
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void langpackFraShouldBeSet() throws Exception {
        compileAndUnzip("fraInstaller.xml", getWorkingDirectory("langpack"));
        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        panelContainer.getComponent(LanguageDialog.class).initLangPack();
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }

    @Test
    public void testLangPickerChoseEng() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        prepareDialogFixture();
        assertThat(dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(), Is.is(new String[]{"eng", "fra"}));
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void testLangPickerChoseFra() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        prepareDialogFixture();
        assertThat(dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(), Is.is(new String[]{"eng", "fra"}));
        dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).selectItem(1);
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }


}