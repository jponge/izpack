/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               CustomAction.java
 *  Description :        Custom action description.
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

package com.izforge.izpack;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Container for serialized custom action data</p>
 *
 * @author  Klaus Bartz
 *
 */
public class CustomActionData implements Serializable
{
  /** Identifier for custom action data typ "installer listener". */
  public static final int INSTALLER_LISTENER = 0;
  /** Identifier for custom action data typ "uninstaller listener". */
  public static final int UNINSTALLER_LISTENER = 1;
  /** Identifier for custom action data typ "uninstaller lib". 
   *  This is used for binary libs (DLLs or SHLs or SOs or ...) which
   *  will be needed from the uninstaller.
   */
  public static final int UNINSTALLER_LIB = 2;
  
  
  /**  The custom action classname. */
  public String name;

  /**  The target operation system of this custom action */
  public List osConstraints = null;
  
  /** Type of this custom action data; possible are 
   * INSTALLER_LISTENER, UNINSTALLER_LISTENER and UNINSTALLER_LIB.
   */
  public int type = 0;

  
  /**
   * Constructs an CustomAction object with the needed values.
   * @param name custom action data name (full qualified class name or library name)
   * @param osConstraints target operation system of this custom action
   * @param type type of this custom action
   */
  public CustomActionData(String name, List osConstraints, int type)
  {
    this.name = name;
    this.osConstraints = osConstraints;
    this.type = type;
  }

}
