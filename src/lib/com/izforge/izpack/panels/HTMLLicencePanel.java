/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               HTMLLicencePanel.java
 *  Description :        A panel to prompt the user for a licence agreement.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 *  The IzPack HTML license panel.
 *
 * @author     Julien Ponge
 */
public class HTMLLicencePanel extends IzPanel implements HyperlinkListener, ActionListener
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The info label. */
  private JLabel infoLabel;

  /**  The text area. */
  private JEditorPane textArea;

  /**  The agreement label. */
  private JLabel agreeLabel;

  /**  The radio buttons. */
  private JRadioButton yesRadio, noRadio;


  /**
   *  The constructor.
   *
   * @param  idata   The installation data.
   * @param  parent  Description of the Parameter
   */
  public HTMLLicencePanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    setLayout(layout);

    // We load the licence
    loadLicence();

    // We put our components

    infoLabel = new JLabel(parent.langpack.getString("LicencePanel.info"),
      parent.icons.getImageIcon("history"), JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 0, 2, 1, 1.0, 0.0);
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.WEST;
    layout.addLayoutComponent(infoLabel, gbConstraints);
    add(infoLabel);

    try
    {
      textArea = new JEditorPane();
      textArea.setEditable(false);
      textArea.addHyperlinkListener(this);
      JScrollPane scroller = new JScrollPane(textArea);
      textArea.setPage(loadLicence());
      parent.buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 1.0);
      gbConstraints.anchor = GridBagConstraints.CENTER;
      gbConstraints.fill = GridBagConstraints.BOTH;
      layout.addLayoutComponent(scroller, gbConstraints);
      add(scroller);
    }
    catch (Exception err)
    {
      err.printStackTrace();
    }

    agreeLabel = new JLabel(parent.langpack.getString("LicencePanel.agree"),
      parent.icons.getImageIcon("help"), JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.WEST;
    gbConstraints.fill = GridBagConstraints.NONE;
    layout.addLayoutComponent(agreeLabel, gbConstraints);
    add(agreeLabel);

    ButtonGroup group = new ButtonGroup();

    yesRadio = new JRadioButton(parent.langpack.getString("LicencePanel.yes"), false);
    group.add(yesRadio);
    parent.buildConstraints(gbConstraints, 0, 3, 1, 1, 0.5, 0.0);
    gbConstraints.anchor = GridBagConstraints.WEST;
    layout.addLayoutComponent(yesRadio, gbConstraints);
    add(yesRadio);
    yesRadio.addActionListener(this);

    noRadio = new JRadioButton(parent.langpack.getString("LicencePanel.no"), false);
    group.add(noRadio);
    parent.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.5, 0.0);
    gbConstraints.anchor = GridBagConstraints.EAST;
    layout.addLayoutComponent(noRadio, gbConstraints);
    add(noRadio);
    noRadio.addActionListener(this);
  }


  /**
   *  Loads the license text.
   *
   * @return    The license text URL.
   */
  private URL loadLicence()
  {
    String resNamePrifix = "HTMLLicencePanel.licence";
    try
    {
      return ResourceManager.getInstance().getURL(resNamePrifix);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }


  /**
   *  Actions-handling method (here it launches the installation).
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
   * @return    true if the user agrees with the license, false otherwise.
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


  /**
   *  Hyperlink events handler.
   *
   * @param  e  The event.
   */
  public void hyperlinkUpdate(HyperlinkEvent e)
  {
    try
    {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        textArea.setPage(e.getURL());
    }
    catch (Exception err)
    {}
  }


  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    if (!yesRadio.isSelected())
      parent.lockNextButton();
  }
}

