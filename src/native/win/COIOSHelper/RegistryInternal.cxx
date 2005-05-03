/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               RegistryInterna<l.cxx
 *  Description :        Source file with OS related functions for registry access.
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
#include "COIOSHelper.h"

//----------------------------------------------------------------//
// A "lookup table" to  get root names for error handling.
//----------------------------------------------------------------//

static char *HKEY_LOOKUP[] =
{
	"HKEY_CLASSES_ROOT","HKEY_CURRENT_USER",
	"HKEY_LOCAL_MACHINE","HKEY_USERS",
	"HKEY_PERFORMANCE_DATA","HKEY_CURRENT_CONFIG",
	"HKEY_DYN_DATA", "UNDEFINED"
};


//----------------------------------------------------------------//
// Macro to map root identifier to string.
//----------------------------------------------------------------//
#define Root2Char( root ) HKEY_LOOKUP[ root & 0x00000007] 


/*
 * 
 * Returns TRUE if the specified key exist, else FALSE
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 */

jboolean regKeyExist(WinLibEnv *libEnv, int root, const char *key )
{
	HKEY	hKey = 0;
	bool	ok = false;
	if( RegOpenKeyEx( (HKEY) root, key, 0, KEY_READ,  &hKey) == ERROR_SUCCESS )
	{
		ok = true;
		RegCloseKey( hKey );
	}
	return( ok );
}

/*
 * 
 * Returns TRUE if the specified key is empty, else FALSE
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 */

jboolean isKeyEmpty(WinLibEnv *libEnv, int root, const char *key )
{
	HKEY	hKey = 0;
	DWORD	subkeys = 1;
	DWORD	values = 1;
	determineCounts( libEnv, root, key, &subkeys, &values );
	return( ( subkeys + values ) == 0 ? true : false );
}


/*
 * 
 * Determines subkey and value counts.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param subkeys  - [out] number of subkeys
 * @param values   - [out] number of values
 */

void determineCounts( WinLibEnv *libEnv, int root, const char *key, DWORD *subkeys, DWORD *values )
{
	HKEY	hKey = 0;
	LONG	retval;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_QUERY_VALUE ,  &hKey)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
    	if( hKey && (retval = RegQueryInfoKey( hKey, NULL, NULL, NULL, 
    		subkeys, NULL, NULL, values, NULL, NULL, NULL, NULL )) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegQueryInfoKey", Root2Char(root) , key, libEnv,retval );
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
}

/*
 * 
 * Sets the data for the key/value pair into registry value
 * If the key does not exist, an exception is thrown to the libEnv
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param value    - the registry value which should be used
 * @param type     - the type of the value
 * @param contents - the data which should be set
 * @param length   - length of buffer contents
 */
void setRegValue(WinLibEnv *libEnv, int root, const char *key, const char *value, jint type, LPBYTE contents, jint length )
{
	HKEY	hKey = 0;
	LONG	retval;
	
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_SET_VALUE ,  &hKey)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
		if( hKey && (retval = RegSetValueEx( hKey, value, 0, type, contents, length )) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A3( "functionFailed.RegSetValueEx", Root2Char(root) , key, value, libEnv,retval );
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
}


/*
 * 
 * Creates the specified key in the registry.
 * If the key exist,  an exception is thrown to the libEnv.
 * If the key does not exist, it is created.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 */
void createRegKey(WinLibEnv *libEnv, int root, const char *key )
{
	HKEY	hKey = 0;
	DWORD	disposition;
	LONG	retval;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_WRITE,  &hKey)) == ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "registry.KeyExist", Root2Char(root) , key, libEnv,retval );
		if( (retval = RegCreateKeyEx((HKEY) root, key, 0,NULL, REG_OPTION_NON_VOLATILE, 
			KEY_ALL_ACCESS, NULL, &hKey, &disposition)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegCreateKeyEx", Root2Char(root) , key, libEnv,retval );
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
}

/*
 * 
 * Deletes the specified key in the registry.
 * If the key does not exist,  an exception is thrown to the libEnv.
 * If the key exist, it is deleted.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be deleted
 */
void deleteRegKey(WinLibEnv *libEnv, int root, const char *key )
{
	HKEY	hKey = 0;
	LONG	retval;
	char	*keycopy = new char[ strlen( key ) + 1];
	char	*pos;
	char	*subPos;
	while(1)
	{
		if( ! keycopy )
			ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
    	// Determine rootkey and subkey.
    	strcpy( keycopy, key );	// Because it is const ...
    	for( pos = keycopy; *pos; ++pos )
    		if( *pos == '\\' )
    			subPos = pos;
    	// Now subPos is on the last existent backslash, splitt keycopy at this point.
    	*subPos = 0;
    	subPos++;
		if( (retval = RegOpenKeyEx( (HKEY) root, keycopy, 0, DELETE,  &hKey)) != ERROR_SUCCESS )
			// Not found or no rights for delete.
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
		if( (retval = RegDeleteKey(hKey, subPos)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegDeleteKey", Root2Char(root) , key, libEnv,retval );
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
	if( keycopy )
		delete [] keycopy;
}

/*
 * 
 * Deletes the specified value in the registry.
 * If the value does not exist,  an exception is thrown to the libEnv.
 * If the value exist, it is deleted.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key 
 * @param key      - value which should be deleted
 */
void deleteRegValue(WinLibEnv *libEnv, int root, const char *key, const char *value )
{
	HKEY	hKey = 0;
	LONG	retval;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_SET_VALUE ,  &hKey)) != ERROR_SUCCESS )
			// Not found or no rights for delete.
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
		if( (retval = RegDeleteValue(hKey, value)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A3( "functionFailed.RegDeleteValue", Root2Char(root) , key, value, libEnv,retval );
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
}

/*
 * 
 * Returns the type of data for a registry value.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param value    - the registry value which should be used
 */

jint getRegValueType( WinLibEnv *libEnv, int root, const char *key , const char *value )
{
	HKEY	hKey = 0;
	DWORD	type = 0;
	DWORD	length = 0;
	LONG	retval;
	while(1)
	{
		if( (retval =  RegOpenKeyEx( (HKEY) root, key, 0, KEY_READ,  &hKey) ) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
		if( ( retval = RegQueryValueEx( hKey, value, 0, &type, 0, &length )) != ERROR_SUCCESS &&
			retval != ERROR_MORE_DATA)
    		ERROR_BREAK_CODE_A3( "functionFailed.RegQueryValueEx", Root2Char(root) , key, value, libEnv,retval );
	}
	if( hKey )
		RegCloseKey( hKey );
	return( (jint) type );
}


/*
 * 
 * Returns the data for a registry value.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param value    - the registry value which should be used
 * @param type     - [out] type of data
 */

LPBYTE getRegValue( WinLibEnv *libEnv, int root, const char *key , const char *value, DWORD *type, DWORD *length)
{
	HKEY	hKey = 0;
	LPBYTE	contents = NULL;
	LONG	retval;
	while(1)
	{
		if( (retval =  RegOpenKeyEx( (HKEY) root, key, 0, KEY_READ,  &hKey) ) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
		if( ( retval = RegQueryValueEx( hKey, value, 0, type, 0, length )) != ERROR_SUCCESS &&
			retval != ERROR_MORE_DATA)
    		ERROR_BREAK_CODE_A3( "functionFailed.RegQueryValueEx", Root2Char(root) , key, value, libEnv,retval );
		if( ( contents = new unsigned char[ (int) *length + 1 ] ) == NULL )
			ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
		if( ( retval = RegQueryValueEx( hKey, value, 0, type, (LPBYTE) contents, length )) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A3( "functionFailed.RegQueryValueEx", Root2Char(root) , key, value, libEnv,retval );
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
	return( contents );
}


/*
 * 
 * Returns the name of the subkey with the given id.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param keyId    - id of the key for which the name should be returned
 */
char *getSubkeyName( WinLibEnv *libEnv, int root, const char *key , int keyId )
{
	HKEY	hKey = 0;
	LONG	retval;
	DWORD	length = 0;
	char	*name = NULL;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_ENUMERATE_SUB_KEYS,  &hKey)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
    	if( hKey && (retval = RegEnumKeyEx( hKey, keyId, NULL, &length, 
    		NULL, NULL, NULL, NULL )) != ERROR_SUCCESS && retval != ERROR_MORE_DATA)
    			ERROR_BREAK_CODE_A2( "functionFailed.RegEnumKeyEx", Root2Char(root) , key, libEnv,retval );
    	length++;
    	if(( name = new char[ length]) == NULL )
    		ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
    	if( hKey && (retval = RegEnumKeyEx( hKey, keyId, name, &length, 
    		NULL, NULL, NULL, NULL )) != ERROR_SUCCESS )
    			ERROR_BREAK_CODE_A2( "functionFailed.RegEnumKeyEx", Root2Char(root) , key, libEnv,retval );
    	
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
	return( name );
}

/*
 * 
 * Returns the number of names of all values of the given key and the names self.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param names    - [out] the founded names
 */
int getSubkeyNames( WinLibEnv *libEnv, int root, const char *key , char ***names )
{
	HKEY	hKey = 0;
	LONG	retval;
	DWORD	length = 0;
	DWORD	subkeys = 0;
	DWORD	values = 0;
	DWORD 	maxLength = 0;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_QUERY_VALUE |  KEY_ENUMERATE_SUB_KEYS,  &hKey)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
    	if( hKey && (retval = RegQueryInfoKey( hKey, NULL, NULL, NULL, 
    		&subkeys, &maxLength, NULL, &values, NULL, NULL, NULL, NULL )) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegQueryInfoKey", Root2Char(root) , key, libEnv,retval );
    	maxLength++;
		if (((*names)= new char *[subkeys])==NULL)
			ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
		for( DWORD i = 0; i < subkeys; ++i )
		{
			if (((*names)[i]= new char [maxLength])==NULL)
				ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
			length = maxLength;
			if( hKey && (retval = RegEnumKeyEx( hKey, i, (*names)[i], &length, 
				NULL, NULL, NULL, NULL )) != ERROR_SUCCESS )
    				ERROR_BREAK_CODE_A2( "functionFailed.RegEnumKeyEx", Root2Char(root) , key, libEnv,retval );
		}
    	if( ! libEnv->good() )
    		break;
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
	return( subkeys );
}

/*
 * 
 * Returns the name of the value with the given id.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param valueId  - id of the value for which the name should be returned
 */
char *getValueName( WinLibEnv *libEnv, int root, const char *key , int valueId )
{
	HKEY	hKey = 0;
	LONG	retval;
	DWORD	length = 0;
	char	*name = NULL;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_ENUMERATE_SUB_KEYS,  &hKey)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
    	if( hKey && (retval = RegEnumValue( hKey, valueId, NULL, &length, 
    		NULL, NULL, NULL, NULL )) != ERROR_SUCCESS && retval != ERROR_MORE_DATA)
    			ERROR_BREAK_CODE_A2( "functionFailed.RegEnumValue", Root2Char(root) , key, libEnv,retval );
    	length++;
    	if(( name = new char[ length]) == NULL )
    		ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");

    	if( hKey && (retval = RegEnumValue( hKey, valueId, name, &length, 
    		NULL, NULL, NULL, NULL )) != ERROR_SUCCESS )
    			ERROR_BREAK_CODE( "functionFailed.RegEnumValue", libEnv,retval );
    				ERROR_BREAK_CODE_A2( "functionFailed.RegEnumValue", Root2Char(root) , key, libEnv,retval );
    	
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
	return( name );
}

/*
 * 
 * Returns the number of names of all values of the given key and the names self.
 * @param libEnv   - environment object for exception handling
 * @param root	   - key for the registry root e.g. HKEY_LOCAL_MACHINE
 * @param key      - the registry key which should be used
 * @param names    - [out] the founded names
 */
int getValueNames( WinLibEnv *libEnv, int root, const char *key , char ***names  )
{
	HKEY	hKey = 0;
	LONG	retval;
	DWORD	length = 0;
	DWORD	subkeys = 0;
	DWORD	values = 0;
	DWORD 	maxLength = 0;
	while(1)
	{
		if( (retval = RegOpenKeyEx( (HKEY) root, key, 0, KEY_QUERY_VALUE |  KEY_ENUMERATE_SUB_KEYS,  &hKey)) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegOpenKeyEx", Root2Char(root) , key, libEnv,retval );
    	if( hKey && (retval = RegQueryInfoKey( hKey, NULL, NULL, NULL, 
    		&subkeys, NULL, NULL, &values, &maxLength, NULL, NULL, NULL )) != ERROR_SUCCESS )
    		ERROR_BREAK_CODE_A2( "functionFailed.RegQueryInfoKey", Root2Char(root) , key, libEnv,retval );
    	maxLength++;
		if (((*names)= new char *[values])==NULL)
			ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
		for( DWORD i = 0; i < values; ++i )
		{
			if (((*names)[i]= new char [maxLength])==NULL)
				ERROR_BREAK_VAR( "", libEnv , "OutOfMemoryError");
			length = maxLength;
			if( hKey && (retval = RegEnumValue( hKey, i, (*names)[i], &length, 
				NULL, NULL, NULL, NULL )) != ERROR_SUCCESS )
    				ERROR_BREAK_CODE_A2( "functionFailed.RegEnumValue", Root2Char(root) , key, libEnv,retval );
		}
    	if( ! libEnv->good() )
    		break;
		break;
	}
	if( hKey )
		RegCloseKey( hKey );
	return( values );
}


