/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Julien Ponge
 *
 *  File :               DestroyerListener.java
 *  Description :        The destroyer listener interface.
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
package com.izforge.izpack.uninstaller;

/**
 *  The destroyer listener interface.
 *
 * @author     Julien Ponge
 * @created    November 1, 2002
 */
public interface DestroyerListener
{
  /**
   *  The destroyer starts.
   *
   * @param  min  The minimum progress value.
   * @param  max  The maximum progress value.
   */
  public void destroyerStart(int min, int max);


  /**  The destroyer stops.  */
  public void destroyerStop();


  /**
   *  The destroyer progresses.
   *
   * @param  pos      The current position.
   * @param  message  The current progress message.
   */
  public void destroyerProgress(int pos, String message);


  /**
   *  The destroyer encountered an error.
   *
   * @param  error  The error text.
   */
  public void destroyerError(String error);
}

