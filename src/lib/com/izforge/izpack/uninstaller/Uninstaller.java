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

import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.installer.PrivilegedRunner;
import com.izforge.izpack.util.OsVersion;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * The uninstaller class.
 *
 * @author Julien Ponge
 */
public class Uninstaller
{

    /**
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command line.
     */
    public static void main(String[] args)
    {
        checkForPrivilegedExecution();

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

    private static void checkForPrivilegedExecution()
    {
        if (PrivilegedRunner.isPrivilegedMode())
        {
            // We have been launched through a privileged execution, so stop the checkings here!
            return;
        }

        if (elevationShouldBeInvestigated())
        {
            PrivilegedRunner runner = new PrivilegedRunner();
            if (runner.isPlatformSupported() && runner.isElevationNeeded())
            {
                try
                {
                    if (runner.relaunchWithElevatedRights() == 0)
                    {
                        System.exit(0);
                    }
                    else
                    {
                        throw new RuntimeException("Launching an uninstaller with elevated permissions failed.");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The uninstaller could not launch itself with administrator permissions.\n" +
                        "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
                }
            }
            else if (!runner.isPlatformSupported())
            {
                JOptionPane.showMessageDialog(null, "This uninstaller should be run by an administrator.\n" +
                    "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
            }
        }
    }

    private static boolean elevationShouldBeInvestigated()
    {
        return (Uninstaller.class.getResource("/exec-admin") != null) ||
                (OsVersion.IS_WINDOWS && !(new PrivilegedRunner().canWriteToProgramFiles()));
    }

    public static void cmduninstall(String[] args)
    {
        try
        {
            UninstallerConsole uco = new UninstallerConsole();
            boolean force = false;
            for (String arg : args)
            {
                if (arg.equals("-f"))
                {
                    force = true;
                }
            }
            System.out.println("Force deletion: " + force);
            uco.runUninstall(force);
        }
        catch (Exception err)
        {
            System.err.println("- Error -");
            err.printStackTrace();
            Housekeeper.getInstance().shutDown(0);
        }
    }

    public static void uninstall(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
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
                    new UninstallerFrame(displayForceOption, forceOptionState);
                }
                catch (Exception err)
                {
                    System.err.println("- Error -");
                    err.printStackTrace();
                    Housekeeper.getInstance().shutDown(0);
                }
            }
        });
    }
}
