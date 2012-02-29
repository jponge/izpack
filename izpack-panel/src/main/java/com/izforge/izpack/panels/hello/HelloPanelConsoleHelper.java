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

package com.izforge.izpack.panels.hello;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.installer.console.Console;
import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.PanelConsoleHelper;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Hello Panel console helper
 *
 * @author Mounir el hajj
 */
public class HelloPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    public boolean runConsoleFromProperties(AutomatedInstallData installData, Properties properties)
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
        LocaleDatabase langPack = installData.getLangpack();
        Info info = installData.getInfo();
        String welcomeText = langPack.getString("HelloPanel.welcome1") + info.getAppName() + " "
                + info.getAppVersion() + langPack.getString("HelloPanel.welcome2");
        console.println(welcomeText);
        ArrayList<Info.Author> authors = info.getAuthors();
        if (!authors.isEmpty())
        {
            console.println(langPack.getString("HelloPanel.authors"));

            for (Info.Author author : authors)
            {
                String email = (author.getEmail() != null && author.getEmail().length() > 0) ? (" <"
                        + author.getEmail() + ">") : "";
                console.println(" - " + author.getName() + email);
            }
        }

        if (info.getAppURL() != null)
        {
            String urlText = langPack.getString("HelloPanel.url") + info.getAppURL();
            console.println(urlText);
        }
        return promptEndPanel(installData, console);
    }
}
