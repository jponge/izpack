/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               LicencePanel.java
 *  Description :        A panel to prompt the user for a licence agreement.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (C) 2002 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
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

/**
 *  The license panel.
 *
 * @author     Julien Ponge
 * @created    November 1, 2002
 */
public class LicencePanel extends IzPanel implements ActionListener
{
  /**  The license text. */
  private String licence;

  /**  The text area. */
  private JTextArea textArea;

  /**  The radio buttons. */
  private JRadioButton yesRadio, noRadio;

  /**  The scrolling container. */
  private JScrollPane scroller;


  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public LicencePanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // We load the licence
    loadLicence();

    // We put our components

    JLabel infoLabel = new JLabel(parent.langpack.getString("LicencePanel.info"), parent.icons.getImageIcon("history"), JLabel.TRAILING);
    add(infoLabel);

    add(Box.createRigidArea(new Dimension(0, 3)));

    textArea = new JTextArea(licence);
    textArea.setMargin(new Insets(2,2,2,2));
    textArea.setCaretPosition(0);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    scroller = new JScrollPane(textArea);
    scroller.setAlignmentX(LEFT_ALIGNMENT);
    add(scroller);

    ButtonGroup group = new ButtonGroup();

    yesRadio = new JRadioButton(parent.langpack.getString("LicencePanel.agree"), false);
    group.add(yesRadio);
    add(yesRadio);
    yesRadio.addActionListener(this);

    noRadio = new JRadioButton(parent.langpack.getString("LicencePanel.notagree"), true);
    group.add(noRadio);
    add(noRadio);
    noRadio.addActionListener(this);
  }


  /**  Loads the licence text.  */
  private void loadLicence()
  {
    try
    {
      // We read it
      String resNamePrifix = "LicencePanel.licence";
      licence = super.getResourceManager().getTextResource(resNamePrifix);
    }
    catch (Exception err)
    {
      licence = "Error : could not load the licence text !";
    }
  }


  /**
   *  Actions-handling method (here it allows the installation).
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
    if (yesRadio.isSelected())
      parent.unlockNextButton();
    else
      parent.lockNextButton();
  }


  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    true if the user has agreed.
   */
  public boolean isValidated()
  {
    if (noRadio.isSelected())
    {
      parent.exit();
      return false;
    }
    else
      return (yesRadio.isSelected());
  }


  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    if (!yesRadio.isSelected())
      parent.lockNextButton();
  }
}

