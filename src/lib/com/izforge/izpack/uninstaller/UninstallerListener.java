/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               UninstallerListener.java
 *  Description :        Custom action listener interface for uninstall time.
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

package com.izforge.izpack.uninstaller;

import java.io.File;
import java.util.List;

import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * <p>Implementations of this class are used
 * to handle customizing uninstallation.
 * The defined methods are called from the destroyer at
 * different, well defined points of uninstallation.</p>
 *
 * @author  Klaus Bartz
 *
 */
public interface UninstallerListener
{
  // ------------------------------------------------------------------------
  // Constant Definitions
  // ------------------------------------------------------------------------
  public static final int BEFORE_DELETION = 1;
  public static final int AFTER_DELETION = 2;
  public static final int BEFORE_DELETE = 3;
  public static final int AFTER_DELETE = 4;
 
  /**
   * This method will be called from the destroyer before
   * the given files will be deleted.
   * @param files all files which should be deleted
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
  void beforeDeletion(List files, AbstractUIProgressHandler handler)
    throws Exception;

  /**
   * Returns true if this listener would be informed at every 
   * delete operation, else false. If it is true, the listener will be called two
   * times (before and after) of every action. Handle carefully, else
   * performance problems are possible.
   * @return true if this listener would be informed at every delete
   * operation, else false
   */
  boolean isFileListener();
 
  /**
   * This method will be called from the destroyer before
   * the given file will be deleted.
   * @param file file which should be deleted
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
  void beforeDelete(File file, AbstractUIProgressHandler handler)
    throws Exception;
  /**
   * This method will be called from the destroyer after
   * the given file was deleted.
   * @param file file which was just deleted
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
  void afterDelete(File file, AbstractUIProgressHandler handler)
    throws Exception;
  /**
   * This method will be called from the destroyer after
   * the given files are deleted.
   * @param files all files which where deleted
   * @param handler a handler to the current used UIProgressHandler
   * @throws Exception
   */
  void afterDeletion(List files, AbstractUIProgressHandler handler)
    throws Exception;
 

}
