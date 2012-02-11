package com.izforge.izpack.integration;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.util.IoHelper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertTrue;

/**
 * Base class for integration tests invoking the {@link Destroyer}.
 *
 * @author Tim Anderson
 */
public class AbstractDestroyerTest
{

    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The install data.
     */
    private AutomatedInstallData installData;


    /**
     * Constructs an <tt>AbstractDestroyerTest</tt>
     *
     * @param installData the install data
     */
    public AbstractDestroyerTest(AutomatedInstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Sets up the test case.
     *
     * @throws java.io.IOException if the install directory cannot be created
     */
    @Before
    public void setUp() throws IOException
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        assertTrue(installPath.mkdirs());
        installData.setInstallPath(installPath.getAbsolutePath());
    }


    /**
     * Runs the {@link Destroyer} in the supplied uninstall jar.
     * <p/>
     * The Destroyer is launched in an isolated class loader as it locates resources using its class loader
     *
     * @param uninstallJar the uninstaller jar
     * @throws Exception for any error
     */
    protected void runDestroyer(File uninstallJar) throws Exception
    {
        // create an isolated class loader
        URLClassLoader loader = new URLClassLoader(new URL[]{uninstallJar.toURI().toURL()}, null);
        Class destroyerClass = loader.loadClass(Destroyer.class.getName());
        Class<?> handlerClass = loader.loadClass(AbstractUIProgressHandler.class.getName());
        Constructor constructor = destroyerClass.getConstructors()[0];

        // create the Destroyer
        String installPath = getInstallPath();
        Object destroyer = constructor.newInstance(installPath, true, Mockito.mock(handlerClass));

        // and run it
        Method method = destroyerClass.getMethod("run");
        method.invoke(destroyer);
    }

    /**
     * Returns the install path.
     *
     * @return the install path
     */
    protected String getInstallPath()
    {
        return installData.getInstallPath();
    }

    /**
     * Returns the install data.
     *
     * @return the install data
     */
    protected AutomatedInstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @param substituter the variable substituter
     * @return the uninstaller jar file
     */
    protected File getUninstallerJar(VariableSubstitutor substituter)
    {
        String dir = IoHelper.translatePath(installData.getInfo().getUninstallerPath(), substituter);
        String path = dir + File.separator + installData.getInfo().getUninstallerName();
        File jar = new File(path);
        MatcherAssert.assertThat(jar.exists(), Is.is(true));
        return jar;
    }
}