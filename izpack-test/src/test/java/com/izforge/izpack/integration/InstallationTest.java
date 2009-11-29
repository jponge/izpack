package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.data.AutomatedInstallData;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.AutomatedInstaller;
import com.izforge.izpack.installer.base.ConsoleInstaller;
import com.izforge.izpack.installer.base.GUIInstaller;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.installer.provider.*;
import org.fest.swing.fixture.FrameFixture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.ThreadCaching;
import org.picocontainer.injectors.ProviderAdapter;

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
    public void setUp() throws Exception {
    }

    @Test
    public void testInstallSamples1() throws Exception {
        compileAndUnzip();
    }

    private void compileAndUnzip() throws Exception {
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", out.getAbsolutePath());
        c.executeCompiler();
        File extractedDir = new File(getClass().getClassLoader().getResource("samples1").getFile(),"temp");

        AssertionHelper.unzipJar(out,extractedDir);
        initBindings();
        pico.getComponent(ResourceManager.class).setResourceBasePath("/samples1/temp/resources/");
        pico.getComponent(InstallData.class);
        InstallerFrame installerFrame = pico.getComponent(InstallerFrame.class);
        installerFrame.enableFrame();
        Thread.sleep(1000);
    }


    private void initBindings() {
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
}
