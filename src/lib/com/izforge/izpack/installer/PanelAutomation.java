/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               PanelAutomation.java
 *  Description :        Automation (silent install) Interface.
 *  Author's email :     jonathan.halliday@arjuna.com
 *  Author's Website :   http://www.arjuna.com
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
package com.izforge.izpack.installer;

import net.n3.nanoxml.XMLElement;

/**
 *  Defines the Interface that must be implemented for running
 *  Panels in automated (a.k.a. silent, headless) install mode.
 *
 *  Implementing classes MUST NOT link against awt/swing classes.
 *  Thus the Panels cannot implement this interface directly,
 *  they should use e.g. helper classes instead.
 *
 * @see AutomatedInstaller
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public interface PanelAutomation
{
	/**
	 *  Asks the panel to set its own XML data that can be brought back for an
	 *  automated installation process. Use it as a blackbox if your panel needs
	 *  to do something even in automated mode.
	 *
	 * @param  installData The installation data
	 * @param  panelRoot  The XML root element of the panels blackbox tree.
	 */
	public void makeXMLData(AutomatedInstallData installData, XMLElement panelRoot);

	/**
	 *  Makes the panel work in automated mode. Default is to do nothing, but any
	 *  panel doing something 'effective' during the installation process should
	 *  implement this method.
	 *
	 * @param  installData The installation data
	 * @param  panelRoot  The XML root element of the panels blackbox tree.
	 */
	public void runAutomated(AutomatedInstallData installData, XMLElement panelRoot);
}
