/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Tino Schwarze, Julien Ponge
 *
 *  File :               CompileHandler.java
 *  Description :        A panel to compile files after installation
 *  Author's email :     tino.schwarze@informatik.tu-chemnitz.de
 *  Author's Website :   http://www.tisc.de
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

/**
 *  Interface for monitoring compilation progress.
 *
 * This is used by <code>CompilePanel</code>, <code>CompileWorker</code> and
 * <code>CompilePanelAutomationHelper</code> to display the progress of the
 * compilation. Most of the functionality, however, is inherited from interface
 * com.izforge.izpack.util.AbstractUIProgressHandler
 *
 * @author   Tino Schwarze
 * @see com.izforge.izpack.util.AbstractUIProgressHandler
 */
public interface CompileHandler extends com.izforge.izpack.util.AbstractUIProgressHandler
{
  /**
   * An error was encountered.
   *
   * This method should notify the user of the error and request a choice
   * whether to continue, abort or reconfigure. It should alter the error
   * accordingly.
   *
   * Although a CompileResult is passed in, the method is only called if
   * something failed.
   *
   * @param error the error to handle
   */
  public void handleCompileError (CompileResult error);

}

