package org.izforge.izpack;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an end-to-end installation
 */
public class InstallationTest {

    @Test
    public void testInstallSamples1() throws MalformedURLException {
        URL urls[] = {};
        JarFileLoader cl = new JarFileLoader(urls);
        File file = new File(getClass().getClassLoader().getResource("samples1/out.jar").getFile());
        assertThat(file.exists(), Is.is(true));
        cl.addFile(file.getAbsolutePath());
//            cl.loadClass("org.gjt.mm.mysql.Driver");

    }
}
