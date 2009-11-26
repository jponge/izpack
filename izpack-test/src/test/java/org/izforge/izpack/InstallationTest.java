package org.izforge.izpack;

import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an end-to-end installation
 */
public class InstallationTest {

    private FrameFixture window;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(getClass());
        configureStub();
        URL urls[] = {};
        JarFileLoader jarLoader = new JarFileLoader(urls);
        File file = new File(getClass().getClassLoader().getResource("samples1/out.jar").getFile());
        assertThat(file.exists(), Is.is(true));
        jarLoader.addFile(file.getAbsolutePath());
        Class c = jarLoader.loadClass("com.izforge.izpack.installer.GUIInstaller");
        Object guiInstallerInstance = c.newInstance();
//        Constructor ctor = c.getDeclaredConstructor(new Class[]{String.class, InstallData.class, InstallerBase.class});
//        ctor.setAccessible(true);

        Object res = c.getMethod("getInstallerFrame").invoke(guiInstallerInstance);
        window = new FrameFixture((Frame) res );
        window.show();
        Thread.sleep(10000);
    }

    private void configureStub() {
    }

    @Test
    public void testInstallSamples1() throws Exception {


//        Installer.main(new String[]{""});

//            cl.loadClass("org.gjt.mm.mysql.Driver");

    }
}