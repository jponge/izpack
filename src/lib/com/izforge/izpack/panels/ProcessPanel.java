/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge, Tino Schwarze
 *
 *  File :               ProcessPanel.java
 *  Description :        A panel to execute processes during installation.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ProcessPanelWorker;
import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 *  The process panel class.
 *
 * This class allows external processes to be executed during installation.
 *
 * Parts of the code have been taken from CompilePanel.java and
 * modified a lot. 
 * 
 * @author     Tino Schwarze
 * @author     Julien Ponge
 * @created    May 2003
 */
public class ProcessPanel extends IzPanel implements AbstractUIProcessHandler
{
  /**  The operation label . */
  protected JLabel processLabel;

  /**  The overall progress bar. */
  protected JProgressBar overallProgressBar;

  /**  True if the compilation has been done. */
  private boolean validated = false;

  /**  The processing worker. Does all the work. */
  private ProcessPanelWorker worker;

  /**  Number of jobs to process. Used for progress indication. */
  private int noOfJobs;

  private int currentJob;
  
  /**  Where the output is displayed */
  private JTextArea outputPane;
  
  private JScrollPane outputScrollPane;
  
  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public ProcessPanel(InstallerFrame parent, InstallData idata) 
    throws IOException
  {
    super(parent, idata);

    this.worker = new ProcessPanelWorker (idata, this);

    JLabel heading = new JLabel();
    Font font = heading.getFont ();
    font = font.deriveFont (Font.BOLD, font.getSize()*2.0f);
    heading.setFont(font);
    heading.setHorizontalAlignment(SwingConstants.CENTER);
    heading.setText(parent.langpack.getString ("ProcessPanel.heading"));
    heading.setVerticalAlignment(SwingConstants.TOP);    
    setLayout (new BorderLayout ());
    add (heading, BorderLayout.NORTH);
    
    // put everything but the heading into it's own panel
    // (to center it vertically)
    JPanel subpanel = new JPanel ();
    
    subpanel.setAlignmentX(0.5f);
    subpanel.setLayout(new BoxLayout (subpanel, BoxLayout.Y_AXIS));
    
    this.processLabel = new JLabel ();
    this.processLabel.setAlignmentX(0.5f);
    this.processLabel.setText (" ");
    subpanel.add (this.processLabel);
    
    this.overallProgressBar = new JProgressBar ();
    this.overallProgressBar.setAlignmentX(0.5f);
    this.overallProgressBar.setStringPainted(true);
    subpanel.add (this.overallProgressBar);
  
    this.outputPane = new JTextArea ();    
    this.outputPane.setEditable(false);
    this.outputScrollPane = new JScrollPane (this.outputPane); 
    subpanel.add (this.outputScrollPane);
    
    add (subpanel, BorderLayout.CENTER);    
  }


  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    The validation state.
   */
  public boolean isValidated()
  {
    return validated;
  }

  /**  The compiler starts.  */
  public void startProcessing (int no_of_jobs)
  {
    this.noOfJobs = no_of_jobs;
    overallProgressBar.setMaximum (noOfJobs);
    parent.lockPrevButton();
  }


  /**  The compiler stops.  */
  public void finishProcessing ()
  {
    overallProgressBar.setValue (this.noOfJobs);
    String no_of_jobs = Integer.toString (this.noOfJobs);
    overallProgressBar.setString (no_of_jobs + " / " + no_of_jobs);
    overallProgressBar.setEnabled (false);

    processLabel.setText(" ");
    processLabel.setEnabled(false);

    validated = true;
    idata.installSuccess = true;
    if (idata.panels.indexOf(this) != (idata.panels.size() - 1))
      parent.unlockNextButton();
  }

  /**
   * Log a message.
   * 
   * @param message The message.
   * @param stderr Whether the message came from stderr or stdout.
   */
  public void logOutput (String message, boolean stderr)
  {
    // TODO: make it colored
    this.outputPane.append(message+'\n');
  }

  /**
   *  Next job starts.
   *
   * @param  jobName   The job name.
   */
  public void startProcess (String jobName)
  {
    processLabel.setText (jobName);
    
    this.currentJob++;
    overallProgressBar.setValue(this.currentJob);
    overallProgressBar.setString (Integer.toString (this.currentJob) + " / " + Integer.toString (this.noOfJobs));
  }


  public void finishProcess ()
  {
  }
  
  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    // We clip the panel
    Dimension dim = parent.getPanelsContainerSize();
    dim.width = dim.width - (dim.width / 4);
    dim.height = 150;
    setMinimumSize(dim);
    setMaximumSize(dim);
    setPreferredSize(dim);
    
    parent.lockNextButton();
    
    this.worker.startThread();
  }

  /** Create XML data for automated installation. */
  public void makeXMLData (XMLElement panelRoot)
  {
    // does nothing (no state to save)
  }

}

