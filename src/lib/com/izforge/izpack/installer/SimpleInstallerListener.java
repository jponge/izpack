/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               SimpleInstallerListener.java
 *  Description :        Simple custom action listener implementation for install time.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de/
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

package com.izforge.izpack.installer;

import java.io.File;

import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.SpecHelper;



/**
 * <p>This class implements all methods of interface
 * InstallerListener, but do not do anything.
 * It can be used as base class to save implementation
 * of unneeded methods.</p>
 * <p>Additional there are some common helper methods
 * which are used from the base class SpecHelper.</p>
 *
 * @author  Klaus Bartz
 *
 */
public class SimpleInstallerListener  implements InstallerListener
{

 
  private AutomatedInstallData installdata = null;
  private SpecHelper  specHelper = null;

  /**
   *  The default constructor.
   */
  public SimpleInstallerListener()
  {
    this(false);
  }

  /**
   * Constructs a simple installer listener.
   * If useSpecHelper is true, a specification helper will be
   * created.
   * @param useSpecHelper
   *  
   */
  public SimpleInstallerListener(boolean useSpecHelper)
  {
    super();
    if( useSpecHelper )
      setSpecHelper( new SpecHelper() );
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#handleFile(java.io.File, com.izforge.izpack.PackFile)
   */
  public void afterFile(File file, PackFile pf)
    throws Exception
  {
    // Do nothing
    ;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#handleDir(java.io.File, com.izforge.izpack.PackFile)
   */
  public void afterDir(File dir, PackFile pf)
    throws Exception
  {
    // Do nothing
    ;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData, com.izforge.izpack.util.AbstractUIProgressHandler)
   */
  public void afterPacks(
    AutomatedInstallData idata,
    AbstractUIProgressHandler handler)
    throws Exception
  {
    
    // Do nothing
    ;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#afterPack(com.izforge.izpack.Pack, int, com.izforge.izpack.util.AbstractUIProgressHandler)
   */
  public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    throws Exception
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData, int, com.izforge.izpack.util.AbstractUIProgressHandler)
   */
  public void beforePacks(
    AutomatedInstallData idata,
    Integer npacks,
    AbstractUIProgressHandler handler)
    throws Exception
  {
    if( installdata == null )
      installdata = idata;
    ;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#beforePack(com.izforge.izpack.Pack, int, com.izforge.izpack.util.AbstractUIProgressHandler)
   */
  public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    throws Exception
  {
    // Do nothing
    ;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.InstallerListener#isFileListener()
   */
  public boolean isFileListener()
  {
    // For default no.
    return false;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.InstallerListener#beforeFile(java.io.File, com.izforge.izpack.PackFile)
   */
  public void beforeFile(File file, PackFile pf) throws Exception
  {
    // Do nothing
    ;
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.InstallerListener#beforeDir(java.io.File, com.izforge.izpack.PackFile)
   */
  public void beforeDir(File dir, PackFile pf) throws Exception
  {
    // Do nothing
    ;
  }

  /**
   * Returns current specification helper.
   * @return current specification helper
   */
  public SpecHelper getSpecHelper()
  {
    return specHelper;
  }

  /**
   * Sets the given specification helper to the current used helper.
   * @param helper specification helper which should be used
   */
  public void setSpecHelper(SpecHelper helper)
  {
    specHelper = helper;
  }

  /**
   * Returns the current installdata object.
   * @return current installdata object
   */
  public AutomatedInstallData getInstalldata()
  {
    return installdata;
  }

  /**
   * Sets the installdata object.
   * @param data installdata object which should be set to current
   */
  public void setInstalldata(AutomatedInstallData data)
  {
    installdata = data;
  }

}
