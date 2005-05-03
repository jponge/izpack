/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               COIOSHelper.cxx
 *  Description :        Main source file for the COIOSHelper stuff.
 *                       
 *  Author's email :     bartzkau@users.berlios.de
 *  Website :            http://www.izforge.com
 * 
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


#include <windows.h>
#include <jni.h>
#include "com_coi_tools_os_izpack_COIOSHelper.h"
#include "WinLibEnv.h"


#ifndef RC_INVOKED    // start of source code

//----------------------------------------------------------------------
// This is the main source of COI OS helper.
// The package contains the C++ side of Java classes; the files has the
// names like the classes with Impl at end. They contains only the
// native java methods with the Java specific handling. The real work
// will be done in files, which ends with "Internal".
// Most of the internals are simple functions, not classes. 
// This way was choosen because most functions should call functions of
// the OS to do there work. To wrap it into classes will produce much
// overhead. Some functions will be called only once, but other
// functions can be called much times.
// 
// For exception handling and so on, every internal function is called
// with an object labelled "WinLibEnv". It is short and will be used
// only if an error occurs.
//
//----------------------------------------------------------------------

// --------------------------------------------------------------------------
// This function frees this dll, allowing the operating system to remove
// the code from memory and releasing the reference to the dll on disk. 
// After this call this dll can not be used any more.
//
// THIS FUNCTION DOES NOT RETURN !!!
// --------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_coi_tools_os_izpack_COIOSHelper_FreeLibrary
	(JNIEnv *env, jobject obj, jstring name)
{
	// convert the name from Java string type
	const char *libraryName = env->GetStringUTFChars (name, 0);

	// get a module handle 
	HMODULE handle = GetModuleHandle (libraryName);

	// release the string object
	env->ReleaseStringUTFChars (name, libraryName);
	
	// destroy the acl factory
	// now we are rady to free the library
	FreeLibraryAndExitThread (handle, 0);
}







	

#else // RC_INVOKED, end of source code, start of resources
// resource definition go here

#endif // RC_INVOKED

		
