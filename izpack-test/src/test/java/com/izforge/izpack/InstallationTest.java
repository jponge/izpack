package com.izforge.izpack;

import com.izforge.izpack.installer.AutomatedInstaller;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.GUIInstaller;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.provider.*;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;

import java.awt.*;
import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an end-to-end installation
 */
public class InstallationTest {

    private FrameFixture window;
    private DefaultPicoContainer pico;

    @Before
    public void setUp() throws Exception {
        URL urls[] = {};
        JarFileLoader jarLoader = new JarFileLoader(urls);
        File file = new File(getClass().getClassLoader().getResource("samples1/out.jar").getFile());
        assertThat(file.exists(), Is.is(true));
        jarLoader.addFile(file.getAbsolutePath());
        Class c = jarLoader.loadClass("com.izforge.izpack.installer.InstallerFrame");
        jarLoader.loadClass("org.picocontainer.ComponentFactory");
//        Class c = jarLoader.loadClass("org.pico");
        initBinding("en");
//        Object guiInstallerInstance = c.newInstance();
//        Constructor ctor = c.getDeclaredConstructor(new Class[]{String.class, InstallData.class, InstallerBase.class});
//        ctor.setAccessible(true);
//        Object res = c.getMethod("getInstallerFrame").invoke(guiInstallerInstance);
        Frame frame =pico.getComponent(InstallerFrame.class);

        window = new FrameFixture((Frame) frame);
        window.show();
    }


    @Test
    public void testInstallSamples1() throws Exception {
//        Installer.main(new String[]{""});
//            cl.loadClass("org.gjt.mm.mysql.Driver");

    }

    private void initBinding(String langcode) {
        pico = new DefaultPicoContainer(new ConstructorInjection());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new GUIInstallerProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(ConsoleInstaller.class, ConsoleInstaller.class,
                        new ComponentParameter(),
                        new ComponentParameter(),
                        new ConstantParameter(langcode))
                .addComponent(AutomatedInstaller.class);
    }
}