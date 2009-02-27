/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.panels;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

/**
 * Functions to support automated usage of the TargetPanel
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public class TargetPanelAutomationHelper implements PanelAutomation
{

    /**
     * Asks to make the XML panel data.
     *
     * @param idata     The installation data.
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        // Installation path markup
        IXMLElement ipath = new XMLElementImpl("installpath",panelRoot);
        // check this writes even if value is the default,
        // because without the constructor, default does not get set.
        ipath.setContent(idata.getInstallPath());

        // Checkings to fix bug #1864
        IXMLElement prev = panelRoot.getFirstChildNamed("installpath");
        if (prev != null)
        {
            panelRoot.removeChild(prev);
        }
        panelRoot.addChild(ipath);
    }

    /**
     * Asks to run in the automated mode.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML tree to read the data from.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        // We set the installation path
        IXMLElement ipath = panelRoot.getFirstChildNamed("installpath");

        // Allow for variable substitution of the installpath value
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        String path = ipath.getContent();
        path = vs.substitute(path, null);

        idata.setInstallPath(path);
    }
}
