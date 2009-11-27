package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.bootstrap.AutomatedInstaller;
import com.izforge.izpack.installer.bootstrap.ConsoleInstaller;
import com.izforge.izpack.installer.provider.*;
import org.fest.swing.fixture.FrameFixture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.picocontainer.DefaultPicoContainer;
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
        initBindings("samples1/temp/");
        InstallerFrame installerFrame = pico.getComponent(InstallerFrame.class);
        installerFrame.enableFrame();
        Thread.sleep(1000);
    }


    private void initBindings(String path) {
        pico = new DefaultPicoContainer(new ThreadCaching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider(path)))
                .addAdapter(new ProviderAdapter(new GUIInstallerProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(ConsoleInstaller.class)
                .addComponent(AutomatedInstaller.class);
    }
}
