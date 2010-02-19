/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.adaptator.IXMLElement;

import java.util.Iterator;

/**
 * The target directory selection panel.
 *
 * @author Julien Ponge
 * @author Jeff Gordon
 */
public class UserPathPanel extends UserPathInputPanel
{

    private static final long serialVersionUID = 3256443616359429170L;

    private static String thisName = "UserPathPanel";

    private boolean skip = false;

    public static String pathVariableName = "UserPathPanelVariable";
    public static String pathPackDependsName = "UserPathPanelDependsName";
    public static String pathElementName = "UserPathPanelElement";

    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public UserPathPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, thisName, parent.langpack.getString(thisName + ".variableName"));
        // load the default directory info (if present)
        if (getDefaultDir() != null)
        {
            idata.setVariable(pathVariableName, getDefaultDir());
        }
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        boolean found = false;
        Debug.trace(thisName + " looking for activation condition");
        // Need to have a way to supress panel if not in selected packs.
        String dependsName = idata.getVariable(pathPackDependsName);
        if (dependsName != null && !(dependsName.equalsIgnoreCase("")))
        {
            Debug.trace("Checking for pack dependency of " + dependsName);
            Iterator iter = idata.selectedPacks.iterator();
            while (iter.hasNext())
            {
                Pack pack = (Pack) iter.next();
                Debug.trace("- Checking if " + pack.name + " equals " + dependsName);
                if (pack.name.equalsIgnoreCase(dependsName))
                {
                    found = true;
                    Debug.trace("-- Found " + dependsName + ", panel will be shown");
                    break;
                }
            }
            skip = !(found);
        }
        else
        {
            Debug.trace("Not Checking for a pack dependency, panel will be shown");
            skip = false;
        }
        if (skip)
        {
            Debug.trace(thisName + " will not be shown");
            parent.skipPanel();
            return;
        }
        super.panelActivate();
        // Set the default or old value to the path selection panel.
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
		String expandedPath = vs.substitute(idata.getVariable(pathVariableName), null);
		_pathSelectionPanel.setPath(expandedPath);
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return Whether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        // Standard behavior of PathInputPanel.
        if (!super.isValidated())
        {
            return (false);
        }
        idata.setVariable(pathVariableName, _pathSelectionPanel.getPath());
        return (true);
    }

    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        if (!(skip))
        {
            new UserPathPanelAutomationHelper().makeXMLData(idata, panelRoot);
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
    */
    public String getSummaryBody()
    {
        if (skip)
        {
            return null;
        }
        else
        {
            return (idata.getVariable(pathVariableName));
        }
    }
}
