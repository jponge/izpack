/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
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

import java.lang.reflect.Method;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
        try
        {
            Class clazz = Uninstaller.class;
            Method target = clazz.getMethod("uninstall", new Class[] { String[].class});
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

    public static void uninstall(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    new UninstallerFrame();
                }
                catch (Exception err)
                {
                    System.err.println("- Error -");
                    err.printStackTrace();
                    System.exit(0);
                }
            }
        });
    }
}
