/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               TargetPanelAutomationHelper.java
 *  Description :        Automation support functions for TargetPanel.
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

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;

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
     * @param idata
     *            The installation data.
     * @param panelRoot
     *            The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, XMLElement panelRoot)
    {
        // Installation path markup
        XMLElement ipath = new XMLElement("installpath");
        // check this writes even if value is the default,
        // because without the constructor, default does not get set.
        ipath.setContent(idata.getInstallPath());

        // Checkings to fix bug #1864
        XMLElement prev = panelRoot.getFirstChildNamed("installpath");
        if (prev != null) panelRoot.removeChild(prev);

        panelRoot.addChild(ipath);
    }

    /**
     * Asks to run in the automated mode.
     * 
     * @param idata
     *            The installation data.
     * @param panelRoot
     *            The XML tree to read the data from.
     */
    public void runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
    {
        // We set the installation path
        XMLElement ipath = panelRoot.getFirstChildNamed("installpath");
        idata.setInstallPath(ipath.getContent());
    }
}
