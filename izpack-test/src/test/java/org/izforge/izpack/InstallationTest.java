package org.izforge.izpack;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an end-to-end installation
 */
public class InstallationTest {

    @Test
    public void testInstallSamples1() throws Exception {
        URL urls[] = {};

        JarFileLoader jarLoader = new JarFileLoader(urls);
        File file = new File(getClass().getClassLoader().getResource("samples1/out.jar").getFile());
        assertThat(file.exists(), Is.is(true));
        jarLoader.addFile(file.getAbsolutePath());
        Class c = jarLoader.loadClass("com.izforge.izpack.installer.Installer");

        Object o = c.newInstance();
        Class[] argTypes = new Class[]{String[].class};
        Method main = c.getDeclaredMethod("main", argTypes);
        System.out.format("invoking %s.main()%n", c.getName());
        String[] mainArgs = new String[0];
        main.invoke(null, (Object) mainArgs);

        Thread.sleep(1000);

//        Installer.main(new String[]{""});

//            cl.loadClass("org.gjt.mm.mysql.Driver");

    }
}
