/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
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

import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;

/**
 * Functions to support automated usage of the ImgPacksPanel
 * 
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public class ImgPacksPanelAutomationHelper implements PanelAutomation
{

    /**
     * Asks to make the XML panel data.
     * 
     * @param idata The installation data.
     * @param panelRoot The XML root to write the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, XMLElement panelRoot)
    {
        // Selected packs markup
        XMLElement sel = new XMLElement("selected");

        // We add each selected pack to sel
        int size = idata.selectedPacks.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement el = new XMLElement("pack");
            Pack pack = (Pack) idata.selectedPacks.get(i);
            Integer integer = new Integer(idata.availablePacks.indexOf(pack));
            el.setAttribute("index", integer.toString());
            sel.addChild(el);
        }

        // Joining
        panelRoot.addChild(sel);
    }

    /**
     * Asks to run in the automated mode.
     * 
     * @param idata The installation data.
     * @param panelRoot The root of the panel data.
     */
    public void runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
    {
        // We get the selected markup
        XMLElement sel = panelRoot.getFirstChildNamed("selected");

        // We get the packs markups
        Vector pm = sel.getChildrenNamed("pack");

        // We select each of them
        int size = pm.size();
        idata.selectedPacks.clear();
        for (int i = 0; i < size; i++)
        {
            XMLElement el = (XMLElement) pm.get(i);
            Integer integer = new Integer(el.getAttribute("index"));
            int index = integer.intValue();
            idata.selectedPacks.add(idata.availablePacks.get(index));
        }
    }
}
