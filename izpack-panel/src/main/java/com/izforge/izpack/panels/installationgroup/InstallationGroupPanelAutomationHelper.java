/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 JBoss Inc
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

package com.izforge.izpack.panels.installationgroup;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.panels.installationgroup.InstallationGroupPanel.GroupData;
import com.izforge.izpack.util.Debug;

import java.util.HashMap;
import java.util.Vector;

/**
 * An automation helper for the InstallationGroupPanel
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class InstallationGroupPanelAutomationHelper
        implements PanelAutomation
{
    /**
     *
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        GroupData[] rows = (GroupData[]) idata.getAttribute("GroupData");
        HashMap packsByName = (HashMap) idata.getAttribute("packsByName");
        // Write out the group to pack mappings
        for (GroupData gd : rows)
        {
            IXMLElement xgroup = new XMLElementImpl("group", panelRoot);
            xgroup.setAttribute("name", gd.name);
            for (String name : gd.packNames)
            {
                Pack pack = (Pack) packsByName.get(name);
                int index = idata.getAvailablePacks().indexOf(pack);
                IXMLElement xpack = new XMLElementImpl("pack", xgroup);
                xpack.setAttribute("name", name);
                xpack.setAttribute("index", "" + index);
                xgroup.addChild(xpack);
            }
            panelRoot.addChild(xgroup);
        }
    }

    /**
     * TODO Need to add a InstallationGroupPanelAutomationHelper to read the
     * xml installDataGUI to allow an install group to specify the selected packs.
     */
    public void runAutomated(AutomatedInstallData idata,
                             IXMLElement panelRoot)
    {
        String installGroup = idata.getVariable("INSTALL_GROUP");
        Debug.trace("InstallationGroupPanelAutomationHelper: runAutomated, INSTALL_GROUP: " + installGroup);
        if (installGroup != null)
        {
            Vector<IXMLElement> groups = panelRoot.getChildrenNamed("group");
            for (IXMLElement group : groups)
            {
                String name = group.getAttribute("name");
                Debug.trace("InstallationGroupPanelAutomationHelper: Checking INSTALL_GROUP against: " + name);
                if (name.equalsIgnoreCase(installGroup))
                {
                    Debug.trace("Found INSTALL_GROUP match for: " + installGroup);
                    idata.getSelectedPacks().clear();
                    Vector<IXMLElement> packs = group.getChildrenNamed("pack");
                    Debug.trace(name + " pack count: " + packs.size());
                    Debug.trace("Available pack count: " + idata.getAvailablePacks().size());
                    for (IXMLElement xpack : packs)
                    {
                        String pname = xpack.getAttribute("name");
                        String indexStr = xpack.getAttribute("index");
                        int index = Integer.parseInt(indexStr);
                        if (index >= 0)
                        {
                            Pack pack = idata.getAvailablePacks().get(index);
                            idata.getSelectedPacks().add(pack);
                            Debug.trace("Added pack: " + pack.name);
                        }
                    }
                    Debug.trace("Set selectedPacks to: " + idata.getSelectedPacks());
                    break;
                }
            }
        }
    }

}
