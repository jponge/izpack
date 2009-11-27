package com.izforge.izpack;

import org.fest.swing.fixture.FrameFixture;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test for an end-to-end installation
 */
public class InstallationTest {

    private FrameFixture window;

    @Before
    public void setUp() throws Exception {
        URL urls[] = {};
        JarFileLoader jarLoader = new JarFileLoader(urls);
        File file = new File(getClass().getClassLoader().getResource("samples1/out.jar").getFile());
        assertThat(file.exists(), is(true));
        jarLoader.addFile(file.getAbsolutePath());
        Class<InstallerJarManager> jarManagerClass = (Class<InstallerJarManager>) jarLoader.loadClass("com.izforge.izpack.InstallerJarManager");
        InstallerJarManager installerJarManager = jarManagerClass.newInstance();

        Frame frame = installerJarManager.getFrame();
        window = new FrameFixture((Frame) frame);
    }


    @Test
    public void testInstallSamples1() throws Exception {
        window.show();        
    }

}