/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               SummaryLoggerInstallerListener.java
 *  Description :        Installer listener which writes the summary into a logfile.
 *  Author's email :     bartzkau@users.berlios.de
 *  Website :            http://www.izforge.com
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.VariableSubstitutor;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.SummaryProcessor;

/**
 * Installer listener which writes the summary of all panels
 * into the logfile which is defined by info.summarylogfilepath. 
 * Default is $INSTALL_PATH/Uninstaller/InstallSummary.htm
 *
 * @author  Klaus Bartz
 *
 */
public class SummaryLoggerInstallerListener extends SimpleInstallerListener
{
  /**
   * Default constructor.
   */
  public SummaryLoggerInstallerListener()
  {
    super(false);
  }
  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData, com.izforge.izpack.util.AbstractUIProgressHandler)
   */
  public void afterPacks(
    AutomatedInstallData idata,
    AbstractUIProgressHandler handler)
    throws Exception
  {
    if( ! getInstalldata().installSuccess )
      return;
    // No logfile at automated installation because panels are not
    // involved.
    if( getInstalldata().panels == null || getInstalldata().panels.size() < 1)
      return;
    String path = getInstalldata().info.getSummaryLogFilePath();
    if( path == null )
      return;
    VariableSubstitutor vs = new VariableSubstitutor(getInstalldata().getVariables());
    path = IoHelper.translatePath(path, vs);
    File parent = new File(path).getParentFile();
    
    if( ! parent.exists())
    {
      parent.mkdirs();
    }
    PrintWriter logfile = null;
    try
    {
      logfile = new PrintWriter(new FileOutputStream(path), true);
    }
    catch (IOException e)
    {
      Debug.error(e);
    }
    String summary = SummaryProcessor.getSummary(getInstalldata());
    logfile.print(summary);
    logfile.close();
  }

}
