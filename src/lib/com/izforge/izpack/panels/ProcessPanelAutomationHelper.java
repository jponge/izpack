/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               CompilePanelAutomationHelper.java
 *  Description :        Automation support functions for CompilePanel.
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

import java.io.IOException;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;
import com.izforge.izpack.installer.ProcessPanelWorker;
import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 * Functions to support automated usage of the CompilePanel
 *
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class ProcessPanelAutomationHelper extends PanelAutomationHelper 
                                            implements PanelAutomation, AbstractUIProcessHandler
{
  private ProcessPanelWorker worker = null;

  private int noOfJobs = 0;
  private int currentJob = 0;

	/**
	 * Save data for running automated.
	 *
	 * @param installData installation parameters
	 * @param panelRoot unused.
	 */
	public void makeXMLData(AutomatedInstallData installData, XMLElement panelRoot)
	{
    // not used here - during automatic installation, no automatic
    // installation information is generated
	}

	/**
	 *  Perform the installation actions.
	 *
	 * @param panelRoot The panel XML tree root.
	 */
	public void runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
	{
    try
    {
      this.worker = new ProcessPanelWorker (idata, this); 

      this.worker.run ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

	}

  public void logOutput (String message, boolean stderr)
  {
    if (stderr)
    {
      System.err.println (message);      
    }
    else
    {
      System.out.println (message);
    }
  }
  
	/**
	 * Reports progress on System.out
	 *
   * @see com.izforge.izpack.util.AbstractUIProcessHandler#startProcessing(int)
	 */
	public void startProcessing (int noOfJobs)
	{
    System.out.println ("[ Starting processing ]");
    this.noOfJobs = noOfJobs;
	}

	/**
	 *
	 * @see com.izforge.izpack.util.AbstractUIProcessHandler#finishProcessing()
	 */
	public void finishProcessing ()
	{
    System.out.println ("[ Processing finished ]");
	}

  /**
   * 
   */
  public void startProcess (String name)
  {
    this.currentJob++;
    System.out.println ("Starting process "+name+
       " ("+Integer.toString (this.currentJob)+"/"+Integer.toString (this.noOfJobs)+")");
  }

  public void finishProcess ()
  {      
  }
}
