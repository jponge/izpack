/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Julien Ponge
 *
 *  File :               InstallPanel.java
 *  Description :        A panel to launch the installation process.
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

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.n3.nanoxml.*;

/**
 *  The install panel class. Launches the actual installation job.
 *
 * @author     Julien Ponge
 * @created    November 1, 2002
 */
public class InstallPanel extends IzPanel implements ActionListener, InstallListener
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The tip label. */
  private JLabel tipLabel;

  /**  The operation label . */
  private JLabel opLabel;

  /**  The progress bar. */
  private JProgressBar progressBar;

  /**  True if the installation has been done. */
  private volatile boolean validated = false;


  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public InstallPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    setLayout(layout);

    tipLabel = new JLabel(parent.langpack.getString("InstallPanel.tip"),
      parent.icons.getImageIcon("tip"), JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.NORTHWEST;
    layout.addLayoutComponent(tipLabel, gbConstraints);
    add(tipLabel);

    opLabel = new JLabel(" ", JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(opLabel, gbConstraints);
    add(opLabel);

    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setString(parent.langpack.getString("InstallPanel.begin"));
    progressBar.setValue(0);
    parent.buildConstraints(gbConstraints, 0, 3, 2, 1, 1.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.NORTH;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    layout.addLayoutComponent(progressBar, gbConstraints);
    add(progressBar);
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
   *  Actions-handling method (here it launches the installation).
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
    parent.install(this);
  }


  /**  The unpacker starts.  */
  public void startUnpack()
  {
    parent.blockGUI();
  }


  /**
   *  An error was encountered.
   *
   * @param  error  The error text.
   */
  public void errorUnpack(String error)
  {
    opLabel.setText(error);
    idata.installSuccess = false;
    JOptionPane.showMessageDialog(this, error.toString(),
      parent.langpack.getString("installer.error"),
      JOptionPane.ERROR_MESSAGE);
  }


  /**  The unpacker stops.  */
  public void stopUnpack()
  {
    parent.releaseGUI();
    parent.lockPrevButton();
    progressBar.setString(parent.langpack.getString("InstallPanel.finished"));
    progressBar.setEnabled(false);
    opLabel.setText(" ");
    opLabel.setEnabled(false);
    idata.installSuccess = true;
    idata.canClose = true;
    validated = true;
    if (idata.panels.indexOf(this) != (idata.panels.size() - 1))
      parent.unlockNextButton();
  }


  /**
   *  Normal progress indicator.
   *
   * @param  val  The progression value.
   * @param  msg  The progression message.
   */
  public void progressUnpack(int val, String msg)
  {
    progressBar.setValue(val + 1);
    opLabel.setText(msg);
  }


  /**
   *  Pack changing.
   *
   * @param  min       The new mnimum progress.
   * @param  max       The new maximum progress.
   * @param  packName  The pack name.
   */
  public void changeUnpack(int min, int max, String packName)
  {
    progressBar.setValue(0);
    progressBar.setMinimum(min);
    progressBar.setMaximum(max);
    progressBar.setString(packName);
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
    
    parent.install(this);

  }


  /**
   *  Asks to run in the automated mode.
   *
   * @param  panelRoot  The panel XML tree root.
   */
  public void runAutomated(XMLElement panelRoot)
  {
    parent.install(this);
    while (!validated)
      Thread.yield();
  }
}

