/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               ProgressBarInstallerListener.java
 *  Description :        Installer listener for support of progress bar interactions.
 *  Author's email :     bartzkau@users.berlios.de
 * 
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
package com.izforge.izpack.event;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.ExtendedUIProgressHandler;

/**
 * Installer listener for reset the progress bar and initialize
 * the simple installer listener to support progress bar interaction.
 * To support progress bar interaction add this installer listener
 * as first listener.
 *
 * @author  Klaus Bartz
 *
 */
public class ProgressBarInstallerListener extends SimpleInstallerListener
{

  /**
   * 
   */
  public ProgressBarInstallerListener()
  {
    super(false);
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData, com.izforge.izpack.util.AbstractUIProgressHandler)
   */
  public void afterPacks(
    AutomatedInstallData idata,
    AbstractUIProgressHandler handler)
    throws Exception
  {
    if( handler instanceof ExtendedUIProgressHandler &&
      getProgressBarCallerCount() > 0)
    {
      String progress = getMsg("CustomActions.progress");
      String tip = getMsg("CustomActions.tip");
      if( tip.equals("CustomActions.tip") || progress.equals("CustomActions.progress"))
      {
        Debug.trace("No messages found for custom action progress bar interactions; skiped.");
        return;
      }
      ((ExtendedUIProgressHandler) handler).restartAction("Configure", 
        progress, tip, getProgressBarCallerCount());
      SimpleInstallerListener.doInformProgressBar = true;
    }
  }


}
