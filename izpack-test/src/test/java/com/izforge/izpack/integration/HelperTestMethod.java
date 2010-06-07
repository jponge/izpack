package com.izforge.izpack.integration;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.language.LanguageDialog;
import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Shared methods beetween tests classes
 *
 * @author Anthonin Bonnefoy
 */
public class HelperTestMethod
{
    public static File prepareInstallation(GUIInstallData installData) throws IOException
    {
        File installPath = new File(installData.getInstallPath());
        FileUtils.deleteDirectory(installPath);
        assertThat(installPath.exists(), Is.is(false));
        return installPath;
    }

    public static void clickDefaultLang(DialogFixture dialogFrameFixture, LanguageDialog languageDialog)
    {
        dialogFrameFixture = prepareDialogFixture(languageDialog);
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        // Seems necessary to unlock window
        dialogFrameFixture.cleanUp();
        dialogFrameFixture = null;
    }

    /**
     * Prepare fest fixture for installer frame
     *
     * @param installerFrame
     * @param installerController
     * @throws Exception
     */
    public static FrameFixture prepareFrameFixture(InstallerFrame installerFrame, InstallerController installerController) throws Exception
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
     *
     * @param languageDialog
     */
    public static DialogFixture prepareDialogFixture(LanguageDialog languageDialog)
    {
        DialogFixture dialogFixture = new DialogFixture(languageDialog);
        dialogFixture.show();
        return dialogFixture;
    }

    public static void waitAndCheckInstallation(GUIInstallData installData, File installPath) throws InterruptedException
    {
        while (!installData.isCanClose())
        {
            Thread.sleep(500);
        }
        assertThat(installPath.exists(), Is.is(true));
        UninstallData uninstallData = new UninstallData();
        for (String installedFile : uninstallData.getInstalledFilesList())
        {
            File file = new File(installedFile);
            assertThat(file.exists(), Is.is(true));
        }
    }
}
