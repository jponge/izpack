/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge, 2004 Klaus Bartz
 *
 *  File :               TargetPanel.java
 *  Description :        A panel to select the installation path.
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

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;

/**
 *  The taget directory selection panel.
 *
 * @author     Julien Ponge
 */
public class TargetPanel extends PathInputPanel
{

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public TargetPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
    // load the default directory info (if present)
    loadDefaultInstallDir(parent);
    if (getDefaultInstallDir() != null)
    {
      // override the system default that uses app name (which is set in the Installer class)
      idata.setInstallPath(getDefaultInstallDir());
    }
  }

  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    // Resolve the default for chosenPath
    super.panelActivate();
    // Set the default or old value to the path selection panel.
    pathSelectionPanel.setPath(idata.getInstallPath());
  }

  /**
   * This method simple delegates to
   * <code>PathInputPanel.loadDefaultInstallDir</code> with
   * the current parent as installer frame.
   */
  public void loadDefaultDir()
  {
    super.loadDefaultInstallDir(parent);
  }

  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    Wether the panel has been validated or not.
   */
  public boolean isValidated()
  {
    // Standard behavior of PathInputPanel.
    if( ! super.isValidated())
      return(false);
    idata.setInstallPath(pathSelectionPanel.getPath());
    return(true);
  }


  /**
   * Returns the default install directory. This is equal to 
   * <code>PathInputPanel.getDefaultInstallDir</code>
   * @return the default install directory
   */
  public String getDefaultDir()
  {
    return getDefaultInstallDir();
  }

  /**
   * Sets the default install directory to the given
   * String. This is equal to 
   * <code>PathInputPanel.setDefaultInstallDir</code>
   * @param defaultDir path to be used for the install directory 
   */
  public void setDefaultDir(String defaultDir)
  {
    setDefaultInstallDir(defaultDir);
  }

  /**
   *  Asks to make the XML panel data.
   *
   * @param  panelRoot  The tree to put the data in.
   */
  public void makeXMLData(XMLElement panelRoot)
  {
    new TargetPanelAutomationHelper().makeXMLData(idata, panelRoot);
  }
  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
   */
  public String getSummaryBody()
  {
    return(idata.getInstallPath());
  }
}
