/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Julien Ponge
 *
 *  File :               PackagerListener.java
 *  Description :        Interface for Packagers listeners.
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
package com.izforge.izpack.compiler;

/**
 *  An interface for classes that want to listen to a packager events.
 *
 * @author     Julien Ponge
 * @created    October 26, 2002
 */
public interface PackagerListener
{
  /**
   *  Called as the packager sends messages.
   *
   * @param  info  The information that has been sent.
   */
  public void packagerMsg(String info);


  /**  Called when the packager starts.  */
  public void packagerStart();


  /**  Called when the packager stops.  */
  public void packagerStop();
}

