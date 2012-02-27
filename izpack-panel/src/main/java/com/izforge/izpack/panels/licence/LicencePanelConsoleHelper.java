/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.console.Console;
import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.PanelConsoleHelper;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * License Panel console helper
 */
public class LicencePanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter)
    {
        return true;
    }

    public boolean runConsoleFromProperties(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(AutomatedInstallData installData, Console console)
    {
        String license = null;
        String resNamePrefix = "LicencePanel.licence";
        try
        {
            // We read it
            license = ResourceManager.getInstance().getTextResource(resNamePrefix);
        }
        catch (Exception err)
        {
            license = "Error : could not load the licence text for defined resource " + resNamePrefix;
            console.println(license);
            return false;
        }

        // controls # of lines to display at a time, to allow simulated scrolling down
        int lines = 25;
        int lineNumber = 0;

        StringTokenizer tokenizer = new StringTokenizer(license, "\n");
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            console.println(token);
            lineNumber++;
            if (lineNumber >= lines)
            {
                if (!doContinue(console))
                {
                    return false;
                }
                lineNumber = 0;
            }

        }

        int i = askToAcceptLicense(console);

        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(installData, console);
        }

    }

    private boolean doContinue(Console console)
    {
        String value = prompt(console, "press Enter to continue, X to exit", "x");
        return !value.equalsIgnoreCase("x");
    }

    private int askToAcceptLicense(Console console)
    {
        return prompt(console, "press 1 to accept, 2 to reject, 3 to redisplay", 1, 3, 2);
    }

}
