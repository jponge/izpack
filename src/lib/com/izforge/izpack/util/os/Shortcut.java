/*
 * IzPack version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2002 by Elmar Grom
 *
 * File :               Shortcut.java
 * Description :        mapping class for the shortcut API
 * Author's email :     elmar@grom.net
 * Website :            http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package   com.izforge.izpack.util.os;

import    java.util.*;

/*---------------------------------------------------------------------------*/
/**
 * This class represents a shortcut in a operating system independent way. 
 * OS specific subclasses are used to implement the necessary mapping from
 * this generic API to the classes that reflect the system dependent AIP.
 *
 * @see   com.izforge.izpack.util.TargetFactory
 *
 * @version  0.0.1 / 3/4/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class Shortcut 
{
  // ------------------------------------------------------------------------
  // Constant Definitions
  // ------------------------------------------------------------------------
  public static final int       APPLICATIONS    = 1;
  public static final int       START_MENU      = 2;
  public static final int       DESKTOP         = 3;
  public static final int       START_UP        = 4;

  /** Hide the window when starting.*/
  public static final int       HIDE            = 0;
  /** Show the window 'normal' when starting. Usually restores the window
      properties at the last shut-down. */
  public static final int       NORMAL          = 1;
  /** Show the window minimized when starting. */
  public static final int       MINIMIZED       = 2;
  /** Show the window maximized when starting. */
  public static final int       MAXIMIZED       = 3;
  
  /** identifies the user type as the current user */
  public static final int       CURRENT_USER    = 1;
  /** identifies the user type as valid for all users */
  public static final int       ALL_USERS       = 2;

 /*--------------------------------------------------------------------------*/
 /**
  * This method initializes the object. It is used as a replacement for the
  * contructor because of the way it is instantiated through the
  * <code>TargetFactory</code>.
  *
  * @param     type   the type or classification of the program group in which
  *                   the link should exist.
  * @param     name   the name of the shortcut.
  */
 /*--------------------------------------------------------------------------*/
  public void initialize (int    type,
                          String name) throws Exception
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns a list of currently existing program groups, based on the
  * requested type. For example if the type is <code>APPLICATIONS</code> then
  * all the names of the program groups in the applications menu would be
  * returned.
  *
  * @param     userType   the type of user for the program group set.
  *
  * @param     type   the type or classification of the program group.
  *
  * @return    a <code>Vector</code> of <code>String</code> objects that
  *            represent the names of the existing program groups. It is
  *            theoretically possible that this list is empty.
  *
  * @see       #APPLICATIONS
  * @see       #START_MENU  
  */
 /*--------------------------------------------------------------------------*/
  public Vector getProgramGroups (int userType)
  {
    return (null);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Subclass implementations return the fully qualified file name under which
  * the link is saved on disk. <b>Note:</b> this method returns valid results
  * only if the instance was created from a file on disk or after a successful
  * save operation. An instance of this class returns an empty string.
  *
  * @return    an empty <code>String</code>
  */
 /*--------------------------------------------------------------------------*/
  public String getFileName ()
  {
    return ("");
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Subclass implementations return the path of the directory where the link
  * file is stored, if it was necessary during the previous save operation to
  * create the directory. This method returns <code>null</code> if no save
  * operation was carried out or there was no need to create a directory
  * during the previous save operation.
  *
  * @return    this implementation returns always <code>null</code>.
  */
 /*--------------------------------------------------------------------------*/
  public String getDirectoryCreated ()
  {
    return (null);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns <code>true</code> if the target OS supports current user and
  * all users.
  *
  * @return    <code>true</code> if the target OS supports current and all users.
  */
 /*--------------------------------------------------------------------------*/
  public boolean multipleUsers ()
  {
    return (false);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Determines if a specific instance of this class supports the creation of
  * shortcuts. The use of this method might seem odd, since one would not
  * implement a flavor of this class that does not actually support the creation
  * of shortcuts. In other words all flavors will in all probability return true.
  * The only version that can be expected to return false is this class itself,
  * since it has no actual implementation for shortcut creation. This is left
  * to OS specific flavors. If the installer is launched on a unsupported OS
  * there will be no appropriate flavor of this class, which will cause this
  * class itself to be instantiated. The client code can now determine by
  * calling this method if the active OS is supported and take appropriate action.
  *
  * @return    <code>true</code> if the creation of shortcuts is supported,
  *            <code>flase</code> if this is not supported.
  */
 /*--------------------------------------------------------------------------*/
  public boolean supported ()
  {
    return (false);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the command line arguments that will be passed to the target when
  * the link is activated.
  *
  * @param     arguments    the command line arguments
  */
 /*--------------------------------------------------------------------------*/
  public void setArguments (String arguments)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the description string that is used to identify the link in a menu
  * or on the desktop.
  *
  * @param     description  the descriptiojn string
  */
 /*--------------------------------------------------------------------------*/
  public void setDescription (String description)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the location of the icon that is shown for the shortcut on the
  * desktop.
  *
  * @param     path   a fully qualified file name of a file that contains
  *                   the icon.
  * @param     index  the index of the specific icon to use in the file.
  *                   If there is only one icon in the file, use an index
  *                   of 0.
  */
 /*--------------------------------------------------------------------------*/
  public void setIconLocation (String path,
                               int    index)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the name of the program group this ShellLinbk should be placed in.
  *
  * @param     groupName    the name of the program group
  */
 /*--------------------------------------------------------------------------*/
  public void setProgramGroup (String groupName)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the show command that is passed to the target application when the
  * link is activated. The show command determines if the the window will be
  * restored to the previous size, minimized, maximized or visible at all. 
  * <br><br>
  * <b>Note:</b><br>
  * Using <code>HIDE</code> will cause the target window not to show at
  * all. There is not even a button on the taskbar. This is a very useful
  * setting when batch files are used to launch a Java application as it
  * will then appear to run just like any native Windows application.<br>
  *
  * @param     show   the show command. Valid settings are: <br>
  *                   <ul>
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#HIDE}
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#NORMAL}
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#MINIMIZED}
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#MAXIMIZED}
  *                   </ul>
  *
  * @see       #getShowCommand
  */
 /*--------------------------------------------------------------------------*/
  public void setShowCommand (int show)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the absolute path to the shortcut target.
  *
  * @param     path     the fully qualified file name of the target
  */
 /*--------------------------------------------------------------------------*/
  public void setTargetPath (String path)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the working directory for the link target.
  *
  * @param     dir    the working directory
  */
 /*--------------------------------------------------------------------------*/
  public void setWorkingDirectory (String dir)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the name shown in a menu or on the desktop for the link.
  *
  * @param     name   The name that the link should display on a menu or on
  *                   the desktop. Do not include a file extension.
  */
 /*--------------------------------------------------------------------------*/
  public void setLinkName (String name)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the type of link
  *
  * @param     type   The type of link desired. The following values can be set:<br>
  *                   <ul>
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#DESKTOP}
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#PROGRAM_MENU}
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#START_MENU}
  *                   <li>{@link com.izforge.izpack.util.os.Shortcut#STARTUP}
  *                   </ul>
  *
  * @exception IllegalArgumentException if an an invalid type is passed
  */
 /*--------------------------------------------------------------------------*/
  public void setLinkType (int type) throws IllegalArgumentException
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the user type for the link
  *
  * @param     userType  the type of user for the link.
  * 
  * @see       #com.izforge.izpack.util.os.Shortcut.CURRENT_USER
  * @see       #com.izforge.izpack.util.os.Shortcut.ALL_USERS
  */
 /*--------------------------------------------------------------------------*/
  public void setUserType (int type)
  {
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Saves this link.
  *
  * @exception Exception if problems are encountered
  */
 /*--------------------------------------------------------------------------*/
  public void save () throws Exception
  {
  }
}
/*---------------------------------------------------------------------------*/
