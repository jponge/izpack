/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.uninstaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.util.SelfModifier;

/**
 * The uninstaller class.
 *
 * @author Julien Ponge
 * @author Tim Anderson
 */
public class Uninstaller
{
    /**
     * The install.log resource path.
     */
    private static final String INSTALL_LOG = "/install.log";

    /**
     * The exec-admin resource path.
     */
    private static final String EXEC_ADMIN = "/exec-admin";

    /**
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command line.
     */
    public static void main(String[] args)
    {
        // relaunch the uninstaller with elevated permissions if required
        if (!PrivilegedRunner.isPrivilegedMode() && isElevationRequired())
        {
            if (relaunchWithElevatedRights())
            {
                System.exit(0);
            }
        }

        boolean cmduninstall = false;
        for (String arg : args)
        {
            if (arg.equals("-c"))
            {
                cmduninstall = true;
            }
        }
        if (cmduninstall)
        {
            System.out.println("Command line uninstaller.\n");
        }
        try
        {
            Class<Uninstaller> clazz = Uninstaller.class;
            Method target;
            if (cmduninstall)
            {
                target = clazz.getMethod("cmduninstall", new Class[]{String[].class});
            }
            else
            {
                target = clazz.getMethod("uninstall", new Class[]{String[].class});
            }
            new SelfModifier(target).invoke(args);
        }
        catch (Exception ioeOrTypo)
        {
            System.err.println(ioeOrTypo.getMessage());
            ioeOrTypo.printStackTrace();
            System.err.println("Unable to exec java as a subprocess.");
            System.err.println("The uninstall may not fully complete.");
            uninstall(args);
        }
    }

    public static void cmduninstall(String[] args)
    {
        UninstallerContainer container = createContainer();
        try
        {
            UninstallerConsole uninstallerConsole = container.getComponent(UninstallerConsole.class);
            boolean force = false;
            for (String arg : args)
            {
                if (arg.equals("-f"))
                {
                    force = true;
                }
            }
            System.out.println("Force deletion: " + force);
            uninstallerConsole.runUninstall(force);
        }
        catch (Exception err)
        {
            System.err.println("- Error -");
            err.printStackTrace();
            container.getComponent(Housekeeper.class).shutDown(0);
        }
    }

    public static void uninstall(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                UninstallerContainer container = createContainer();
                try
                {
                    boolean displayForceOption = true;
                    boolean forceOptionState = false;

                    for (String arg : args)
                    {
                        if (arg.equals("-f"))
                        {
                            forceOptionState = true;
                        }
                        else if (arg.equals("-x"))
                        {
                            displayForceOption = false;
                        }
                    }

                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    UninstallerFrame uninstaller = container.getComponent(UninstallerFrame.class);
                    uninstaller.init(displayForceOption, forceOptionState);
                }
                catch (Exception err)
                {
                    System.err.println("- Error -");
                    err.printStackTrace();
                    container.getComponent(Housekeeper.class).shutDown(0);
                }
            }
        });
    }

    private static UninstallerContainer createContainer()
    {
        return new UninstallerContainer();
    }

    /**
     * Attempts to relaunch the uninstaller with elevated permissions.
     *
     * @return <tt>true</tt> if the relaunch was successful, otherwise <tt>false</tt>
     */
    private static boolean relaunchWithElevatedRights()
    {
        boolean result = false;
        PrivilegedRunner runner = new PrivilegedRunner();
        if (runner.isPlatformSupported())
        {
            try
            {
                if (runner.relaunchWithElevatedRights() == 0)
                {
                    result = true;
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
            if (!result)
            {
                JOptionPane.showMessageDialog(null,
                                              "The uninstaller could not launch itself with administrator permissions.\n" +
                                                      "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "This uninstaller should be run by an administrator.\n" +
                    "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
        }
        return result;
    }

    /**
     * Determines if permission elevation is required to uninstall the application.
     * <p/>
     * Permission elevation is required if:
     * <ul>
     * <li>the <em>exec-admin</em> resource exists; and</li>
     * <li>the current user doesn't have permission to write to the install path</li>
     * </ul>
     *
     * @return <tt>true</tt> if elevation is needed
     * @throws IzPackException if the installation path cannot be determined
     */
    private static boolean isElevationRequired()
    {
        boolean result = false;
        if (Uninstaller.class.getResource(EXEC_ADMIN) != null)
        {
            String path = getInstallPath();
            PrivilegedRunner runner = new PrivilegedRunner();
            result = runner.isElevationNeeded(path);
        }
        return result;
    }

    /**
     * Gets the installation path from the log file.
     *
     * @return the install path
     * @throws IzPackException if the <em>install.log</em> resource cannot be read
     */
    private static String getInstallPath()
    {
        String installPath;
        try
        {
            InputStream in = Uninstaller.class.getResourceAsStream(INSTALL_LOG);
            if (in == null)
            {
                throw new IzPackException(INSTALL_LOG + " resource not found");
            }
            InputStreamReader inReader = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inReader);
            installPath = reader.readLine();
            reader.close();
        }
        catch (IOException exception)
        {
            throw new IzPackException(exception);
        }
        return installPath;
    }

}
