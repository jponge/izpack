/*
 * IzPack Version 3.0.0 pre4 (build 2002.06.15)
 * Copyright (C) 2002 by Elmar Grom
 *
 * File :               ShellLink.c
 * Description :        Represents a MS-Windows Shell Link (shortcut)
 *                      This is the native counterpart to ShellLink.java
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

#include "com_izforge_izpack_util_os_ShellLink.h"
#include <windows.h>
#include <winerror.h>
#include <objbase.h>
#include <basetyps.h>
#include <shlobj.h>
#include <objidl.h>

// --------------------------------------------------------------------------
// Gound rules used for the implementation of this native interface
// of ShellLink:
//
// 1) all functions return an integer success code
// 2) positive success codes report that everything went ok
// 3) negative success codes report some type of problem
// 4) a success code of 0 does not exist
// 5) 'get' functions deposit their results in the corresponding member
//    variables on the Java side.
// 6) "set' functions retrieve their input from the corresponding member
//    variables on the Java side.
// 7) functions other than 'get' and 'set' recive their input -if any- in
//    the form of arguments.
// 8) functions that are exposed on the Java side (public, protectd)
//    follow the Java naming conventions, in that they begin with a lower
//    case character.
// 9) all functions that have a Java wrapper by the same name follow the
//    Windows naming convention, in that they start with an upper case
//    letter. This avoids having to invent new method names for Java and
//    it allows to keep a clean naming convention on the Java side.
// ============================================================================
//
// I M P O R T A N T !
// -------------------
//
// This interface communicates with the OS via COM. In order for things to
// work properly, it is necessary to observe the following pattern of
// operation and to observe the order of execution (i.e. do not call
// getInterface() before calling initializeCOM()).
//
// 1) call initializeCOM() - It's best to do this in the constructor
// 2) call getInterface() - It's best to do this in the constructor as well
//
// 3) do your stuff (load, save, get, set ...)
//
// 4) call releaseInterface() before terminating the application, best done
//    in the finalizer
// 5) call releaseCOM() before terminating the application, best done
//    in the finalizer. Do NOT call this if the call to initializeCOM() did
//    not succeed, otherwise you'll mess things up pretty badly!
// ============================================================================
// Variables that must be declared on the Java side:
//
// private int     nativeHandle;
//
// private String  linkPath;
// private String  linkName;
//
// private String  arguments;
// private String  description;
// private String  iconPath;
// private String  targetPath;
// private String  workingDirectory;
//
// private int     hotkey;
// private int     iconIndex;
// private int     showCommand;
// private int     linkType;
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
// Macro Definitions
// --------------------------------------------------------------------------
#define   ACCESS                0         // index for retrieving the registry access key
#define   MIN_KEY               0         // for verifying that an index received in the form of a call parameter is actually leagal
#define   MAX_KEY               5         // for verifying that an index received in the form of a call parameter is actually leagal

// --------------------------------------------------------------------------
// Prototypes
// --------------------------------------------------------------------------
// Hey C crowd, don't freak out! ShellLink.h is auto generated and I'd like
// to have everything close by in this package. Besides, these are only used
// in this file...
// --------------------------------------------------------------------------
int  getNewHandle ();
void freeLinks ();

// --------------------------------------------------------------------------
// Constant Definitions
// --------------------------------------------------------------------------

// the registry keys to get access to the various shortcut locations for the current user
const char CURRENT_USER_KEY [5][100] =
{
  "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", // this is where the details are stored in the registry
  "Desktop",                                                               // this is where desktop shortcuts go
  "Programs",                                                              // this is where items of the progams menu go
  "Start Menu",                                                            // this is right in the start menu
  "Startup"                                                                // this is where stuff goes that should be executed on OS launch
};

// the registry keys to get access to the various shortcut locations for all users
const char ALL_USER_KEY [5][100] =
{
  "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", // this is where the details are stored in the registry
  "Common Desktop",                                                        // this is where desktop shortcuts go
  "Common Programs",                                                       // this is where items of the progams menu go
  "Common Start Menu",                                                     // this is right in the start menu
  "Common Startup"                                                         // this is where stuff goes that should be executed on OS launch
};

// Success Codes
const jint  SL_OK                 =  1;     // returned if a call was successful
const jint  SL_ERROR              = -1;     // unspecific return if a call was not successful
const jint  SL_INITIALIZED        = -2;     // return value from initialization functions if already initialized
const jint  SL_NOT_INITIALIZED    = -3;     // return value from uninitialization functions if never initialized
const jint  SL_OUT_OF_HANDLES     = -4;     // there are no more interface handles available
const jint  SL_NO_IPERSIST        = -5;     // could not get a handle for the IPersist interface
const jint  SL_NO_SAVE            = -6;     // could not save the link
const jint  SL_WRONG_DATA_TYPE    = -7;     // an unexpected data type has been passed or received
const jint  SL_CAN_NOT_READ_PATH  = -8;     // was not able to read the link path from the Windows Registry

const int   MAX_TEXT_LENGTH       =  1000;  // buffer size for text buffers
const int   ALLOC_INCREMENT       =  10;    // allocation increment for allocation of additional storage space for link references

// --------------------------------------------------------------------------
// Variable Declarations
// --------------------------------------------------------------------------
int           referenceCount      = 0;

// --------------------------------------------------------
// DLLs are not objects!
// --------------------
// What this means is that if multiple references are made
// to the same DLL in the same program space, no new
// storage is allocated for the variables in the DLL.
// For all practical purposes, variables in DLLs are equal
// to static variables in classes - all instances share
// the same storage space.
// ========================================================
// Since this code is designed to operate in conjunction
// with a Java class, there is a possibility for multiple
// instances of the class to acces this code 'simultaniously'.
// As a result, one instance could be modifying the link
// data for another instance. To avoid this, I am
// artificially creating multiple DLL 'instances' by
// providing a storage array for pointers to multiple
// instances of IShellLink. Each Java instance must
// access its IShellLink through a handle (the array
// index where its corresponding pointer is stored).
// ========================================================
// For details on how this works see:
// - getNewHandle()
// - freeLinks()
// --------------------------------------------------------
int           linkCapacity        = 0;      // indicates the current capacity for storing pointers
IShellLink**  p_shellLink         = NULL;   // pointers to the IShellLink interface

// --------------------------------------------------------------------------
// Gain COM access
//
// returns: SL_OK       if the initialization was successfull
//          SL_ERROR    otherwise
//
// I M P O R T A N T !!
// --------------------
//
// 1) This method must be called first!
// 2) The application must call releaseCOM() just before terminating but
//    only if a result of SL_OK was retruned form this function!
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_initializeCOM (JNIEnv  *env,
                                                                                jobject  obj)
{
  HRESULT hres;

  if (referenceCount > 0)
  {
    referenceCount++;
    return (SL_OK);
  }

  hres = CoInitializeEx (NULL, COINIT_APARTMENTTHREADED);

  if (SUCCEEDED (hres))
  {
    referenceCount++;
    return (SL_OK);
  }

  return (SL_ERROR);
}

// --------------------------------------------------------------------------
// Releases COM and frees associated resources. This function should be
// called as the very last operation before the application terminates.
// Call this function only if a prior call to initializeCOM() returned SL_OK.
//
// returns: SL_OK               under normal circumstances
//          SL_NOT_INITIALIZED  if the reference count indicates that no
//                              current users exist.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_releaseCOM (JNIEnv  *env,
                                                                             jobject  obj)
{
  referenceCount--;

  if (referenceCount == 0)
  {
    CoUninitialize ();
    // This is the end of things, so this is a good time to
    // free the storage for the IShellLink pointers.
    freeLinks ();
    return (SL_OK);
  }
  else if (referenceCount < 0)
  {
    referenceCount++;
    return (SL_NOT_INITIALIZED);
  }
  else
  {
    return (SL_OK);
  }
}

// --------------------------------------------------------------------------
// This function gains access to the ISchellLink interface. It must be
// called before any other calls can be made but after initializeCOM().
//
// I M P O R T A N T !!
// --------------------
//
// releaseInterface() must be called before terminating the application!
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_getInterface (JNIEnv  *env,
                                                                               jobject  obj)
{
  HRESULT hres;
  int     handle;

  // Get a handle
  handle = getNewHandle ();
  if (handle < 0)
  {
    return (SL_OUT_OF_HANDLES);
  }

  // Store the handle on the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");

  (env)->SetIntField (obj, handleID, (jint)handle);

  /*
   * Note: CoCreateInstance() supports only the creation of a single instance.
   * Need to find out how to use CoGetClassObject() to create multiple instances.
   * It should be possible to have multiple instances available, got to make this work!
   */

  // Get a pointer to the IShellLink interface
  hres = CoCreateInstance (CLSID_ShellLink,
                           NULL,
                           CLSCTX_INPROC_SERVER,
                           IID_IShellLink,
                           (void **)&p_shellLink [handle]);

  // Do error handling
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }

  return (SL_ERROR);
}

// --------------------------------------------------------------------------
// This function returns a new handle to be used for the next client. If no
// more handles are available -1 is returnd.
// --------------------------------------------------------------------------
int getNewHandle ()
{
  IShellLink* pointer;

  // loop through the array to find an unoccupied location
  int i;
  for (i = 0; i < linkCapacity; i++)
  {
    pointer = p_shellLink [i];
    // if an unoccupied location is found return the index
    if (pointer == NULL)
    {
      return (i);
    }
  }

  // if we get here, all locations are in use and we need to
  // create more storage space to satisfy the request
  int   newSize     = sizeof (IShellLink*) * (linkCapacity + ALLOC_INCREMENT);
  void* tempPointer = realloc ((void *)p_shellLink, newSize);

  if (tempPointer != NULL)
  {
    p_shellLink  = (IShellLink**)tempPointer;
    linkCapacity = linkCapacity + ALLOC_INCREMENT;

    for (int k = i; k < linkCapacity; k++)
    {
      p_shellLink [k] = NULL;
    }
    return (i);
  }
  else
  {
    return (-1);
  }
}

// --------------------------------------------------------------------------
// This function frees the storage that was allocated for the storage of
// pointers to IShellLink interfaces. It also cleans up any interfaces that
// have not yet been reliquished (clients left a mess -> bad boy!).
// --------------------------------------------------------------------------
void freeLinks ()
{
  if (p_shellLink != NULL)
  {
    // loop through the array and release any interfaces that
    // have not been freed yet
    IShellLink* pointer;
    for (int i = 0; i < linkCapacity; i++)
    {
      pointer = p_shellLink [i];
      // if an unoccupied location is found, return the index
      if (pointer != NULL)
      {
        pointer->Release ();
        p_shellLink [i] = NULL;
      }
    }

    // free the pointer storage itself
    linkCapacity = 0;
    free (p_shellLink);
  }
}

// --------------------------------------------------------------------------
// This function frees this dll, allowing the operating system to remove
// the code from memory and releasing the reference to the dll on disk. 
// After this call this dll can not be used any more.
//
// THIS FUNCTION DOES NOT RETURN !!!
// --------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_izforge_izpack_util_os_ShellLink_FreeLibrary (JNIEnv *env, 
                                                                              jobject obj,
                                                                              jstring name)
{
  // convert the name from Java string type
  const char *libraryName = (env)->GetStringUTFChars (name, 0);

  // get a module handle 
  HMODULE handle = GetModuleHandle (libraryName);

  // release the string object
  (env)->ReleaseStringUTFChars (name, libraryName);
  
  // now we are rady to free the library
  FreeLibraryAndExitThread (handle, 0);
}

// --------------------------------------------------------------------------
// Releases the interface
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_releaseInterface (JNIEnv  *env,
                                                                                   jobject  obj)
{
  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  if (handle < 0)
  {
    return (SL_OK);
  }
  
  if (p_shellLink [handle] == NULL)
  {
    return (SL_NOT_INITIALIZED);
  }

  p_shellLink [handle]->Release ();
  p_shellLink [handle] = NULL;
  (env)->SetIntField (obj, handleID, -1);
  return (SL_OK);
}

// --------------------------------------------------------------------------
// Retrieves the command-line arguments associated with a shell link object
//
// Result is deposited in 'arguments'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetArguments (JNIEnv  *env,
                                                                               jobject  obj)
{
  char    arguments [MAX_TEXT_LENGTH];
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetArguments (arguments,
                                             MAX_TEXT_LENGTH);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  argumentsID = (env)->GetFieldID      (cls, "arguments", "Ljava/lang/String;");
    jstring   j_arguments = (env)->NewStringUTF    (arguments);

    (env)->SetObjectField (obj, argumentsID, j_arguments);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the description string for a shell link object.
//
// Result is deposited in 'description'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetDescription (JNIEnv  *env,
                                                                                 jobject  obj)
{
  char description [MAX_TEXT_LENGTH];
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetDescription (description,
                                               MAX_TEXT_LENGTH);

  if (SUCCEEDED (hres))
  {
    jfieldID  descriptionID = (env)->GetFieldID      (cls, "description", "Ljava/lang/String;");
    jstring   j_description = (env)->NewStringUTF    (description); // convert to Java String type

    (env)->SetObjectField (obj, descriptionID, j_description);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the hot key for a shell link object.
//
// Result is deposited in 'hotkey'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetHotkey	(JNIEnv  *env,
                                                                             jobject  obj)
{
  WORD    hotkey;
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetHotkey (&hotkey);

  if (SUCCEEDED (hres))
  {
    jfieldID  hotkeyID = (env)->GetFieldID      (cls, "hotkey", "I");

    (env)->SetIntField (obj, hotkeyID, (jint)hotkey);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the location (path and index) of the icon for a shell link object.
//
// The path is deposited in 'iconPath'
// The index is deposited in 'iconIndex'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetIconLocation	(JNIEnv  *env,
                                                                                   jobject  obj)
{
  HRESULT hres;
  char    iconPath [MAX_PATH];
  int     iconIndex;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetIconLocation (iconPath,
                                                MAX_PATH,
                                                &iconIndex);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  pathID      = (env)->GetFieldID      (cls, "iconPath", "Ljava/lang/String;");
    jfieldID  indexID     = (env)->GetFieldID      (cls, "iconIndex", "I");
    jstring   j_iconPath  = (env)->NewStringUTF    (iconPath);

    (env)->SetObjectField  (obj, pathID, j_iconPath);
    (env)->SetIntField     (obj, indexID, (jint)iconIndex);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the path and filename of a shell link object.
//
// Result is deposited in 'targetPath'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetPath (JNIEnv  *env,
                                                                          jobject  obj)
{
  WIN32_FIND_DATA findData;
  char            targetPath [MAX_PATH];
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetPath (targetPath,
                                        MAX_PATH,
                                        &findData,
                                        SLGP_UNCPRIORITY);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  pathID        = (env)->GetFieldID      (cls, "targetPath", "Ljava/lang/String;");
    jstring   j_targetPath  = (env)->NewStringUTF    (targetPath);

    (env)->SetObjectField (obj, pathID, j_targetPath);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the show (SW_) command for a shell link object.
//
// Result is deposited in 'showCommand'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetShowCommand (JNIEnv  *env,
                                                                                 jobject  obj)
{
  HRESULT   hres;
  int       showCommand;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetShowCmd (&showCommand);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  commandID = (env)->GetFieldID      (cls, "showCommand", "I");

    (env)->SetIntField (obj, commandID, (jint)showCommand);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the name of the working directory for a shell link object.
//
// Result is deposited in 'workingDirectory'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetWorkingDirectory	(JNIEnv  *env,
                                                                                       jobject  obj)
{
  HRESULT hres;
  char workingDirectory [MAX_PATH];

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetWorkingDirectory (workingDirectory,
                                                    MAX_PATH);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  directoryID         = (env)->GetFieldID      (cls, "workingDirectory", "Ljava/lang/String;");
    jstring   j_workingDirectory  = (env)->NewStringUTF    (workingDirectory);

    (env)->SetObjectField (obj, directoryID, j_workingDirectory);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Resolves a shell link by searching for the shell link object and
// updating the shell link path and its list of identifiers (if necessary).
//
// I recommend to call this function before saving the shortcut. This will
// ensure that the link is working and all the identifiers are updated, so
// that the link will actually work when used later on. If for some reason
// the link can not be resolved, at least the creating application knows
// about this.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_Resolve	(JNIEnv  *env,
                                                                           jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->Resolve (NULL,
                                        SLR_NO_UI | SLR_UPDATE);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the command-line arguments associated with a shell link object.
//
// Input is taken from 'arguments'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetArguments (JNIEnv  *env,
                                                                               jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    argumentsID = (env)->GetFieldID               (cls, "arguments", "Ljava/lang/String;");
  jstring     j_arguments = (jstring)(env)->GetObjectField  (obj, argumentsID);
  const char *arguments   = (env)->GetStringUTFChars        (j_arguments, 0);

  hres = p_shellLink [handle]->SetArguments (arguments);

  (env)->ReleaseStringUTFChars (j_arguments, arguments);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the description string for a shell link object.
//
// Input is taken from 'description'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetDescription (JNIEnv  *env,
                                                                                 jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    descriptionID = (env)->GetFieldID               (cls, "description", "Ljava/lang/String;");
  jstring     j_description = (jstring)(env)->GetObjectField  (obj, descriptionID);
  const char *description   = (env)->GetStringUTFChars        (j_description, 0);

  hres = p_shellLink [handle]->SetDescription (description);

  (env)->ReleaseStringUTFChars (j_description, description);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the hot key for a shell link object.
//
// Input is taken from 'hotkey'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetHotkey	(JNIEnv  *env,
                                                                             jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    hotkeyID  = (env)->GetFieldID      (cls, "hotkey", "I");
  jint        hotkey    = (env)->GetIntField     (obj, hotkeyID);

  hres = p_shellLink [handle]->SetHotkey (hotkey);
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the location (path and index) of the icon for a shell link object.
//
// The path is taken from 'iconPath'
// The index is taken from 'iconIndex'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetIconLocation	(JNIEnv  *env,
                                                                                   jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    pathID        = (env)->GetFieldID               (cls, "iconPath", "Ljava/lang/String;");
  jstring     j_iconPath    = (jstring)(env)->GetObjectField  (obj, pathID);
  const char *iconPath      = (env)->GetStringUTFChars        (j_iconPath, 0);

  jfieldID    indexID       = (env)->GetFieldID               (cls, "iconIndex", "I");
  jint        iconIndex     = (env)->GetIntField              (obj, indexID);

  hres = p_shellLink [handle]->SetIconLocation (iconPath,
                                                iconIndex);

  (env)->ReleaseStringUTFChars (j_iconPath, iconPath);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the path and filename of a shell link object.
//
// Input is taken from 'targetPath'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetPath	(JNIEnv  *env,
                                                                           jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    pathID        = (env)->GetFieldID               (cls, "targetPath", "Ljava/lang/String;");
  jstring     j_targetPath  = (jstring)(env)->GetObjectField  (obj, pathID);
  const char *targetPath    = (env)->GetStringUTFChars        (j_targetPath, 0);

  hres = p_shellLink [handle]->SetPath (targetPath);

  (env)->ReleaseStringUTFChars (j_targetPath, targetPath);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the show (SW_) command for a shell link object.
//
// Input is taken from 'showCommand'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetShowCommand (JNIEnv  *env,
                                                                                 jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    commandID   = (env)->GetFieldID      (cls, "showCommand", "I");
  jint        showCommand = (env)->GetIntField     (obj, commandID);

  hres = p_shellLink [handle]->SetShowCmd (showCommand);
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the name of the working directory for a shell link object.
//
// Input is taken from 'workingDirectory'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetWorkingDirectory	(JNIEnv  *env,
                                                                                       jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    pathID              = (env)->GetFieldID               (cls, "workingDirectory", "Ljava/lang/String;");
  jstring     j_workingDirectory  = (jstring)(env)->GetObjectField  (obj, pathID);
  const char *workingDirectory    = (env)->GetStringUTFChars        (j_workingDirectory, 0);

  hres = p_shellLink [handle]->SetWorkingDirectory (workingDirectory);

  (env)->ReleaseStringUTFChars (j_workingDirectory, workingDirectory);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// This function saves the shell link.
//
// name - the fully qualified path for saving the shortcut.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_saveLink (JNIEnv  *env,
                                                                           jobject  obj,
                                                                           jstring  name)
{
  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ----------------------------------------------------
  // Query IShellLink for the IPersistFile interface for
  // saving the shell link in persistent storage.
  // ----------------------------------------------------
  IPersistFile* p_persistFile;
  HRESULT       hres = p_shellLink [handle]->QueryInterface (IID_IPersistFile,
                                                             (void **)&p_persistFile);

  if (!SUCCEEDED (hres))
  {
    return (SL_NO_IPERSIST);
  }

  // ----------------------------------------------------
  // convert from Java string type
  // ----------------------------------------------------
  const unsigned short *pathName = (env)->GetStringChars (name, 0);
  
  // ----------------------------------------------------
  // Save the link
  // ----------------------------------------------------
  hres = p_persistFile->Save   ((wchar_t*)pathName, FALSE);
  p_persistFile->SaveCompleted ((wchar_t*)pathName);
  
  // ----------------------------------------------------
  // Release the pointer to IPersistFile
  // and the string object
  // ----------------------------------------------------
  p_persistFile->Release ();
  (env)->ReleaseStringChars (name, pathName);

  // ------------------------------------------------------
  // return success code
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_NO_SAVE);
  }
}

// --------------------------------------------------------------------------
// This function loads a shell link.
//
// name - the fully qualified path for loading the shortcut.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_loadLink (JNIEnv  *env,
                                                                           jobject  obj,
                                                                           jstring  name)
{
  HRESULT     hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ----------------------------------------------------
  // Query IShellLink for the IPersistFile interface for
  // saving the shell link in persistent storage.
  // ----------------------------------------------------
  IPersistFile* p_persistFile;
  hres = p_shellLink [handle]->QueryInterface (IID_IPersistFile,
                                               (void **)&p_persistFile);

  if (SUCCEEDED (hres))
  {
    // convert from Java string type
    const unsigned short *pathName = (env)->GetStringChars (name, 0);

    // --------------------------------------------------
    // Load the link
    // --------------------------------------------------
    hres = p_persistFile->Load ((wchar_t *)pathName,
                                STGM_DIRECT    |
                                STGM_READWRITE |
                                STGM_SHARE_EXCLUSIVE);

    // --------------------------------------------------
    // Release the pointer to IPersistFile
    // --------------------------------------------------
    p_persistFile->Release ();
    (env)->ReleaseStringChars (name, pathName);
  }

  // ------------------------------------------------------
  // return success code
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// This function retrieves the location of the folders that hold shortcuts.
// The information comes from the Windows registry.
//
// target   - where the path should point. The following are legal values
//            to use
//
//            1 - path for shortcuts that show on the desktop
//            2 - path for shortcuts that show in the Programs menu
//            3 - path for shortcuts that show in the start menu
//            4 - path to the Startup group. These shortcuts are executed
//                at OS launch time
//
//            Note: all other values cause an empty string to be returned
//
// Program groups (sub-menus) in the programs and start menus can be created
// by creating a new folder at the indicated location and placing the links
// in that folder. These folders can be nested to any depth with each level
// creating an additional menu level.
//
// Results are deposited in 'currentUserLinkPath' and 'allUsersLinkPath' 
// respectively
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetLinkPath (JNIEnv  *env,
                                                                              jobject  obj,
                                                                              jint     target)
{
  ULONG   ul_size = MAX_PATH;       // buffer size
  char    currentPath [MAX_PATH];   // the current user path we are looking for 
  char    allPath [MAX_PATH];       // the all users path we are looking for 
  HKEY    h_key;                    // handle for the open key
  HKEY    h_allKey;                 // handle for the open key
  DWORD   lp_type;                  // data type, this is expected to be REG_SZ (null terminated string)
  
  if ((target > MIN_KEY) && (target < MAX_KEY))
  {
    // get the path for the current user
    LONG  successCode = RegOpenKeyEx (HKEY_CURRENT_USER,
                                      CURRENT_USER_KEY [ACCESS],
                                      0,
                                      KEY_QUERY_VALUE,
                                      &h_key);

    if (SUCCEEDED (successCode))
    {
      RegQueryValueEx (h_key,
                       CURRENT_USER_KEY [target],
                       NULL,
                       &lp_type,
                       (unsigned char *)&currentPath,
                       &ul_size);

      RegCloseKey     (h_key);
      
      // make sure we actually received a null terminated string as expected
      if (!(lp_type == REG_SZ))
      {
        return (SL_WRONG_DATA_TYPE);
      }
    }
    else
    {
      return (SL_CAN_NOT_READ_PATH);
    }
    
    // get the path for all users
    successCode = RegOpenKeyEx (HKEY_LOCAL_MACHINE,
                                ALL_USER_KEY [ACCESS],
                                0,
                                KEY_QUERY_VALUE,
                                &h_allKey);

    if (SUCCEEDED (successCode))
    {
      ul_size = MAX_PATH;
      RegQueryValueEx (h_allKey,
                       ALL_USER_KEY [target],
                       NULL,
                       &lp_type,
                       (unsigned char *)&allPath,
                       &ul_size);

      RegCloseKey     (h_allKey);
      
      // make sure we actually received a null terminated string as expected
      if (!(lp_type == REG_SZ))
      {
          allPath[0] = 0;
      }
    }
    else
    {
      allPath[0] = 0;
    }
    
    // ------------------------------------------------------
    // set the member variables
    // ------------------------------------------------------
    jclass    cls    = (env)->GetObjectClass (obj);
    jfieldID  pathID = (env)->GetFieldID     (cls, "currentUserLinkPath", "Ljava/lang/String;");
    jstring   j_path = (env)->NewStringUTF   (currentPath);

    (env)->SetObjectField (obj, pathID, j_path);

    pathID = (env)->GetFieldID     (cls, "allUsersLinkPath", "Ljava/lang/String;");
    j_path = (env)->NewStringUTF   (allPath);

    (env)->SetObjectField (obj, pathID, j_path);

    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}
// --------------------------------------------------------------------------
