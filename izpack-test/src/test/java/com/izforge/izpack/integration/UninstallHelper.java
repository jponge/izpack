package com.izforge.izpack.integration;

import static com.izforge.izpack.test.util.TestHelper.assertFileExists;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.console.ConsoleUninstallerContainer;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.file.FileUtils;


/**
 * Uninstallation helper.
 *
 * @author Tim Anderson
 */
public class UninstallHelper
{

    /**
     * Uninstalls an application.
     * <p/>
     * This copies the uninstaller jar so that the original can be removed.
     *
     * @param installData the installation data
     * @throws Exception for any error
     */
    public static void uninstall(AutomatedInstallData installData) throws Exception
    {
        File uninstallerJar = getUninstallerJar(installData);
        assertFileExists(uninstallerJar);
        File copy = File.createTempFile("uninstaller", ".jar");
        copy.deleteOnExit();
        IoHelper.copyFile(uninstallerJar, copy);
        uninstall(copy);
        FileUtils.delete(copy); // probably won't delete as the class loader will still have a reference to it?
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     */
    public static File getUninstallerJar(AutomatedInstallData installData)
    {
        Info info = installData.getInfo();
        VariableSubstitutor replacer = new VariableSubstitutorImpl(installData.getVariables());
        String dir = IoHelper.translatePath(info.getUninstallerPath(), replacer);
        String path = dir + File.separator + info.getUninstallerName();
        return new File(path);
    }

    /**
     * Uninstalls the application at the specified path, by running te {@link Destroyer} in the supplied uninstall
     * jar.
     * <p/>
     * The Destroyer is launched in an isolated class loader as it locates resources using its class loader.
     * This also ensures it has all the classes it needs to run.
     *
     * @param uninstallJar the uninstall jar
     * @throws Exception for any error
     */
    public static void uninstall(File uninstallJar) throws Exception
    {
        // create an isolated class loader for loading classes and resources
        URLClassLoader loader = new URLClassLoader(new URL[]{uninstallJar.toURI().toURL()}, null);

        // get the container class
        Class containerClass = loader.loadClass(ConsoleUninstallerContainer.class.getName());

        // get the destroyer class
        Class destroyerClass = loader.loadClass(Destroyer.class.getName());

        Object container = containerClass.newInstance();
        Method getComponent = containerClass.getMethod("getComponent", Class.class);
        Object destroyer = getComponent.invoke(container, destroyerClass);
        Method forceDelete = destroyerClass.getMethod("setForceDelete", boolean.class);
        forceDelete.invoke(destroyer, true);

        // create the Destroyer and run it
        Method run = destroyerClass.getMethod("run");
        run.invoke(destroyer);
    }
}
