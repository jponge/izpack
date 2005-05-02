/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               PacksPanelAutomationHelper.java
 *  Description :        Automation support functions for PacksPanel.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.panels;

import java.util.Iterator;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;

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
     * @param idata
     *            The installation data.
     * @param panelRoot
     *            The XML tree to write the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, XMLElement panelRoot)
    {
        // We add each pack to the panelRoot element
        for (int i = 0; i < idata.availablePacks.size(); i++)
        {
            Pack pack = (Pack) idata.availablePacks.get(i);
            XMLElement el = new XMLElement("pack");
            el.setAttribute("index", new Integer(i).toString());
            el.setAttribute("name", pack.name);
            Boolean selected = Boolean.valueOf(idata.selectedPacks.contains(pack));
            el.setAttribute("selected", selected.toString());

            panelRoot.addChild(el);
        }
    }

    /**
     * Asks to run in the automated mode.
     * 
     * @param idata
     *            The installation data.
     * @param panelRoot
     *            The root of the panel data.
     */
    public void runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
    {
        // We first get the <selected> child (new from version 3.7.0).
        XMLElement selectedPacks = panelRoot.getFirstChildNamed("selected");
        // We get the packs markups
        Vector pm = selectedPacks.getChildrenNamed("pack");

        // We figure out the selected ones
        int size = pm.size();
        idata.selectedPacks.clear();
        for (int i = 0; i < size; i++)
        {
            XMLElement el = (XMLElement) pm.get(i);
            Boolean selected = new Boolean(true); // No longer needed.

            if (selected.booleanValue())
            {
                String index_str = el.getAttribute("index");

                // be liberal in what we accept
                // (For example, this allows auto-installer files to be fitted
                // to automatically
                // generated installers, yes I need this! tisc.)
                if (index_str != null)
                {
                    try
                    {
                        int index = Integer.parseInt(index_str);
                        if ((index >= 0) && (index < idata.availablePacks.size()))
                        {
                            idata.selectedPacks.add(idata.availablePacks.get(index));
                        }
                        else
                        {
                            System.err.println("Invalid pack index \"" + index_str + "\" in line "
                                    + el.getLineNr());
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Invalid pack index \"" + index_str + "\" in line "
                                + el.getLineNr());
                    }
                }
                else
                {
                    String name = el.getAttribute("name");

                    if (name != null)
                    {
                        // search for pack with that name
                        Iterator pack_it = idata.availablePacks.iterator();

                        boolean found = false;

                        while ((!found) && pack_it.hasNext())
                        {
                            Pack pack = (Pack) pack_it.next();

                            if (pack.name.equals(name))
                            {
                                idata.selectedPacks.add(pack);
                                found = true;
                            }

                        }

                        if (!found)
                        {
                            System.err.println("Could not find selected pack named \"" + name
                                    + "\" in line " + el.getLineNr());
                        }

                    }

                }

            }

        }

    }

}
