package com.izforge.izpack.integration;

import java.io.File;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.uninstaller.Destroyer;


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
        UninstallHelper.uninstall(uninstallJar);
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     */
    protected File getUninstallerJar()
    {
        return UninstallHelper.getUninstallerJar(getInstallData());
    }
}