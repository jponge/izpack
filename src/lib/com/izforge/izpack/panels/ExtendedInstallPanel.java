/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               ExtendedInstallPanel.java
 *  Description :        A panel to launch the installation process.
 *  Author's email :     bartzkau@users.berlios.de
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


import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.ExtendedUIProgressHandler;

/**
 *  The install panel class. 
 * Launches the actual installation job with extensions for
 * custom actions.
 *
 * @author     Klaus Bartz
 */
public class ExtendedInstallPanel extends InstallPanel
implements ExtendedUIProgressHandler
{
  protected int currentStep = 0;


  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
   public ExtendedInstallPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.util.ExtendedUIProgressHandler#startAction(java.lang.String, java.lang.String, java.lang.String, int)
   */
  public void restartAction(String name, String overallMsg, String tipMsg, int no_of_steps)
  {
    overallOpLabel.setText(overallMsg);
    tipLabel.setText(tipMsg);
    currentStep = 0;
    startAction(name, no_of_steps );
  }
  
  /**
   *  Normal progress indicator.
   *
   * @param  val  The progression value.
   * @param  msg  The progression message.
   */
  public void progress(int val, String msg)
  {
    packProgressBar.setValue(val + 1);
    packOpLabel.setText(msg);
    currentStep++;
  }


  /* (non-Javadoc)
   * @see com.izforge.izpack.util.ExtendedUIProgressHandler#progress(java.lang.String, java.lang.String)
   */
  public void progress(String stepMessage)
  {
    packOpLabel.setText(stepMessage);
    currentStep++;
    packProgressBar.setValue(currentStep);
  }

  /**
   *  Pack changing.
   *
   * @param  packName  The pack name.
   * @param  stepno    The number of the pack.
   * @param  max       The new maximum progress.
   */
  public void nextStep(String packName, int stepno, int max)
  {
    currentStep = 0;
    super.nextStep(packName, stepno, max);
  }

}
