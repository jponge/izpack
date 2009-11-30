package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.bootstrap.ApplicationComponentTest;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.*;
import org.apache.commons.io.FileUtils;
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
    private ApplicationComponentTest applicationComponentTest;
    private DialogFixture dialogFrameFixture;

    @Before
    public void initBinding() throws Throwable {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        file.delete();
        applicationComponentTest = new ApplicationComponentTest();
        applicationComponentTest.initBindings();
    }

    @After
    public void tearBinding() {
        if (installerFrameFixture != null) {
            installerFrameFixture.cleanUp();
        }
    }

    @Test
    public void langpackEngShouldBeSet() throws Exception {
        compileAndUnzip("langpack", "engInstaller.xml");
        applicationComponentTest.getComponent(LanguageDialog.class).initLangPack();
        ResourceManager resourceManager = applicationComponentTest.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void langpackFraShouldBeSet() throws Exception {
        compileAndUnzip("langpack", "fraInstaller.xml");
        applicationComponentTest.getComponent(LanguageDialog.class).initLangPack();
        ResourceManager resourceManager = applicationComponentTest.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }

    @Test
    public void testHelloAndFinishPanels() throws Exception {
        compileAndUnzip("samples", "helloAndFinish.xml");
        applicationComponentTest.getComponent(LanguageDialog.class).initLangPack();
        InstallerFrame installerFrame = applicationComponentTest.getComponent(InstallerFrame.class);
        installerFrameFixture = new FrameFixture(installerFrame);

        dialogFrameFixture.show();
        installerFrameFixture.show();  
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
        assertThat(dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(),Is.is(new String[]{"eng", "fra"}));
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        ResourceManager resourceManager = applicationComponentTest.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void testLangPickerChoseFra() throws Exception {
        compileAndUnzip("samples/basicInstall", "basicInstall.xml");
        prepareDialogFixture();
        assertThat(dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(),Is.is(new String[]{"eng", "fra"}));
        dialogFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).selectItem(1);
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        ResourceManager resourceManager = applicationComponentTest.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }

    private void prepareDialogFixture() {
        LanguageDialog languageDialog= applicationComponentTest.getComponent(LanguageDialog.class);
        dialogFrameFixture = new DialogFixture(languageDialog);
        dialogFrameFixture.show();
    }

//        InstallerFrame installerFrame = applicationComponentTest.getComponent(InstallerFrame.class);
//        installerFrameFixture = new FrameFixture(installerFrame);
//
//         Hello panel
//        String[] strings = installerFrameFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents();
//        System.out.println(strings);
//        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Finish panel

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
        applicationComponentTest.getComponent(ResourceManager.class).setResourceBasePath(relativePath + "/temp/resources/");
    }
}
