package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.bootstrap.ApplicationComponent;
import com.izforge.izpack.bootstrap.IApplicationComponent;
import com.izforge.izpack.bootstrap.IPanelComponent;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.GuiId;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.LanguageDialog;
import org.apache.commons.io.FileUtils;
import org.fest.swing.exception.ScreenLockException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.Timeout;

import java.awt.*;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation using mock data
 */
public class InstallationTest {

    private FrameFixture installerFrameFixture;

    private File currentDir = new File(getClass().getClassLoader().getResource(".").getFile());

    @Rule
    public MethodRule globalTimeout = new Timeout(60000);

    private static final String APPNAME = "Test Installation";
    private IApplicationComponent applicationComponent;
    private IPanelComponent panelComponent;
    private DialogFixture dialogFrameFixture;
    private InstallerFrame installerFrame;
    private LanguageDialog languageDialog;

    @Before
    public void initBinding() throws Throwable {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        file.delete();
        applicationComponent = new ApplicationComponent();
        applicationComponent.initBindings();
        panelComponent = applicationComponent.getComponent(IPanelComponent.class);
    }

    @After
    public void tearBinding() {
        applicationComponent.dispose();
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
    public void langpackEngShouldBeSet() throws Exception {
        compileAndUnzip("langpack", "engInstaller.xml");
        panelComponent.getComponent(LanguageDialog.class).initLangPack();
        ResourceManager resourceManager = applicationComponent.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void langpackFraShouldBeSet() throws Exception {
        compileAndUnzip("langpack", "fraInstaller.xml");
        panelComponent.getComponent(LanguageDialog.class).initLangPack();
        ResourceManager resourceManager = applicationComponent.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }

    @Test
    public void testHelloAndFinishPanels() throws Exception {
        compileAndUnzip("samples", "helloAndFinish.xml");
        panelComponent.getComponent(LanguageDialog.class).initLangPack();
        prepareFrameFixture();

        // Hello panel
        installerFrameFixture.requireSize(new Dimension(640, 480));
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel
    }


    @Test
    public void testLangPickerChoseEng() throws Exception {
        compileAndUnzip("samples/basicInstall", "basicInstall.xml");
        prepareDialogFixture();
        assertThat(dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(), Is.is(new String[]{"eng", "fra"}));
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        ResourceManager resourceManager = applicationComponent.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void testLangPickerChoseFra() throws Exception {
        compileAndUnzip("samples/basicInstall", "basicInstall.xml");
        prepareDialogFixture();
        assertThat(dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(), Is.is(new String[]{"eng", "fra"}));
        dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).selectItem(1);
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        ResourceManager resourceManager = applicationComponent.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }

    @Test
    public void testBasicInstall() throws Exception {
        compileAndUnzip("samples/basicInstall", "basicInstall.xml");
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
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Packs Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Finish panel
    }

    private void prepareFrameFixture() throws Exception {
        installerFrame = panelComponent.getComponent(InstallerFrame.class);
        installerFrame.loadPanels();
        installerFrameFixture = new FrameFixture(installerFrame);
        installerFrameFixture.show();
    }

    private void prepareDialogFixture() {
        languageDialog = panelComponent.getComponent(LanguageDialog.class);
        dialogFrameFixture = new DialogFixture(languageDialog);
        dialogFrameFixture.show();
    }


    /**
     * Compile an installer and unzip the created jar.
     *
     * @param workingDirectoryName Working directory of the installer
     * @param installationFile     The izpack installation file
     * @throws Exception
     */
    private void compileAndUnzip(String workingDirectoryName, String installationFile) throws Exception {
        File workingDirectory = new File(getClass().getClassLoader().getResource(workingDirectoryName).getFile());
        File installerFile = new File(workingDirectory, installationFile);
        File out = new File(workingDirectory, "out.jar");
        compileAndUnzip(installerFile, workingDirectory, out);
    }

    /**
     * Compile an installer and unzip it.
     *
     * @param installerFile The izpack installer file
     * @param baseDir       The directory containing the installer file
     * @param out           The output of the compiler
     * @throws Exception
     */
    private void compileAndUnzip(File installerFile, File baseDir, File out) throws Exception {
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", out.getAbsolutePath());
        c.executeCompiler();
        File extractedDir = new File(baseDir, "temp");
        // Clean before use 
        FileUtils.deleteDirectory(extractedDir);
        extractedDir.mkdirs();
        AssertionHelper.unzipJar(out, extractedDir);

        String relativePath = baseDir.getAbsolutePath().substring(currentDir.getAbsolutePath().length());
        System.out.println(relativePath);
        applicationComponent.getComponent(ResourceManager.class).setResourceBasePath(relativePath + "/temp/resources/");
    }
}
