/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               CustomData.java
 *  Description :        Custom data description.
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
 * <p>Container for serialized custom  data</p>
 *
 * @author  Klaus Bartz
 *
 */
public class CustomData implements Serializable
{
  /** Identifier for custom data typ "installer listener". */
  public static final int INSTALLER_LISTENER = 0;
  /** Identifier for custom data typ "uninstaller listener". */
  public static final int UNINSTALLER_LISTENER = 1;
  /** Identifier for custom data typ "uninstaller lib". 
   *  This is used for binary libs (DLLs or SHLs or SOs or ...) which
   *  will be needed from the uninstaller.
   */
  public static final int UNINSTALLER_LIB = 2;
  
  /** Identifier for custom data typ "uninstaller jar files". */
  public static final int UNINSTALLER_JAR = 3;
  

  /**  The contens of the managed custom data. If it is a listener or
   *   a uninstaller jar, all contained files are listed with it 
   *   complete sub path. If it is a uninstaller native
   *   library, this value is the path in the installer jar.
   */
  public List contents;

  /**  Full qualified name of the managed listener. If type
   *   is not a listener, this value is undefined.
   */
  public String listenerName;

  /**  The target operation system of this custom action */
  public List osConstraints = null;
  
  /** Type of this custom action data; possible are 
   * INSTALLER_LISTENER, UNINSTALLER_LISTENER, UNINSTALLER_LIB
   * and UNINSTALLER_JAR.
   */
  public int type = 0;

  
  /**
   * Constructs an CustomData object with the needed values.
   * The list names contains all full qualified class names which are
   * needed by the custom action or contained in the jar file. 
   * At custom actions the first entry is the custom action self.
   * @param names custom action data names (full qualified class name or library name)
   * @param osConstraints target operation system of this custom action
   * @param type type of this custom data
   public CustomData(List names, List osConstraints, int type)
  {
    this.names = names;
    this.osConstraints = osConstraints;
    this.type = type;
  }
  */

  /**
   * Constructs an CustomData object with the needed values.
   * If a listener will be managed with this object, the full qualified
   * name of the listener self must be set as listener name.
   * If a listener or a jar file for uninstall will be managed, all
   * needed files (class, properties and so on) must be referenced
   * in the contents with the path which they have in the installer jar file.
   * @param listenerName path of the listener
   * @param contents also needed objects referenced with the path in install.jar
   * @param osConstraints target operation system of this custom action
   * @param type type of this custom data
   */
  public CustomData(String listenerName, List contents, List osConstraints, int type)
  {
    this.listenerName = listenerName;
    this.contents = contents;
    this.osConstraints = osConstraints;
    this.type = type;
  }

}
