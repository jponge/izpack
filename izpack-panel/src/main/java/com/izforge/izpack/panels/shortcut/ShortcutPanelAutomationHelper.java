/*
 * $Id: copyright-notice-template 1421 2006-03-12 16:32:32Z jponge $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Marc Eppelmann (marc.eppelmann&#064;gmx.de)
 * Copyright 2010 Florian Buehlmann
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
package com.izforge.izpack.panels.shortcut;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;

/**
 * The ShortcutPanelAutomationHelper is responsible to create Shortcuts during the automated
 * installation.
 * 
 * @author Marc Eppelmann (marc.eppelmann&#064;gmx.de)
 * @version $Revision: 1540 $
 */
public class ShortcutPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation
{

    private UninstallData uninstallData;

    private VariableSubstitutor variableSubstitutor;

    private ResourceManager resourceManager;

    private ShortcutPanelLogic shortcutPanelLogic;

    /**
     * @param resourceManager
     * @param uninstallData
     * @param variableSubstitutor
     */
    public ShortcutPanelAutomationHelper(ResourceManager resourceManager,
            UninstallData uninstallData, VariableSubstitutor variableSubstitutor)
    {
        super();
        this.resourceManager = resourceManager;
        this.uninstallData = uninstallData;
        this.variableSubstitutor = variableSubstitutor;
        try
        {
            shortcutPanelLogic = ShortcutPanelLogic.getInstance();
        }
        catch (Exception e)
        {
            Housekeeper.getInstance().shutDown(4);
        }
    }

    /**
     * Create the xml configuration content for automatic installation. Normally this method is not
     * used because we are in an automatic installation step.
     * 
     * @param installData Installation data
     * @param panelRoot panel specific data for autoinstall.xml
     */
    public void makeXMLData(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        Debug.log("entering makeXMLData");
        for (IXMLElement element : shortcutPanelLogic.getAutoinstallXMLData())
        {
            panelRoot.addChild(element);
        }
    }

    /**
     * Implementation of the Shortcut specific automation code.
     * 
     * @param installData Installation data
     * @param panelRoot panel specific data from autoinstall.xml
     */
    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        try
        {
            shortcutPanelLogic.initInstance(installData, resourceManager, uninstallData,
                    variableSubstitutor);
        }
        catch (Exception e)
        {
            Housekeeper.getInstance().shutDown(4);
        }
        shortcutPanelLogic.setAutoinstallXMLData(panelRoot);
        if (shortcutPanelLogic.isCreateShortcutsImmediately())
        {
            try
            {
                shortcutPanelLogic.createAndRegisterShortcuts();
            }
            catch (Exception e)
            {
                // ignore exception
            }
        }
    }
}
