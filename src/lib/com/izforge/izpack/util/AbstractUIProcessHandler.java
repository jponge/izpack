/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2002 Olexij Tkatchenko
 *
 *  File :               FileExecutor.java
 *  Description :        File execution class.
 *  Author's email :     ot@parcs.de
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
package com.izforge.izpack.util;

/**
 * Interface for UIs which need to interface to external processes.
 * 
 * @author tisc
 */
public interface AbstractUIProcessHandler extends AbstractUIHandler
{
  /**
   * Log the given message.
   * 
   * @param message
   * @param stderr true if this is a message received from a program via stderr
   */
  public void logOutput (String message, boolean stderr);
  
  public void startProcessing (int no_of_processes);
  
  /**
   * Notify the user that a process has started.
   * 
   * @param name
   */
  public void startProcess (String name);
  
  public void finishProcess ();
  
  public void finishProcessing ();
}
