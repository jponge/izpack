/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               SummaryPanel.java
 *  Description :        A panel to give a summary of sampled data.
 *  Author's email :     bartzkau@users.berlios.de
 *  Website :            http://www.izforge.com
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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.MultiLineLabel;
import com.izforge.izpack.util.SummaryProcessor;

/**
 * Summary panel to use before InstallPanel.
 * This panel calls the {@link SummaryProcessor}
 * which calls all declared panels for a summary
 * and shows the given captiond and messaged in a 
 * <code>JEditorPane</code>.
 *
 * @author     Klaus Bartz
 *
 */
public class SummaryPanel extends IzPanel
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The text area. */
  private JEditorPane textArea;

  /**
   *  The constructor.
   *
   * @param  parent  The parent.
   * @param  idata   The installation data.
   */
  public SummaryPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
    // We initialize our layout
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints gbConstraints = new GridBagConstraints();
    setLayout(layout);
    MultiLineLabel introLabel = 
      createMultiLineLabelLang( "SummaryPanel.info");    
    parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
    gbConstraints.insets = new Insets(0, 0, 20, 0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    add(introLabel, gbConstraints);


    try
    {
      textArea = new JEditorPane();
      textArea.setContentType("text/html");
      textArea.setEditable(false);
      JScrollPane scroller = new JScrollPane(textArea);
      parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
      gbConstraints.anchor = GridBagConstraints.CENTER;
      gbConstraints.fill = GridBagConstraints.BOTH;
      add(scroller, gbConstraints);
    } 
    catch (Exception err)
    {
      err.printStackTrace();
    }
  }
  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.IzPanel#panelActivate()
   */
  public void panelActivate()
  {
    super.panelActivate();
    textArea.setText(SummaryProcessor.getSummary(idata));
    textArea.setCaretPosition(0);
  }

}
