/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               XInfoPanel.java
 *  Description :        A panel to show some adaptative textual information.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (c) 2001 Johannes Lehtinen
 *  johannes.lehtinen@iki.fi
 *  http://www.iki.fi/jle/
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

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 *  The XInfo panel class - shows some adaptative text (ie by parsing for some
 *  variables.
 *
 * @author     Julien Ponge
 */
public class XInfoPanel extends IzPanel
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The info label. */
  private JLabel infoLabel;

  /**  The text area. */
  private JTextArea textArea;

  /**  The info to display. */
  private String info;

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public XInfoPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    setLayout(layout);

    // We add the components

    infoLabel =
      LabelFactory.create(
        parent.langpack.getString("InfoPanel.info"),
        parent.icons.getImageIcon("edit"),
        JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(infoLabel, gbConstraints);
    add(infoLabel);

    textArea = new JTextArea();
    textArea.setEditable(false);
    JScrollPane scroller = new JScrollPane(textArea);
    parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 0.9);
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scroller, gbConstraints);
    add(scroller);
  }

  /**  Loads the info text.  */
  private void loadInfo()
  {
    try
    {
      // We read it
      info = ResourceManager.getInstance().getTextResource("XInfoPanel.info");
    } catch (Exception err)
    {
      info = "Error : could not load the info text !";
    }
  }

  /**  Parses the text for special variables.  */
  private void parseText()
  {
    try
    {
      // Initialize the variable substitutor
      VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

      // Parses the info text
      info = vs.substitute(info, null);
    } catch (Exception err)
    {
      err.printStackTrace();
    }
  }

  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    // Text handling
    loadInfo();
    parseText();

    // UI handling
    textArea.setText(info);
    textArea.setCaretPosition(0);
  }

  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    Always true.
   */
  public boolean isValidated()
  {
    return true;
  }
}
