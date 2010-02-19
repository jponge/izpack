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

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Functions to support automated usage of the PacksPanel
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public class PacksPanelAutomationHelper implements PanelAutomation
{
    /**
     * Asks to make the XML panel data.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML tree to write the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        // We add each pack to the panelRoot element
        for (int i = 0; i < idata.availablePacks.size(); i++)
        {
            Pack pack = idata.availablePacks.get(i);
            IXMLElement el = new XMLElementImpl("pack", panelRoot);
            el.setAttribute("index", Integer.toString(i));
            el.setAttribute("name", pack.name);
            Boolean selected = Boolean.valueOf(idata.selectedPacks.contains(pack));
            el.setAttribute("selected", selected.toString());

            panelRoot.addChild(el);
        }
    }

    /**
     * Asks to run in the automated mode.
     *
     * @param idata     The installation data.
     * @param panelRoot The root of the panel data.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        final class PInfo
        {

            private boolean _selected;

            private int _index;

            private String _name = "";

            PInfo(boolean selected, String index, String name)
            {
                _selected = selected;
                try
                {
                    _index = Integer.valueOf(index).intValue();
                }
                catch (NumberFormatException e)
                {
                    _index = -100;
                }
                if (name != null)
                {
                    _name = name;
                }
            }

            public boolean isSelected()
            {
                return _selected;
            }

            public boolean equals(int index)
            {
                return _index == index && _name.equals("");
            }

            public boolean equals(String name)
            {
                return _name.equals(name);
            }

            @Override
            public String toString()
            {
                String retVal = "";
                if (!_name.equals(""))
                {
                    retVal = "Name: " + _name + " and ";
                }
                retVal += "Index: " + String.valueOf(_index);
                return retVal;
            }
        }

        List<PInfo> autoinstallPackInfoList = new ArrayList<PInfo>();

        // We get the packs markups
        Vector<IXMLElement> packList = panelRoot.getChildrenNamed("pack");

        // Read all packs from the xml and remember them to merge it with the selected packs from
        // install data
        System.out.println("Read pack list from xml definition.");
        int numberOfPacks = packList.size();
        for (int packIndex = 0; packIndex < numberOfPacks; packIndex++)
        {
            IXMLElement pack = packList.get(packIndex);
            String index = pack.getAttribute("index");
            String name = pack.getAttribute("name");
            final String selectedString = pack.getAttribute("selected");
            boolean selected = selectedString.equalsIgnoreCase("true")
                    || selectedString.equalsIgnoreCase("on");
            final PInfo packInfo = new PInfo(selected, index, name);
            autoinstallPackInfoList.add(packInfo);
            System.out.println("Try to " + (selected ? "add to" : "remove from") + " selection ["
                    + packInfo.toString() + "]");
        }

        // Now merge the selected pack from automated install data with the selected packs form
        // autoinstall.xml
        System.out.println("Modify pack selection.");
        for (Pack pack : idata.availablePacks)
        {
            // Check if the pack is in the List of autoinstall.xml (search by name and index)
            final int indexOfAvailablePack = idata.availablePacks.indexOf(pack);
            for (PInfo packInfo : autoinstallPackInfoList)
            {
                // Check if we have a pack available that is referenced in autoinstall.xml
                if ((packInfo.equals(pack.name)) || (packInfo.equals(indexOfAvailablePack)))
                {
                    if (pack.required)
                    {
                        // Do not modify required packs
                        if (!packInfo.isSelected())
                        {
                            System.out.println("Pack [" + packInfo.toString()
                                    + "] must be installed because it is required!");
                        }
                    }
                    else
                    {
                        if (packInfo.isSelected())
                        {
                            // Check if the conditions allow to select the pack
                            if ((idata.selectedPacks.indexOf(pack) < 0)
                                    && (pack.id != null)
                                    && (idata.getRules().canInstallPack(pack.id,
                                            idata.getVariables())))
                            {
                                idata.selectedPacks.add(pack);
                                System.out.println("Pack [" + packInfo.toString()
                                        + "] added to selection.");
                            }
                        }
                        else
                        {
                            // Pack can be removed from selection because it is not required
                            idata.selectedPacks.remove(pack);
                            System.out.println("Pack [" + packInfo.toString()
                                    + "] removed from selection.");

                        }
                    }
                    break;
                }
            }
        }
        // Update panelRoot to reflect the changes made by the automation helper, panel validate or panel action
        for (int counter = panelRoot.getChildrenCount(); counter > 0; counter--)
        {
            panelRoot.removeChild(panelRoot.getChildAtIndex(0));
        }
        makeXMLData(idata, panelRoot);
    }
}
