/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge, Tino Schwarze
 *
 *  File :               CompilePanel.java
 *  Description :        A panel to compile files after installation
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

import com.izforge.izpack.installer.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.n3.nanoxml.*;

/**
 *  The compile panel class.
 *
 * This class allows .java files to be compiled after installation.
 *
 * Parts of the code have been taken from InstallPanel.java and
 * modified a lot.
 * 
 * @author     Tino Schwarze
 * @author     Julien Ponge
 * @created    May 2003
 */
public class CompilePanel extends IzPanel implements ActionListener, CompileHandler
{
  /**  The combobox for compiler selection. */
  protected JComboBox compilerComboBox;

  /**  The combobox for compiler argument selection. */
  protected JComboBox argumentsComboBox;

  /**  The start button. */
  protected JButton startButton;

  /**  The browse button. */
  protected JButton browseButton;

  /**  The tip label. */
  protected JLabel tipLabel;

  /**  The operation label . */
  protected JLabel opLabel;

  /**  The pack progress bar. */
  protected JProgressBar packProgressBar;

  /**  The operation label . */
  protected JLabel overallLabel;

  /**  The overall progress bar. */
  protected JProgressBar overallProgressBar;

  /**  True if the compilation has been done. */
  private boolean validated = false;

  /**  The compilation worker. Does all the work. */
  private CompileWorker worker;

  /**  Number of jobs to compile. Used for progress indication. */
  private int noOfJobs;

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public CompilePanel(InstallerFrame parent, InstallData idata) 
    throws IOException
  {
    super(parent, idata);

    this.worker = new CompileWorker (idata, this);

    GridBagConstraints gridBagConstraints;

    JLabel heading = new JLabel();
    // put everything but the heading into it's own panel
    // (to center it vertically)
    JPanel subpanel = new JPanel ();
    JLabel compilerLabel = new JLabel();
    compilerComboBox = new JComboBox();
    this.browseButton = ButtonFactory.createButton (parent.langpack.getString ("CompilePanel.browse"), idata.buttonsHColor);
    JLabel argumentsLabel = new JLabel();
    this.argumentsComboBox = new JComboBox();
    this.startButton = ButtonFactory.createButton (parent.langpack.getString ("CompilePanel.start"), idata.buttonsHColor);
    this.tipLabel = new JLabel(parent.langpack.getString ("CompilePanel.tip"),
        parent.icons.getImageIcon ("tip"), JLabel.TRAILING);
    this.opLabel = new JLabel();
    packProgressBar = new JProgressBar();
    this.overallLabel = new JLabel();
    this.overallProgressBar = new JProgressBar();

    setLayout(new GridBagLayout());

    Font font = heading.getFont ();
    font = font.deriveFont (Font.BOLD, font.getSize()*2.0f);
    heading.setFont(font);
    heading.setHorizontalAlignment(SwingConstants.CENTER);
    heading.setText(parent.langpack.getString ("CompilePanel.heading"));
    heading.setVerticalAlignment(SwingConstants.TOP);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.1;
    add(heading, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.9;
    add (subpanel, gridBagConstraints);

    subpanel.setLayout(new GridBagLayout());

    int row = 0;

    compilerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    compilerLabel.setLabelFor(compilerComboBox);
    compilerLabel.setText(parent.langpack.getString ("CompilePanel.choose_compiler"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    //gridBagConstraints.weighty = 0.1;
    subpanel.add(compilerLabel, gridBagConstraints);

    compilerComboBox.setEditable(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    //gridBagConstraints.weighty = 0.1;

    Iterator it = this.worker.getAvailableCompilers().iterator();

    while (it.hasNext())
      compilerComboBox.addItem ((String)it.next());
    
    subpanel.add(compilerComboBox, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.gridx = 1;
    gridBagConstraints.anchor = GridBagConstraints.EAST;
    browseButton.addActionListener (this);
    subpanel.add(browseButton, gridBagConstraints);

    argumentsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    argumentsLabel.setLabelFor(argumentsComboBox);
    argumentsLabel.setText(parent.langpack.getString ("CompilePanel.additional_arguments"));
    //argumentsLabel.setToolTipText("");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    //gridBagConstraints.weighty = 0.1;
    subpanel.add(argumentsLabel, gridBagConstraints);

    argumentsComboBox.setEditable(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    //gridBagConstraints.weighty = 0.1;

    it = this.worker.getAvailableArguments ().iterator();

    while (it.hasNext())
      argumentsComboBox.addItem ((String)it.next());
    
    subpanel.add(argumentsComboBox, gridBagConstraints);

    // leave some space above the label
    gridBagConstraints.insets = new Insets (10, 0, 0, 0);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.NONE;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    subpanel.add(tipLabel, gridBagConstraints);

    opLabel.setText(" ");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    subpanel.add(opLabel, gridBagConstraints);

    packProgressBar.setValue(0);
    packProgressBar.setString(parent.langpack.getString ("CompilePanel.progress.initial"));
    packProgressBar.setStringPainted(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.SOUTH;
    subpanel.add(packProgressBar, gridBagConstraints);

    overallLabel.setText (parent.langpack.getString ("CompilePanel.progress.overall"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    subpanel.add(overallLabel, gridBagConstraints);

    overallProgressBar.setValue(0);
    overallProgressBar.setString("");
    overallProgressBar.setStringPainted(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = row++;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.SOUTH;
    subpanel.add(overallProgressBar, gridBagConstraints);

    startButton.setText(parent.langpack.getString ("CompilePanel.start"));
    startButton.addActionListener (this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridy = row++;
    gridBagConstraints.fill = GridBagConstraints.NONE;
    // leave some space above the button
    gridBagConstraints.insets = new Insets (5, 0, 0, 0);
    subpanel.add(startButton, gridBagConstraints);
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

  /**
   *  Action function, called when the start button is pressed.
   */
  public void actionPerformed (ActionEvent e)
  {
    if (e.getSource() == this.startButton)
    {
      this.worker.setCompiler ((String)this.compilerComboBox.getSelectedItem ());

      this.worker.setCompilerArguments ((String)this.argumentsComboBox.getSelectedItem ());

      this.blockGUI ();
      this.worker.startThread ();
    }
    else if (e.getSource () == this.browseButton)
    {
      this.parent.blockGUI ();
      JFileChooser chooser = new JFileChooser ();
      chooser.setCurrentDirectory (new File ((String)this.compilerComboBox.getSelectedItem()).getParentFile ());
      int result = chooser.showDialog (this.parent, this.parent.langpack.getString ("CompilePanel.browse.approve"));
      if (result == JFileChooser.APPROVE_OPTION)
      {
        File file_chosen = chooser.getSelectedFile();

        if (file_chosen.isFile ())
        {
          this.compilerComboBox.setSelectedItem (file_chosen.getAbsolutePath());
        }

      }

      this.parent.releaseGUI();
    }

  }

  /**
   * Block the GUI - disalow input.
   */
  protected void blockGUI ()
  {
    // disable all controls
    this.startButton.setEnabled (false);
    this.browseButton.setEnabled (false);
    this.compilerComboBox.setEnabled (false);
    this.argumentsComboBox.setEnabled (false);

    this.parent.blockGUI();
  }

  /**
   * Release the GUI - allow input.
   *
   * @param allowconfig allow the user to enter new configuration
   */
  protected void releaseGUI (boolean allowconfig)
  {
    // disable all controls
    if (allowconfig)
    {
      this.startButton.setEnabled (true);
      this.browseButton.setEnabled (true);
      this.compilerComboBox.setEnabled (true);
      this.argumentsComboBox.setEnabled (true);
    }

    this.parent.releaseGUI();
  }

  /**
   *  An error was encountered.
   *
   * @param  error  The error information.
   * @see com.izforge.izpack.installer.CompileHandler
   */
  public void handleCompileError (CompileResult error)
  {
    String message = error.getMessage ();
    opLabel.setText(message);
    CompilerErrorDialog dialog = new CompilerErrorDialog (parent, message, idata.buttonsHColor);
    dialog.show (error);

    if (dialog.getResult() == CompilerErrorDialog.RESULT_IGNORE)
    {
      error.setAction (CompileResult.ACTION_CONTINUE);
    }
    else if (dialog.getResult() == CompilerErrorDialog.RESULT_RECONFIGURE)
    {
      error.setAction (CompileResult.ACTION_RECONFIGURE);
    }
    else // default case: abort
    {
      error.setAction (CompileResult.ACTION_ABORT);
    }

  }


  /**  The compiler starts.  */
  public void startAction (String name, int noOfJobs)
  {
    this.noOfJobs = noOfJobs;
    overallProgressBar.setMaximum (noOfJobs);
    parent.lockPrevButton();
  }


  /**  The compiler stops.  */
  public void stopAction ()
  {
    CompileResult result = this.worker.getResult ();

    this.releaseGUI(result.isReconfigure());

    if (result.isContinue())
    {
      parent.lockPrevButton();

      packProgressBar.setString(parent.langpack.getString("CompilePanel.progress.finished"));
      packProgressBar.setEnabled(false);
      packProgressBar.setValue (packProgressBar.getMaximum());

      overallProgressBar.setValue (this.noOfJobs);
      String no_of_jobs = Integer.toString (this.noOfJobs);
      overallProgressBar.setString (no_of_jobs + " / " + no_of_jobs);
      overallProgressBar.setEnabled (false);

      opLabel.setText(" ");
      opLabel.setEnabled(false);

      validated = true;
      idata.installSuccess = true;
      if (idata.panels.indexOf(this) != (idata.panels.size() - 1))
        parent.unlockNextButton();
    }
    else
    {
      idata.installSuccess = false;
    }

  }


  /**
   *  Normal progress indicator.
   *
   * @param  val  The progression value.
   * @param  msg  The progression message.
   */
  public void progress (int val, String msg)
  {
    //Debug.trace ("progress: " + val + " " + msg);
    packProgressBar.setValue(val + 1);
    opLabel.setText(msg);
  }


  /**
   *  Job changing.
   *
   * @param  min       The new mnimum progress.
   * @param  max       The new maximum progress.
   * @param  jobName   The job name.
   * @param  jobNo     The job number.
   */
  public void nextStep (String jobName, int max, int jobNo)
  {
    packProgressBar.setValue(0);
    packProgressBar.setMaximum(max);
    packProgressBar.setString(jobName);

    opLabel.setText ("");

    overallProgressBar.setValue (jobNo);
    overallProgressBar.setString (Integer.toString (jobNo) + " / " + Integer.toString (this.noOfJobs));
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
  }

  /** Create XML data for automated installation. */
  public void makeXMLData (XMLElement panelRoot)
  {
    // just save the compiler chosen and the arguments
    XMLElement compiler = new XMLElement ("compiler");
    compiler.setContent (this.worker.getCompiler());
    panelRoot.addChild (compiler);

    XMLElement args = new XMLElement ("arguments");
    args.setContent (this.worker.getCompilerArguments());
    panelRoot.addChild (args);
  }

  /**
   * Show a special dialog for compiler errors.
   *
   * This dialog is neccessary because we have lots of information if
   * compilation failed. We'd also like the user to chose whether
   * to ignore the error or not.
   */
  protected class CompilerErrorDialog extends JDialog implements ActionListener 
  {
    /** user closed the dialog without pressing "Ignore" or "Abort" */
    public static final int RESULT_NONE = 0;
    /** user pressed "Ignore" button */
    public static final int RESULT_IGNORE = 23;
    /** user pressed "Abort" button */
    public static final int RESULT_ABORT = 42;
    /** user pressed "Reconfigure" button */
    public static final int RESULT_RECONFIGURE = 47;

    /** visual goodie: button hightlight color */
    private java.awt.Color buttonHColor = null;
    
    /** Creates new form compilerErrorDialog */
    public CompilerErrorDialog(java.awt.Frame parent, String title, java.awt.Color buttonHColor)
    {
      super(parent, title, true);
      this.buttonHColor = buttonHColor;
      initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     *
     * Generated with help from NetBeans IDE.
     */
    private void initComponents() 
    {
      JPanel errorMessagePane = new JPanel();
      errorMessageText = new JTextArea();
      JTextArea seeBelowText = new JTextArea ();
      JTabbedPane errorDisplayPane = new JTabbedPane();
      JScrollPane commandScrollPane = new JScrollPane();
      commandText = new JTextArea();
      JScrollPane stdOutScrollPane = new JScrollPane();
      stdOutText = new JTextArea();
      JScrollPane stdErrScrollPane = new JScrollPane();
      stdErrText = new JTextArea();
      JPanel buttonsPanel = new JPanel();
      reconfigButton = ButtonFactory.createButton (parent.langpack.getString ("CompilePanel.error.reconfigure"), this.buttonHColor);
      ignoreButton = ButtonFactory.createButton (parent.langpack.getString ("CompilePanel.error.ignore"), this.buttonHColor);
      abortButton = ButtonFactory.createButton (parent.langpack.getString ("CompilePanel.error.abort"), this.buttonHColor);

      addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent evt) {
            closeDialog(evt);
          }
        });

      errorMessagePane.setLayout (new BoxLayout (errorMessagePane, BoxLayout.Y_AXIS));
      errorMessageText.setBackground(super.getBackground());
      errorMessageText.setEditable(false);
      errorMessageText.setLineWrap(true);
      //errorMessageText.setText("The compiler does not seem to work. See below for the command we tried to execute and the results.");
      //errorMessageText.setToolTipText("null");
      errorMessageText.setWrapStyleWord(true);
      errorMessagePane.add(errorMessageText);

      seeBelowText.setBackground(super.getBackground());
      seeBelowText.setEditable(false);
      seeBelowText.setLineWrap(true);
      seeBelowText.setWrapStyleWord(true);
      seeBelowText.setText (parent.langpack.getString ("CompilePanel.error.seebelow"));
      errorMessagePane.add (seeBelowText);

      getContentPane().add(errorMessagePane, java.awt.BorderLayout.NORTH);

      // use 12pt monospace font for compiler output etc.
      Font output_font = new Font ("Monospaced", Font.PLAIN, 12);

      //errorDisplayPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      //errorDisplayPane.setName("null");
      commandText.setFont (output_font);
      commandText.setEditable(false);
      commandText.setRows(10);
      commandText.setColumns(82);
      commandText.setWrapStyleWord(true);
      commandText.setLineWrap(true);
      //commandText.setText("akjfkajfeafjakefjakfkaejfja");
      commandScrollPane.setViewportView(commandText);

      errorDisplayPane.addTab("Command", commandScrollPane);

      stdOutText.setFont (output_font);
      stdOutText.setEditable(false);
      stdOutText.setWrapStyleWord(true);
      stdOutText.setLineWrap(true);
      stdOutScrollPane.setViewportView(stdOutText);

      errorDisplayPane.addTab("Standard Output", null, stdOutScrollPane);

      stdErrText.setFont (output_font);
      stdErrText.setEditable(false);
      stdErrText.setWrapStyleWord(true);
      stdErrText.setLineWrap(true);
      stdErrScrollPane.setViewportView(stdErrText);

      errorDisplayPane.addTab("Standard Error", null, stdErrScrollPane);

      getContentPane().add(errorDisplayPane, java.awt.BorderLayout.CENTER);

      buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

      reconfigButton.addActionListener (this);
      buttonsPanel.add(reconfigButton);

      ignoreButton.addActionListener (this);
      buttonsPanel.add(ignoreButton);

      abortButton.addActionListener (this);
      buttonsPanel.add(abortButton);

      getContentPane().add(buttonsPanel, java.awt.BorderLayout.SOUTH);

      pack();
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) 
    {
      setVisible(false);
      dispose();
    }


    public void show (CompileResult error)
    {
      this.errorMessageText.setText (error.getMessage ());
      this.commandText.setText (error.getCmdline ());
      this.stdOutText.setText (error.getStdout ());
      this.stdErrText.setText (error.getStderr ());
      super.show();
    }

    public int getResult ()
    {
      return this.result;
    }


    public void actionPerformed (ActionEvent e)
    {
      boolean closenow = false;

      if (e.getSource () == this.ignoreButton)
      {
        this.result = RESULT_IGNORE;
        closenow = true;
      }
      else if (e.getSource () == this.abortButton)
      {
        this.result = RESULT_ABORT;
        closenow = true;
      }
      else if (e.getSource () == this.reconfigButton)
      {
        this.result = RESULT_RECONFIGURE;
        closenow = true;
      }

      if (closenow)
      {
        this.setVisible (false);
        this.dispose ();
      }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextArea commandText;
    //private JScrollPane stdOutScrollPane;
    private JTextArea stdErrText;
    //private JPanel buttonsPanel;
    //private JScrollPane commandScrollPane;
    private JTextArea errorMessageText;
    //private JScrollPane stdErrScrollPane;
    private JButton ignoreButton;
    private JTextArea stdOutText;
    private JButton abortButton;
    private JButton reconfigButton;
    //private JTabbedPane errorDisplayPane;
    // End of variables declaration//GEN-END:variables

    private int result = RESULT_NONE;
  }
}

