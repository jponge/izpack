/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               ActionBase.java
 *  Description :        Data class for ant action listeners.
 *  Author's email :     bartzkau@users.berlios.de
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
package com.izforge.izpack.event;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Base class for action classes like AntAction.
 *
 * @author     Klaus Bartz
 *
 */
public class ActionBase  implements Serializable
{
  // --- String constants for parsing the XML specification  -----------------
  // --- These definitions are placed here because the const strings are -----
  // --- used by more than one InstallerListener and UninstallerListener -----
  // --- class. --------------------------------------------------------------
  
  public static final String PACK = "pack";
  public static final String NAME = "name";
  // Order related "symbols"
  public static final String ORDER = "order";
  public static final String BEFOREPACK = "beforepack";
  public static final String AFTERPACK = "afterpack";
  public static final String BEFOREPACKS = "beforepacks";
  public static final String AFTERPACKS = "afterpacks";
  public static final String UNINSTALL_ORDER = "uninstall_order";
  public static final String BEFOREDELETION = "beforedeletion";
  public static final String AFTERDELETION = "afterdeletion";

  public static final String PROPERTY = "property";
  public static final String VALUE = "value";

  public static final String YES = "yes";
  public static final String NO = "no";
  public static final String FALSE = "false";
  public static final String TRUE = "true";

  public static final String QUIET = "quiet";
  public static final String VERBOSE = "verbose";

  public static final String LOGFILE = "logfile";
  public static final String BUILDFILE = "buildfile";
  public static final String PROPERTYFILE = "propertyfile";

  public static final String PATH = "path";
  public static final String SRCDIR = "srcdir";
  public static final String TARGETDIR = "targetdir";

  public static final String TARGET = "target";
  public static final String UNINSTALL_TARGET = "uninstall_target";
  public static final String ACTION = "action";
  public static final String UNINSTALL_ACTION = "uninstall_action";

  public static final String ONDEST = "ondestination";
  public static final String COPY = "copy";
  public static final String REMOVE = "remove";
  public static final String REWIND = "rewind";
  public static final String TOUCH = "touch";
  public static final String MOVE = "move";
  public static final String OVERRIDE = "override";
  public static final String UPDATE = "update";
  public static final String NOTHING = "nothing";

  public static final String FILESET = "fileset";
  public static final String INCLUDE = "include";
  public static final String INCLUDES = "includes";
  public static final String EXCLUDE = "exclude";
  public static final String EXCLUDES = "excludes";

  public static final String OS = "os";
  public static final String FAMILY = "family";
  public static final String VERSION = "version";
  public static final String ARCH = "arch";
  public static final String CASESENSITIVE = "casesensitive";
  public static final String UNIX = "unix";
  public static final String WINDOWS = "windows";
  public static final String MAC = "mac";
  public static final String ASKTRUE = "asktrue";
  public static final String ASKFALSE = "askfalse";

  private static final HashSet installOrders = new HashSet();
  private static final HashSet uninstallOrders = new HashSet();

  protected String uninstallOrder = ActionBase.BEFOREDELETION;

  protected String order = null;

  static
  {
    installOrders.add(ActionBase.BEFOREPACK);
    installOrders.add(ActionBase.AFTERPACK);
    installOrders.add(ActionBase.BEFOREPACKS);
    installOrders.add(ActionBase.AFTERPACKS);
    uninstallOrders.add(ActionBase.BEFOREDELETION);
    uninstallOrders.add(ActionBase.AFTERDELETION);
  }

  /**
   * Default constructor
   */
  public ActionBase()
  {
    super();
  }
  
  /**
   * Returns the order.
   * @return the order
   */
  public String getOrder()
  {
    return order;
  }
    
  /**
   * Sets the order to the given string.
   * Valid values are "beforepacks", "beforepack",
   * "afterpack" and "afterpacks".
   * @param order order to be set
   */
  public void setOrder( String order ) throws Exception
  {
    if( ! installOrders.contains(order ))
      throw new Exception("Bad value for order.");
    this.order = order;
  }
    
  /**
   * Returns the order for uninstallation.
   * @return the order for uninstallation
   */
  public String getUninstallOrder()
  {
    return uninstallOrder;
  }
    
  /**
   * Sets the order to the given string for uninstallation.
   * Valid values are "beforedeletion" and "afterdeletion".
   * @param order order to be set
   */
  public void setUninstallOrder( String order ) throws Exception
  {
    if( ! uninstallOrders.contains(order ))
      throw new Exception("Bad value for order.");
    this.uninstallOrder = order;
  }
    


}
