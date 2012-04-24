package com.izforge.izpack.integration;

import static com.izforge.izpack.test.util.TestHelper.assertFileExists;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.UninstallerContainer;
import com.izforge.izpack.util.IoHelper;


/**
 * Base class for integration tests invoking the {@link Destroyer}.
 *
 * @author Tim Anderson
 */
public class AbstractDestroyerTest extends AbstractInstallationTest
{

    /**
     * Constructs an <tt>AbstractDestroyerTest</tt>
     *
     * @param installData the install data
     */
    public AbstractDestroyerTest(AutomatedInstallData installData)
    {
        super(installData);
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

        // create the container using the isolated class loader
        Class containerClass = loader.loadClass(UninstallerContainer.class.getName());
        Object container = containerClass.newInstance();

        // create the Destroyer
        String installPath = getInstallPath();
        Object destroyer = constructor.newInstance(installPath, true, Mockito.mock(handlerClass), container);

        // and run it
        Method method = destroyerClass.getMethod("run");
        method.invoke(destroyer);
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     */
    protected File getUninstallerJar()
    {
        return getUninstallerJar(new VariableSubstitutorImpl(getInstallData().getVariables()));
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @param substituter the variable substituter
     * @return the uninstaller jar file
     */
    protected File getUninstallerJar(VariableSubstitutor substituter)
    {
        Info info = getInstallData().getInfo();
        String dir = IoHelper.translatePath(info.getUninstallerPath(), substituter);
        String path = dir + File.separator + info.getUninstallerName();
        File jar = new File(path);
        assertFileExists(jar);
        return jar;
    }
}