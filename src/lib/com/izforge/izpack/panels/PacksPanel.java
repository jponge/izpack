/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               PacksPanel.java
 *  Description :        A panel to select the packs to install.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (C) 2002 Marcus Wolschon
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

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.IoHelper;

/**
 *  The packs selection panel class.
 *  This class handles only the layout. Common
 *  stuff are handled by the base class.
 *
 * @author     Julien Ponge
 * @author     Jan Blok
 * @author     Klaus Bartz
 */
public class PacksPanel extends PacksPanelBase
{

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public PacksPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.panels.PacksPanelBase#createNormalLayout()
   */
  protected void createNormalLayout()
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    createLabel( "PacksPanel.info", "preferences", null, null);
    add(Box.createRigidArea(new Dimension(0, 3)));
    createLabel( "PacksPanel.tip", "tip", null, null);
    add(Box.createRigidArea(new Dimension(0, 5)));
    tableScroller = new JScrollPane();
    packsTable = createPacksTable(300, tableScroller, null, null );
    if( dependenciesExist )
      dependencyArea = createTextArea("PacksPanel.dependencyList", null, null, null );
    descriptionArea = createTextArea("PacksPanel.description", null, null, null );
    spaceLabel = createPanelWithLabel( "PacksPanel.space",  null, null );
    if( IoHelper.supported("getFreeSpace"))
    {
      add(Box.createRigidArea(new Dimension(0, 3)));
      freeSpaceLabel = createPanelWithLabel( "PacksPanel.freespace",  null, null );
    }
  }

}
