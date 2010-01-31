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
public class HelloPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {

    public boolean runConsoleFromProperties(AutomatedInstallData installData, Properties p) {
        return true;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
                                             PrintWriter printWriter) {
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata) {
        String str;
        str = idata.getLangpack().getString("HelloPanel.welcome1") + idata.getInfo().getAppName() + " "
                + idata.getInfo().getAppVersion() + idata.getLangpack().getString("HelloPanel.welcome2");
        System.out.println(str);
        ArrayList<Info.Author> authors = idata.getInfo().getAuthors();
        int size = authors.size();
        if (size > 0) {
            str = idata.getLangpack().getString("HelloPanel.authors");

            for (int i = 0; i < size; i++) {
                Info.Author a = authors.get(i);
                String email = (a.getEmail() != null && a.getEmail().length() > 0) ? (" <"
                        + a.getEmail() + ">") : "";
                System.out.println(" - " + a.getName() + email);
            }

        }

        if (idata.getInfo().getAppURL() != null) {
            str = idata.getLangpack().getString("HelloPanel.url") + idata.getInfo().getAppURL();
            System.out.println(str);
        }
        int i = askEndOfConsolePanel();
        if (i == 1) {
            return true;
        } else if (i == 2) {
            return false;
        } else {
            return runConsole(idata);
        }
    }
}
