/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               InstallerException.java
 *  Description :        Exception for custom actions at install time.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de
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
 * Indicates a Failure in a custom action.
 *
 * @author  Klaus Bartz
 *
 */
public class InstallerException extends Exception
{

  /**
   * 
   */
  public InstallerException()
  {
    super();
  }

  /**
   * @param message
   */
  public InstallerException(String message)
  {
    super(message);
  }

  /**
   * @param cause
   */
  public InstallerException(Throwable cause)
  {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public InstallerException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
