/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               InstallPanelAutomationHelper.java
 *  Description :        Automation support functions for InstallPanel.
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
package com.izforge.izpack.panels;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;
import com.izforge.izpack.installer.Unpacker;
import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * Functions to support automated usage of the InstallPanel
 *
 * @author Jonathan Halliday
 */
public class InstallPanelAutomationHelper extends PanelAutomationHelper 
                                            implements PanelAutomation, AbstractUIProgressHandler
{
	// state var for thread sync.
	private boolean done = false;

  private int noOfPacks = 0;
  
	/**
	 * Null op - this panel type has no state to serialize.
	 *
	 * @param installData unused.
	 * @param panelRoot unused.
	 */
	public void makeXMLData(AutomatedInstallData installData, XMLElement panelRoot)
	{
		// do nothing.
	}

	/**
	 *  Perform the installation actions.
	 *
	 * @param panelRoot The panel XML tree root.
	 */
	public void runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
	{
		Unpacker unpacker = new Unpacker(idata, this);
		unpacker.start();
		done = false;
		while (!done)
		{
			Thread.yield();
		}
	}

	/**
	 * Reports progress on System.out
	 *
	 * @see AbstractUIProgressHandler#startAction(String, int)
	 */
	public void startAction (String name, int no_of_steps)
	{
		System.out.println("[ Starting to unpack ]");
    this.noOfPacks = no_of_steps;
	}

	/**
	 * Sets state variable for thread sync.
	 *
	 * @see com.izforge.izpack.util.AbstractUIProgressHandler#stopAction()
	 */
	public void stopAction()
	{
		System.out.println("[ Unpacking finished. ]");
		done = true;
	}

	/**
	 * Null op.
	 *
	 * @param val
	 * @param msg
   * @see com.izforge.izpack.util.AbstractUIProgressHandler#progress(int, String)
	 */
	public void progress(int val, String msg)
	{
		// silent for now. should log individual files here, if we had a verbose mode?
	}

	/**
	 * Reports progress to System.out
	 *
   * @param packName The currently installing pack.
	 * @param stepno The number of the pack
	 * @param max unused
   * @see com.izforge.izpack.util.AbstractUIProgressHandler#nextStep(String, int, int)
	 */
	public void nextStep (String packName, int stepno, int stepsize)
	{
		System.out.print("[ Processing package: " + packName +" (");
    System.out.print (stepno);
    System.out.print ('/');
    System.out.print (this.noOfPacks);
    System.out.println (") ]");
	}

}
