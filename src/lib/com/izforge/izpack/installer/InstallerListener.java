/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               InstallerListener.java
 *  Description :        Custom action listener interface for install time.
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

/**
 * <p>Implementations of this class are used
 * to handle customizing installation.
 * The defined methods are called from the unpacker at
 * different, well defined points of installation.</p>
 *
 * @author  Klaus Bartz
 * 
 */
public interface InstallerListener
{
  // ------------------------------------------------------------------------
  // Constant Definitions
  // ------------------------------------------------------------------------
  public static final int BEFORE_FILE = 1;
  public static final int AFTER_FILE = 2;
  public static final int BEFORE_DIR = 3;
  public static final int AFTER_DIR = 4;
  public static final int BEFORE_PACK = 5;
  public static final int AFTER_PACK = 6;
  public static final int BEFORE_PACKS = 7;
  public static final int AFTER_PACKS = 8;
 
  /**
   * This method will be called from the unpacker before the installation
   * of all packs will be performed.
   * @param idata object containing the current installation data
   * @param npacks number of packs which are defined for this installation
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
  void beforePacks(AutomatedInstallData idata, Integer npacks, AbstractUIProgressHandler handler)
    throws Exception;

  /**
   * This method will be called from the unpacker before the installation
   * of one pack will be performed.
   * @param pack current pack object
   * @param i current pack number
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
  void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    throws Exception;

  /**
   * Returns true if this listener would be informed at every file and directory
   * installation, else false. If it is true, the listener will be called two
   * times (before and after) for every action. Handle carefully, else
   * performance problems are possible.
   * @return true if this listener would be informed at every file and directory
   * installation, else false
   */
  boolean isFileListener();
 
  /**
   * This method will be called from the unpacker before one
   * directory should be created. If parent directories should be
   * created also, this method will be called for every directory
   * beginning with the base.
   * @param dir current File object of the just directory which should be created
   * @param pf corresponding PackFile object
   * @throws Exception
   */
  void beforeDir( File dir, PackFile pf ) throws Exception;

  /**
   * This method will be called from the unpacker after one
   * directory was created. If parent directories should be
   * created, this method will be called for every directory
   * beginning with the base.
   * @param dir current File object of the just created directory
   * @param pf corresponding PackFile object
   * @throws Exception
   */
  void afterDir( File dir, PackFile pf ) throws Exception;

  /**
   * This method will be called from the unpacker before one
   * file should be installed.
   * @param file current File object of the file which should be installed
   * @param pf corresponding PackFile object
   * @throws Exception
   */
  void beforeFile( File file, PackFile pf ) throws Exception;

  /**
   * This method will be called from the unpacker after one
   * file was installed.
   * @param file current File object of the just installed file
   * @param pf corresponding PackFile object
   * @throws Exception
   */
  void afterFile( File file, PackFile pf ) throws Exception;

  /**
   * 
   * This method will be called from the unpacker after the installation
   * of one pack was performed.
   * @param pack current pack object
   * @param i current pack number
   * @param handler a handler to the current used UIProgressHandler
   */
  void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    throws Exception;

  /**
   * This method will be called from the unpacker after the installation
   * of all packs was performed.
   * @param idata object containing the current installation data
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
	void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
    throws Exception;
}
