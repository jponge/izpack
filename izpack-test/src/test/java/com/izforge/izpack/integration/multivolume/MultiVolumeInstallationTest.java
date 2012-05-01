package com.izforge.izpack.integration.multivolume;

import java.io.File;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestGUIInstallerContainer;
import com.izforge.izpack.compiler.packager.impl.MultiVolumePackager;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpacker;
import com.izforge.izpack.integration.HelperTestMethod;


/**
 * Tests installation when using the {@link MultiVolumePackager} and {@link MultiVolumeUnpacker}.
 *
 * @author Tim Anderson
 */
public class MultiVolumeInstallationTest extends AbstractMultiVolumeInstallationTest
{

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
     * Creates the installer container.
     *
     * @return a new installer container
     */
    @Override
    protected InstallerContainer createInstallerContainer()
    {
        return new TestGUIInstallerContainer();
    }

    /**
     * Performs the installation.
     *
     * @param container   the installer
     * @param installData the installation data
     * @param installPath the installation path
     * @throws Exception for any error
     */
    protected void install(InstallerContainer container, AutomatedInstallData installData, File installPath)
            throws Exception
    {
        InstallerController controller = container.getComponent(InstallerController.class);
        LanguageDialog languageDialog = container.getComponent(LanguageDialog.class);
        InstallerFrame installerFrame = container.getComponent(InstallerFrame.class);

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

        // Install Panel
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);
        fixture.button(GuiId.BUTTON_NEXT.id).click();

        // Finish panel
        Thread.sleep(1200);
        fixture.button(GuiId.BUTTON_QUIT.id).click();
    }


}
