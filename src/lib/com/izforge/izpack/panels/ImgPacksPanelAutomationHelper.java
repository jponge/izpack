/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               ImgPacksPanelAutomationHelper.java
 *  Description :        Automation support functions for ImgPacksPanel.
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
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.AutomatedInstallData;

import java.util.Vector;

/**
 * Functions to support automated usage of the ImgPacksPanel
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public class ImgPacksPanelAutomationHelper implements PanelAutomation
{
	/**
	 *  Asks to make the XML panel data.
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
	 *  Asks to run in the automated mode.
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
