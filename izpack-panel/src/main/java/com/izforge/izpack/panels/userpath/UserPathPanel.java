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

package com.izforge.izpack.panels.userpath;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;

/**
 * The target directory selection panel.
 *
 * @author Julien Ponge
 * @author Jeff Gordon
 */
public class UserPathPanel extends UserPathInputPanel
{
    private static final long serialVersionUID = 3256443616359429170L;

    private static final Logger logger = Logger.getLogger(UserPathPanel.class.getName());

    private boolean skip = false;

    public static String pathVariableName = "UserPathPanelVariable";
    public static String pathPackDependsName = "UserPathPanelDependsName";
    public static String pathElementName = "UserPathPanelElement";

    /**
     * Constructs an <tt>UserPathPanel</tt>.
     *
     * @param panel           the panel meta-data
     * @param parent          the parent window
     * @param installData     the installation data
     * @param resourceManager the resource manager
     * @param log             the log
     */
    public UserPathPanel(Panel panel, InstallerFrame parent, GUIInstallData installData,
                         ResourceManager resourceManager, Log log)
    {
        super(panel, parent, installData, UserPathPanel.class.getSimpleName(), resourceManager, log);
        // load the default directory info (if present)
        if (getDefaultDir() != null)
        {
            installData.setVariable(pathVariableName, getDefaultDir());
        }
    }

    @Override
    public void panelActivate()
    {
        boolean found = false;
        logger.fine("Looking for activation condition");
        // Need to have a way to supress panel if not in selected packs.
        String dependsName = installData.getVariable(pathPackDependsName);
        if (dependsName != null && !(dependsName.equalsIgnoreCase("")))
        {
            logger.fine("Checking for pack dependency of " + dependsName);
            for (Pack pack : installData.getSelectedPacks())
            {
                logger.fine("- Checking if " + pack.name + " equals " + dependsName);
                if (pack.name.equalsIgnoreCase(dependsName))
                {
                    found = true;
                    logger.fine("-- Found " + dependsName + ", panel will be shown");
                    break;
                }
            }
            skip = !(found);
        }
        else
        {
            logger.fine("Not Checking for a pack dependency, panel will be shown");
            skip = false;
        }
        if (skip)
        {
            logger.fine(UserPathPanel.class.getSimpleName() + " will not be shown");
            parent.skipPanel();
            return;
        }
        super.panelActivate();
        // Set the default or old value to the path selection panel.
        String expandedPath = installData.getVariable(pathVariableName);
        try
        {
            expandedPath = variableSubstitutor.substitute(expandedPath);
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, e.toString(), e);
            // ignore
        }
        _pathSelectionPanel.setPath(expandedPath);
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return Whether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        // Standard behavior of PathInputPanel.
        if (!super.isValidated())
        {
            return (false);
        }
        installData.setVariable(pathVariableName, _pathSelectionPanel.getPath());
        return (true);
    }

    /**
     * Asks to make the XML panel installDataGUI.
     *
     * @param panelRoot The tree to put the installDataGUI in.
     */
    @Override
    public void makeXMLData(IXMLElement panelRoot)
    {
        if (!(skip))
        {
            new UserPathPanelAutomationHelper(variableSubstitutor).makeXMLData(installData, panelRoot);
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
    */

    @Override
    public String getSummaryBody()
    {
        if (skip)
        {
            return null;
        }
        else
        {
            return (installData.getVariable(pathVariableName));
        }
    }
}
