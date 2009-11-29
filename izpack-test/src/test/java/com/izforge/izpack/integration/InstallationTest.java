package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.*;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.installer.provider.*;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

    private DefaultPicoContainer pico;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File baseDir = new File(getClass().getClassLoader().getResource("samples1").getFile());
    private File installerFile = new File(getClass().getClassLoader().getResource("samples1/install.xml").getFile());
    private File out = new File(baseDir, "out.jar");

    @Before
    public void initBinding() throws Exception {
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
    }

    @After
    public void tearBinding() {
        pico.dispose();
    }

    @Test
    public void testInstallSamples1() throws Exception {
        compileAndUnzip();
        // Hello panel
        window.requireSize(new Dimension(640,480));
        window.button(GuiId.NEXT_BUTTON.id).click();
        window.requireVisible();
        // Finish panel
        window.button(GuiId.QUIT_BUTTON.id).click();
        window.requireNotVisible();
    }


    private void compileAndUnzip() throws Exception {
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", out.getAbsolutePath());
        c.executeCompiler();
        File extractedDir = new File(getClass().getClassLoader().getResource("samples1").getFile(), "temp");

        AssertionHelper.unzipJar(out, extractedDir);
        pico.getComponent(ResourceManager.class).setResourceBasePath("/samples1/temp/resources/");
        InstallData installData = pico.getComponent(InstallData.class);
        InstallerFrame installerFrame = pico.getComponent(InstallerFrame.class);
        assertThat(installData.getLangpack().getString("installer.yes"), Is.is("Yes"));
        window = new FrameFixture(installerFrame);
        window.show();
    }
}
