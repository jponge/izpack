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

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.installer.*;

import java.io.IOException;

/**
 * Functions to support automated usage of the CompilePanel
 *
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class CompilePanelAutomationHelper extends PanelAutomationHelper 
                                            implements PanelAutomation, CompileHandler
{
  private CompileWorker worker = null;

  private int job_max = 0;
  private String job_name = null;
  private int last_line_len = 0;

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
    XMLElement compiler_xml = panelRoot.getFirstChildNamed ("compiler");

    String compiler = null;

    if (compiler_xml != null)
      compiler = compiler_xml.getContent ();

    if (compiler == null)
    {
      System.out.println ("invalid automation data: could not find compiler");
      return;
    }

    XMLElement args_xml = panelRoot.getFirstChildNamed ("arguments");

    String args = null;

    if (args_xml != null)
      args = args_xml.getContent ();

    if (args_xml == null)
    {
      System.out.println ("invalid automation data: could not find compiler arguments");
      return;
    }

    try
    {
      this.worker = new CompileWorker (idata, this); 
      this.worker.setCompiler (compiler);
      this.worker.setCompilerArguments (args);

      this.worker.run ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

	}

	/**
	 * Reports progress on System.out
	 *
   * @see com.izforge.izpack.util.AbstractUIProgressHandler#startAction(String, int)
	 */
	public void startAction (String name, int noOfJobs)
	{
    System.out.println ("[ Starting compilation ]");
    this.job_name = "";
	}

	/**
	 * Reports the error to System.err
	 *
	 * @param error the error
	 * @see CompileHandler#handleCompileError(CompileResult)
	 */
	public void handleCompileError (CompileResult error)
	{
    System.out.println ();
    System.out.println ("[ Compilation failed ]");
    System.err.println ("Command line: "+error.getCmdline());
    System.err.println ();
    System.err.println ("stdout of compiler:");
    System.err.println (error.getStdout());
    System.err.println ("stderr of compiler:");
    System.err.println (error.getStderr());
    // do not abort compilation, just continue
    error.setAction (CompileResult.ACTION_CONTINUE);
	}

	/**
	 * Sets state variable for thread sync.
	 *
	 * @see com.izforge.izpack.util.AbstractUIProgressHandler#stopAction()
	 */
	public void stopAction ()
	{
    if ((this.job_name != null) && (this.last_line_len > 0))
    {
      String line = this.job_name + ": done.";
      System.out.print ("\r"+line);
      for (int i = line.length(); i < this.last_line_len; i++)
        System.out.print (' ');
      System.out.println ();
    }

    if (this.worker.getResult().isSuccess())
    {
      System.out.println ("[ Compilation successful ]");
    }

	}

	/**
	 * Tell about progress.
	 *
	 * @param val
	 * @param msg
	 * @see com.izforge.izpack.util.AbstractUIProgressHandler#progress(int, String)
	 */
	public void progress(int val, String msg)
	{
    float percentage = ((float)val)*100.0f/(float)this.job_max;

    String percent = (new Integer ((int)percentage)).toString()+'%';
    String line = this.job_name + ": " + percent;

    int line_len = line.length();

    System.out.print ("\r"+line);
    for (int i = line_len; i < this.last_line_len; i++)
      System.out.print (' ');

    this.last_line_len = line_len;
	}

	/**
	 * Reports progress to System.out
	 *
   * @param jobName The next job's name.
	 * @param max unused
	 * @param jobNo The next job's number.
   * @see com.izforge.izpack.util.AbstractUIProgressHandler#nextStep(String, int, int)
	 */
	public void nextStep (String jobName, int max, int jobNo)
	{
    if ((this.job_name != null) && (this.last_line_len > 0))
    {
      String line = this.job_name + ": done.";
      System.out.print ("\r"+line);
      for (int i = line.length(); i < this.last_line_len; i++)
        System.out.print (' ');
      System.out.println ();
    }

    this.job_max = max;
    this.job_name = jobName;
    this.last_line_len = 0;
	}
}
