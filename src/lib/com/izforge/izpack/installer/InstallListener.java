/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               InstallListener.java
 *  Description :        Used to monitor the installation progress
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
package com.izforge.izpack.installer;

/**
 *  The installer listener interface. It is callback-driven.
 *
 * @author     Julien Ponge
 * @created    October 27, 2002
 */
public interface InstallListener
{
  /**  The unpacker starts.  */
  public void startUnpack();


  /**
   *  An error was encountered.
   *
   * @param  error  The error message.
   */
  public void errorUnpack(String error);


  /**  The unpacker stops.  */
  public void stopUnpack();


  /**
   *  Normal progress indicator.
   *
   * @param  val  The progress value.
   * @param  msg  The message string.
   */
  public void progressUnpack(int val, String msg);


  /**
   *  Pack changing.
   *
   * @param  min       The new minimum progress.
   * @param  max       The new maximum progress.
   * @param  packName  The new pack name.
   */
  public void changeUnpack(int min, int max, String packName);
}

