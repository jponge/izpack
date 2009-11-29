package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.*;
import com.izforge.izpack.installer.provider.*;
import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.Timeout;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ProviderAdapter;

import java.awt.*;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation using mock data
 */
public class InstallationTest {

    private FrameFixture window;

    private File currentDir = new File(getClass().getClassLoader().getResource(".").getFile());

    @Rule
    public MethodRule globalTimeout = new Timeout(60000);
    
    private DefaultPicoContainer pico;
    private static final String APPNAME = "Test Installation";

    @Before
    public void initBinding() throws Throwable {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        file.delete();

        pico = new DefaultPicoContainer(new Caching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(GUIInstaller.class)
                .addComponent(AutomatedInstaller.class);
        PicoProvider.setPico(pico);
    }

    @After
    public void tearBinding() {
        if (window != null) {
            window.cleanUp();
        }
        pico.dispose();
    }

    @Test
    public void langpackEngShouldBeSet() throws Exception {
        compileAndUnzip("langpack", "engInstaller.xml");
        pico.getComponent(InstallerFrame.class);
        ResourceManager resourceManager = pico.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("eng"));
    }

    @Test
    public void langpackFraShouldBeSet() throws Exception {
        compileAndUnzip("langpack", "fraInstaller.xml");
        pico.getComponent(InstallerFrame.class);
        ResourceManager resourceManager = pico.getComponent(ResourceManager.class);
        assertThat(resourceManager.getLocale(), Is.is("fra"));
    }

    @Test
    public void testHelloAndFinishPanels() throws Exception {
        compileAndUnzip("samples", "helloAndFinish.xml");
        prepareFrame();
        // Hello panel
        window.requireSize(new Dimension(640, 480));
        window.button(GuiId.BUTTON_NEXT.id).click();
        window.requireVisible();
        // Finish panel
    }


    @Test
    public void testBasicInstall() throws Exception {
        compileAndUnzip("samples/basicInstall", "basicInstall.xml");
        prepareFrame();
        // Hello panel
        String[] strings = window.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents();
        System.out.println(strings);
        window.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(10000);
        // Finish panel
    }


    private void prepareFrame() {
        InstallerFrame installerFrame = pico.getComponent(InstallerFrame.class);
        window = new FrameFixture(installerFrame);
        window.show();
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
        pico.getComponent(ResourceManager.class).setResourceBasePath(relativePath + "/temp/resources/");
    }
}
