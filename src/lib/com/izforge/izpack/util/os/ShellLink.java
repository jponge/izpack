/*
 * IzPack version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2002 by Elmar Grom
 *
 * File :               ShellLink.java
 * Description :        Represents a MS-Windows Shell Link (shortcut)
 *                      This is the Java side of the implementation
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

import    java.lang.*;
import    java.io.*;
import    com.izforge.izpack.util.*;

/*---------------------------------------------------------------------------*/
/**
 * This class represents a MS-Windows shell link, aka shortcut. It
 * supports creation, modification and deletion as well as reporting on
 * details of shell links. This class uses a number of native methods to
 * access the MS-Windows registry and load save and manipulate link data.
 * The native code is contained in the file <code>ShellLink.cpp</code>.
 * <br><br>
 * For more detailed information on Windows shortcuts read the win32
 * documentation from Microsoft on the IShellLink interface. There are
 * also useful articles on this topic on the MIcrosoft website.
 * <br><br>
 * <A HREF=http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnmgmt/html/msdn_shellnk1.asp>Using Shell Links in Windows 95</A><br>
 * <A HREF=http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnmgmt/html/msdn_shellnk2.asp>The IShellLink Interface</A><br>
 * <A HREF=http://msdn.microsoft.com/library/default.asp?url=/library/en-us/shellcc/platform/Shell/IFaces/IShellLink/IShellLink.asp>IShellLink</A>
 *
 * @version  0.0.1 / 1/21/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class ShellLink implements NativeLibraryClient
{
  // ------------------------------------------------------------------------
  // Constant Definitions
  // ------------------------------------------------------------------------
  /** Hide the window when starting. This is particularly useful when
      launching from a *.bat file, because no DOS window and no button
      for the DOS window on the task bar will show!
      <br><br>
      <b>Note:</b> this option is not available through the Windows UI! */
  public  static final int      HIDE          = 0;
  /** Show the window 'normal' when starting. Restores the window properties
      at the last shut-down. */
  public  static final int      NORMAL        = 1;
  /** Show the window minimized when starting. The window will not show but
      a corresponding button in the task bar will. */
  public  static final int      MINIMIZED     = 2;
  /** Show the window maximized when starting. */
  public  static final int      MAXIMIZED     = 3;

  private static final int      MIN_SHOW      = 0;  
  private static final int      MAX_SHOW      = 3;  

  // ------------------------------------------------------
  // Shortcut types
  // ------------------------------------------------------
  /** This type of shortcut shows on the desktop */
  public  static final int      DESKTOP       = 1;
  /** This type of shortcut shows in the program menu */
  public  static final int      PROGRAM_MENU  = 2;
  /** This type of shortcut shows in the start menu */
  public  static final int      START_MENU    = 3;
  /** This type of shortcut is executed at OS launch time  */
  public  static final int      STARTUP       = 4;

  private static final int      MIN_TYPE      = 1;  
  private static final int      MAX_TYPE      = 4;  

  // ------------------------------------------------------
  // Return values from nafive methods
  // ------------------------------------------------------
  /** Returned from native calls if the call was successful */
  private static final int      SL_OK               =  1;
  /** Unspecific return if a native call was not successful */
  private static final int      SL_ERROR            = -1;
  /** Return value from native initialization functions if
      already initialized */
  private static final int      SL_INITIALIZED      = -2;
  /** Return value from native uninitialization functions if
      never initialized */
  private static final int      SL_NOT_INITIALIZED  = -3;
  /** Return value from native uninitialization functions if
      there are no more interface handles available */
  private static final int      SL_OUT_OF_HANDLES   = -4;
  /** Return value from native uninitialization functions if 
      nohandle for the IPersist interface could be obtained */
  private static final int      SL_NO_IPERSIST      = -5;
  /** Return value from native uninitialization functions if
      the save operation fort the link failed */
  private static final int      SL_NO_SAVE          = -6;
  /** Return value if the function called had to deal with
      unexpected data types. This might be returned by
      registry functions if they receive an unexpected
      data type from the registry. */
  private static final int      SL_WRONG_DATA_TYPE  = -7;

  // ------------------------------------------------------
  // Miscellaneous constants
  // ------------------------------------------------------
  private static final int      UNINITIALIZED       = -1;
  /** the extension that must be used for link files */
  private static final String   LINK_EXTENSION      = ".lnk";
  /** the constant to use for selecting the current user. */
  public  static final int      CURRENT_USER        = 0;
  /** the constant to use for selecting the all users. */
  public  static final int      ALL_USERS           = 1;  

  // ------------------------------------------------------------------------
  // Variable Declarations
  // ------------------------------------------------------------------------
  /** This handle links us to a specific native instance. Do not use or
      modify, the variable is for exclusive use by the native side. */
  private int     nativeHandle            = UNINITIALIZED;

  private String  currentUserLinkPath     = "";
  private String  allUsersLinkPath        = "";
  private String  groupName               = "";
  private String  linkName                = "";
  /** this is the fully qualified name of the link on disk. Note that this
      variable contains only valid data if the link was created from a disk
      file or after a successful save operation. At other times the content
      is upredicatable. */
  private String  linkFileName            = "";
  /** Contains the directory where the link file is stored after any save
      operation that needs to create that directory. Otherwise it contains
      <code>null</code>. */
  private String  linkDirectory           = "";

  private String  arguments               = "";
  private String  description             = "";
  private String  iconPath                = "";
  private String  targetPath              = "";
  private String  workingDirectory        = "";
  /** there seems to be an error in JNI that causes an access violation if
      a String that is accessed from native code borders on another type of
      variable. This caused problems in <code>set()</code> For this reason,
      the dummy string is placed here. Observed with version: <pre>
      java version "1.3.0"
      Java(TM) 2 Runtime Environment, Standard Edition (build 1.3.0-C)
      Java HotSpot(TM) Client VM (build 1.3.0-C, mixed mode) </pre> */
  private String  dummyString             = "";

  private int     hotkey                  = 0;
  private int     iconIndex               = 0;
  private int     showCommand             = NORMAL;
  private int     linkType                = DESKTOP;
  private int     userType                = CURRENT_USER;

  private boolean initializeSucceeded     = false;

  // ------------------------------------------------------------------------
  // Native Methods
  // ------------------------------------------------------------------------
  // For documentation on these methods see ShellLink.cpp
  // ------------------------------------------------------------------------
  private native int initializeCOM ();
  private native int releaseCOM ();
  private native int getInterface ();
  private native int releaseInterface ();
  private native int GetArguments ();
  private native int GetDescription ();
  private native int GetHotkey ();
  private native int GetIconLocation ();
  private native int GetPath ();
  private native int GetShowCommand ();
  private native int GetWorkingDirectory ();
  private native int Resolve ();
  private native int SetArguments ();
  private native int SetDescription ();
  private native int SetHotkey ();
  private native int SetIconLocation ();
  private native int SetPath ();
  private native int SetShowCommand ();
  private native int SetWorkingDirectory ();
  private native int saveLink (String name);
  private native int loadLink (String name);
  private native int GetLinkPath (int target);
  
  /** This method is used to free the library at the end of progam execution.
      After this call, any instance of this calss will not be usable any more! */
  private native void FreeLibrary (String name);

 /*--------------------------------------------------------------------------*/
 /**
  * Creates an instance of <code>ShellLink</code> of a specific type.
  *
  * @param     type   The type of link desired. The following values can be set:<br>
  *                   <ul>
  *                   <li><code>DESKTOP</code>
  *                   <li><code>PROGRAM_MENU</code>
  *                   <li><code>START_MENU</code>
  *                   <li><code>STARTUP</code>
  *                   </ul>
  * @param     name   The name that the link should display on a menu or on
  *                   the desktop. Do not include a file extension.
  *
  * @exception IllegalArgumentException if any of the call parameters are
  *                                     incorrect
  * @exception Exception if problems are encountered in initializing the
  *                      native interface
  */
 /*--------------------------------------------------------------------------*/
  public ShellLink (int    type,
                    String name)  throws Exception, IllegalArgumentException
  {
    if ((type < MIN_TYPE) || 
        (type > MAX_TYPE)   )
    {
      throw (new IllegalArgumentException ("the type parameter used an illegal value"));
    }
    if (name == null)
    {
      throw (new IllegalArgumentException ("the name parameter was null"));
    }
                
    linkName = name;
    linkType = type;
    
    initialize ();
    if (GetLinkPath (linkType) != SL_OK)
    {
      throw (new Exception ("could not get a path for this type of link"));
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Creates an instance of <code>ShellLink</code> from an existing shell link
  * on disk.
  *
  * @param     name      the fully qualified file name of the link.
  * @param     userType  the type of user for the link path.
  * 
  * @see       #CURRENT_USER
  * @see       #ALL_USERS
  *
  * @exception IllegalArgumentException if the name was null
  * @exception Exception if problems are encountered in reading the file
  */
 /*--------------------------------------------------------------------------*/
  public ShellLink (String name,
                    int    userType) throws Exception, IllegalArgumentException
  {
    if (name == null)
    {
      throw (new IllegalArgumentException ("the name parameter was null"));
    }

    this.userType = userType;

    initialize ();

    // store the individual parts of the path for later use    
    int pathEnd         = name.lastIndexOf (File.separator);
    int nameStart       = pathEnd + 1;
    int nameEnd         = name.lastIndexOf ('.');
    if (nameEnd < 0)
    {
      throw (new Exception ("illegal file name"));
    }
    linkName            = name.substring (nameStart, nameEnd);

    if (userType == CURRENT_USER)
    {
      currentUserLinkPath = name.substring (0, pathEnd);
    }
    else
    {
      allUsersLinkPath = name.substring (0, pathEnd);
    }
    if (loadLink (fullLinkName (userType)) != SL_OK)
    {
      throw (new Exception ("reading of the file did not succeed"));
    }

    // get all settings from the native side
    get ();
    
    linkFileName = fullLinkName (userType);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Creates an instance of <code>ShellLink</code> from an existing shell link
  * on disk.
  *
  * @param     type     The type of link, one of the following values: <br>
  *                     <ul>
  *                     <li><code>DESKTOP</code>
  *                     <li><code>PROGRAM_MENU</code>
  *                     <li><code>START_MENU</code>
  *                     <li><code>STARTUP</code>
  *                     </ul>
  * @param     userType  the type of user for the link path.
  * @param     group    The program group this link is a part of. If the
  *                     link is not part of a program group, pass an empty
  *                     string or null for this parameter.
  * @param     name     The file name of this link. Do not include a file
  *                     extension.
  *
  * @see       #CURRENT_USER
  * @see       #ALL_USERS
  *
  * @exception IllegalArgumentException if any of the call parameters are
  *                                     incorrect
  * @exception Exception if problems are encountered in initializing the
  *                      native interface
  */
 /*--------------------------------------------------------------------------*/
  public ShellLink (int    type,
                    int    userType,
                    String group, 
                    String name) throws Exception, IllegalArgumentException
  {
    if ((type < MIN_TYPE) || 
        (type > MAX_TYPE)   )
    {
      throw (new IllegalArgumentException ("the type parameter used an illegal value"));
    }
    if (name == null)
    {
      throw (new IllegalArgumentException ("the name parameter was null"));
    }

    this.userType = userType;
    
    initialize ();

    // get a settings from the native side
    get ();

    // set the variables for path, group and name
    int result = GetLinkPath (linkType);
    if (result != SL_OK)
    {
      if (result == SL_WRONG_DATA_TYPE)
      {
        throw (new Exception ("could not get link path, registry returned unexpected data type"));
      }
      else
      {
        throw (new Exception ("could not get link path"));
      }
    }

    if (group != null)
    {
      groupName = group;
    }
    linkName = name;
    
    // load the link
    if (loadLink (fullLinkName (userType)) != SL_OK)
    {
      throw (new Exception ("reading of the file did not succeed"));
    }

    linkFileName = fullLinkName (userType);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Initializes COM and gets an instance of the IShellLink interface.
  *
  * @exception Exception if problems are encountered
  */
 /*--------------------------------------------------------------------------*/
  private void initialize () throws Exception
  {
    try
    {
      Librarian.getInstance ().loadLibrary ("ShellLink", this);
    }
    catch (UnsatisfiedLinkError exception)
    {
      throw (new Exception ("could not locate native library"));
    }

    try
    {
      if (initializeCOM () != SL_OK)
      {
        throw (new Exception ("could not initialize COM"));
      }
      else
      {
        initializeSucceeded = true;
      }
    }
    catch (Throwable exception)
    {
      throw (new Exception ("unidentified problem initializing COM\n" + exception.toString ()));
    }

    int successCode = getInterface ();
    if (successCode != SL_OK)
    {
      releaseCOM ();
      initializeSucceeded = false;
      
      if (successCode == SL_OUT_OF_HANDLES)
      {
        throw (new Exception ("could not get an instance of IShellLink, no more handles available"));
      }
      else
      {
        throw (new Exception ("could not get an instance of IShellLink, failed to co-create instance"));
      }
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Destructor, releases COM and frees native resources.
  */
 /*--------------------------------------------------------------------------*/
  protected void finalize ()
  {
    releaseInterface ();

    if (initializeSucceeded)
    {
      releaseCOM ();
      initializeSucceeded = false;
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * This method is used to free the library at the end of progam execution.
  * After this call, any instance of this calss will not be usable any more!
  * <b><i><u>Note that this method does NOT return!</u></i></b>
  * <br><br>
  * <b>DO NOT CALL THIS METHOD DIRECTLY!</b><br>
  * It is used by the librarian to free the native library before physically
  * deleting it from its temporary loaction. A call to this method will
  * freeze the application irrecoverably!
  * 
  * @param    name    the name of the library to free. Use only the name and
  *                   extension but not the path.
  *
  * @see      com.izforge.izpack.NativeLibraryClient#freeLibrary
  */
 /*--------------------------------------------------------------------------*/
  public void freeLibrary (String name)
  {
    int result = releaseInterface ();

    if (initializeSucceeded)
    {
      result = releaseCOM ();
      initializeSucceeded = false;
    }

    FreeLibrary (name);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Constructs and returns the full path for the link file.
  *
  * @param     userType  the type of user for the link path.
  *
  * @return    the path to use for storing the link
  * 
  * @see       #CURRENT_USER
  * @see       #ALL_USERS
  */
 /*--------------------------------------------------------------------------*/
  private String fullLinkPath (int userType)
  {
    StringBuffer path = new StringBuffer ();
    
    // ----------------------------------------------------
    // make sure we hava a valid storage path 
    // ----------------------------------------------------
    int result = GetLinkPath (linkType); 

    // ----------------------------------------------------
    // build the complete name    
    // ----------------------------------------------------
    if (userType == CURRENT_USER)
    {
      path.append (currentUserLinkPath);
    }
    else
    {
      path.append (allUsersLinkPath);
    } 

    if ((groupName != null) && (groupName.length () > 0))
    {
      path.append (File.separator);
      path.append (groupName);
    }
  
    return (path.toString ());
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Constructs and returns the fully qualified name for the link file.
  *
  * @param     userType  the type of user for the link path.
  *
  * @return    the fully qualified file name to use for storing the link
  * 
  * @see       #CURRENT_USER
  * @see       #ALL_USERS
  */
 /*--------------------------------------------------------------------------*/
  private String fullLinkName (int userType)
  {
    StringBuffer name = new StringBuffer ();

    name.append (fullLinkPath (userType));    

    name.append (File.separator);
    name.append (linkName);
    name.append (LINK_EXTENSION);
  
    return (name.toString ());
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets all members on the native side.
  *
  * @exception Exception if any problem is encountered during this operation.
  */
 /*--------------------------------------------------------------------------*/
  private void set () throws Exception
  {
    if (SetArguments () != SL_OK)
    {
      throw (new Exception ("could not set arguments"));
    }
    if (SetDescription () != SL_OK)
    {
      throw (new Exception ("could not set description"));
    }
    if (SetHotkey () != SL_OK)
    {
      throw (new Exception ("could not set hotkey"));
    }
    if (SetIconLocation () != SL_OK)
    {
      throw (new Exception ("could not set icon location"));
    }
    if (SetPath () != SL_OK)
    {
      throw (new Exception ("could not set target path"));
    }
    if (SetShowCommand () != SL_OK)
    {
      throw (new Exception ("could not set show command"));
    }
    if (SetWorkingDirectory () != SL_OK)
    {
      throw (new Exception ("could not set working directory"));
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Gets all members from the native side.
  *
  * @exception Exception if any problem is encountered during this operation.
  */
 /*--------------------------------------------------------------------------*/
  private void get () throws Exception
  {
    if (GetArguments () != SL_OK)
    {
      throw (new Exception ("could not get arguments"));
    }
    if (GetDescription () != SL_OK)
    {
      throw (new Exception ("could not get description"));
    }
    if (GetHotkey () != SL_OK)
    {
      throw (new Exception ("could not get hotkey"));
    }
    if (GetIconLocation () != SL_OK)
    {
      throw (new Exception ("could not get icon location"));
    }

    int result = GetLinkPath (linkType);
    if (result != SL_OK)
    {
      if (result == SL_WRONG_DATA_TYPE)
      {
        throw (new Exception ("could not get link path, registry returned unexpected data type"));
      }
      else
      {
        throw (new Exception ("could not get link path"));
      }
    }
    
    if (GetPath () != SL_OK)
    {
      throw (new Exception ("could not get target path"));
    }
    if (GetShowCommand () != SL_OK)
    {
      throw (new Exception ("could not get show command"));
    }
    if (GetWorkingDirectory () != SL_OK)
    {
      throw (new Exception ("could not get working directory"));
    }
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
    this.groupName = groupName;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the command line arguments that will be passed to the target when
  * the link is activated.
  *
  * @param     arguments    the command line arguments
  *
  * @see       #getArguments
  */
 /*--------------------------------------------------------------------------*/
  public void setArguments (String arguments)
  {
    this.arguments = arguments;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the description string that is used to identify the link in a menu
  * or on the desktop.
  *
  * @param     description  the descriptiojn string
  *
  * @see       #getDescription
  */
 /*--------------------------------------------------------------------------*/
  public void setDescription (String description)
  {
    this.description = description;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the hotkey that can be used to activate the link.
  *
  * @param     hotkey   a valid Windows virtual key code. Modifiers (e.g. for
  *                     alt or shift key) are added in the upper byte. Note
  *                     that only the lower 16 bits for tis parameter are used.
  *
  * @see       #getHotkey
  */
 /*--------------------------------------------------------------------------*/
  public void setHotkey (int hotkey)
  {
    this.hotkey = hotkey;
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
  *
  * @see       #getIconLocation
  */
 /*--------------------------------------------------------------------------*/
  public void setIconLocation (String path,
                               int    index)
  {
    this.iconPath  = path;
    this.iconIndex = index;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the absolute path to the shortcut target.
  *
  * @param     path     the fully qualified file name of the target
  *
  * @see       #getTargetPath
  */
 /*--------------------------------------------------------------------------*/
  public void setTargetPath (String path)
  {
    this.targetPath = path;
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
  *                   <li><code>HIDE</code>
  *                   <li><code>NORMAL</code>
  *                   <li><code>MINIMIZED</code>
  *                   <li><code>MAXIMIZED</code>
  *                   </ul>
  *
  * @see       #getShowCommand
  */
 /*--------------------------------------------------------------------------*/
  public void setShowCommand (int show)
  {
    if ((show < MIN_SHOW) || (show > MAX_SHOW))
    {
      throw (new IllegalArgumentException ("illegal value for show command"));
    }
  
    this.showCommand = show;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the working directory for the link target.
  *
  * @param     dir    the working directory
  *
  * @see       #getWorkingDirectory
  */
 /*--------------------------------------------------------------------------*/
  public void setWorkingDirectory (String dir)
  {
    this.workingDirectory = dir;
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
    linkName = name;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the type of link
  *
  * @param     type   The type of link desired. The following values can be set:<br>
  *                   <ul>
  *                   <li>{@link #DESKTOP}
  *                   <li>{@link #PROGRAM_MENU}
  *                   <li>{@link #START_MENU}
  *                   <li>{@link #STARTUP}
  *                   </ul>
  *
  * @exception IllegalArgumentException if an an invalid type is passed
  */
 /*--------------------------------------------------------------------------*/
  public void setLinkType (int type) throws IllegalArgumentException
  {
    if ((type < MIN_TYPE) || 
        (type > MAX_TYPE)   )
    {
      throw (new IllegalArgumentException ("illegal value for type"));
    }
                
    linkType = type;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Sets the user type for link
  *
  * @param     userType  the type of user for the link.
  * 
  * @see       #CURRENT_USER
  * @see       #ALL_USERS
  *
  * @exception IllegalArgumentException if an an invalid type is passed
  */
 /*--------------------------------------------------------------------------*/
  public void setUserType (int type) throws IllegalArgumentException
  {
    if ((type == CURRENT_USER) || (type == ALL_USERS))
    {
      userType = type;
    }
    else
    {
      throw (new IllegalArgumentException (type + " is not a recognized user type"));
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the path where the links of the selected type are stroed. This
  * method is useful for discovering which program groups already exist.
  *
  * @param     userType   the type of user for the link path.
  *
  * @return    the path to the type of link set for this instance. 
  * 
  * @see       #CURRENT_USER
  * @see       #ALL_USERS
  */           
 /*--------------------------------------------------------------------------*/
  public String getLinkPath (int userType)
  {
    if (userType == CURRENT_USER)
    {
      return (currentUserLinkPath);
    }
    else
    {
      return (allUsersLinkPath);
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the command line that the link passes to the target.
  *
  * @return    the command line
  *
  * @see       #setArguments
  */
 /*--------------------------------------------------------------------------*/
  public String getArguments ()
  {
    return (arguments);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the description for the link.
  *
  * @return    the description
  *
  * @see       #setDescription
  */
 /*--------------------------------------------------------------------------*/
  public String getDescription ()
  {
    return (description);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retruns the hotkey that can be used to activate the link.
  *
  * @return    the virtual keycode for the hotkey
  *
  * @see       #setHotkey
  */
 /*--------------------------------------------------------------------------*/
  public int getHotkey ()
  {
    return (hotkey);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the path and file name of the file that contains the icon that
  * is associated with the link.
  *
  * @return    the path to the icon
  *
  * @see       #setIconLocation
  */
 /*--------------------------------------------------------------------------*/
  public String getIconLocation ()
  {
    return (iconPath);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the index of the icon with the icon or resource file
  *
  * @return    the index
  *
  * @see       #setIconLocation
  */
 /*--------------------------------------------------------------------------*/
  public int getIconIndex ()
  {
    return (iconIndex);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retruns the absolute path of the link target
  *
  * @return    the path
  *
  * @see       #setTargetPath
  */
 /*--------------------------------------------------------------------------*/
  public String getTargetPath ()
  {
    return (targetPath);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the initial condition of the target window (HIDE, NORMAL, 
  * MINIMIZED, MAXIMIZED).
  *
  * @return    the target show command
  *
  * @see       #setShowCommand
  */
 /*--------------------------------------------------------------------------*/
  public int getShowCommand ()
  {
    return (showCommand);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retruns the working deirectory for the link target.
  *
  * @return    the working directory
  *
  * @see       #setWorkingDirectory
  */
 /*--------------------------------------------------------------------------*/
  public String getWorkingDirectory ()
  {
    return (workingDirectory);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the fully qualified file name under which the link is saved on
  * disk. <b>Note:</b> this method returns valid results only if the instance
  * was created from a file on disk or after a successful save operation.
  *
  * @return    the fully qualified file name for the shell link
  */
 /*--------------------------------------------------------------------------*/
  public String getFileName ()
  {
    return (linkFileName);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the path of the directory where the link file is stored, if it
  * was necessary during the previous save operation to create the directory.
  * This method returns <code>null</code> if no save operation was carried
  * out or there was no need to create a directory during the previous save
  * operation.
  *
  * @return    the path of the directory where the link file is stored or
  *            <code>null</code> if no save operation was carried out or
  *            there was no need to create a directory during the previous
  *            save operation.
  */
 /*--------------------------------------------------------------------------*/
  public String getDirectoryCreated ()
  {
    return (linkDirectory);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the name shown in a menu or on the desktop for the link.
  *
  * @return    the name
  */
 /*--------------------------------------------------------------------------*/
  public String getLinkName ()
  {
    return (linkName);
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
    // set all values on the native side
    set ();   

    // make sure the target actually resolves
    int result = Resolve ();
    if (result != SL_OK)
    {
      throw (new Exception ("cannot resolve target"));
    }

    // make sure the directory exists
    File directory = new File (fullLinkPath (userType));
    if (!directory.exists ())
    {
      directory.mkdirs ();
      linkDirectory = directory.getPath ();
    }
    else
    {
      linkDirectory = "";
    }

    // perform the save operation
    String saveTo = fullLinkName (userType);
    result        = saveLink (saveTo);
    
    if (result == SL_NO_IPERSIST)
    {
      throw (new Exception ("could not get handle for IPesist"));
    }
    else if (result == SL_NO_SAVE)
    {
      throw (new Exception ("the save operation failed"));
    }
    
    linkFileName = saveTo;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Saves this link to any desired location.
  *
  * @param     name     the fully qualified file name for the link
  *
  * @exception IllegalArgumentException if the parameter was null
  * @exception Exception if the save operation could not be carried out
  */
 /*--------------------------------------------------------------------------*/
  public void save (String name) throws Exception
  {
    if (name == null)
    {
      throw (new IllegalArgumentException ("name was null"));
    }

    // set all values on the native side
    set ();   

    // make sure the target actually resolves
    if (Resolve () != SL_OK)
    {
      throw (new Exception ("cannot resolve target"));
    }

    // make sure the directory exists
    File directory = new File (name.substring (0, name.lastIndexOf (File.separatorChar)));
    if (!directory.exists ())
    {
      directory.mkdirs ();
      linkDirectory = directory.getPath ();
    }
    else
    {
      linkDirectory = null;
    }

    // perform the save operation
    if (saveLink (name) != SL_OK)
    {
      throw (new Exception ("the save operation failed"));
    }
    
    linkFileName = name;
  }
}
/*---------------------------------------------------------------------------*/
