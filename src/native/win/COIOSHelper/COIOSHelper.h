/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               COIOSHelper.h
 *  Description :        Main header file for the COIOSHelper stuff.
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

#include <jni.h>
#include "WinLibEnv.h"

// Defines
// shortcut for set a error value to WinLibEnv and break.

#define ERROR_BREAK( err, acceptor ) { acceptor->setError(err); break; }
// shortcut for set a error value to WinLibEnv with verifying the system error and break.
#define ERROR_BREAK_SYS( err, acceptor ) { acceptor->setErrorWithOS(err); break; }
#define ERROR_BREAK_CODE( err, acceptor, code ) { acceptor->setError(err, code); break; }
#define ERROR_BREAK_CODE_A1( err, arg1 , acceptor, code) \
	{ acceptor->setError(err, code); acceptor->addArg(arg1); break; }
#define ERROR_BREAK_CODE_A2( err, arg1, arg2, acceptor, code ) \
	{ acceptor->setError(err, code); acceptor->addArgs(arg1, arg2); break; }
#define ERROR_BREAK_CODE_A3( err, arg1, arg2, arg3, acceptor, code ) \
	{ acceptor->setError(err, code); acceptor->addArgs(arg1, arg2, arg3); break; }

#define ERROR_BREAK_VAR( err, acceptor, exName ) { acceptor->setError(err, exName); break; }


#define LOOK_OS()	if( ! _isNT4orHigher() ) { lastInternError = 8; return( -8 ); }
#define STRING_INIT "not found"

#define	MAX_ERROR	50
#ifndef MAX_NAME_LEN
#define MAX_NAME_LEN	256
#endif


// in RegistryInternal.c

extern jboolean regKeyExist(WinLibEnv *libEnv, int root, const char *key );
extern void setRegValue(WinLibEnv *libEnv, int root, const char *key, 
	const char *value, jint type, LPBYTE contents, jint length );
extern void createRegKey(WinLibEnv *libEnv, int root, const char *key );
extern jint getRegValueType( WinLibEnv *libEnv, int root, const char *key , const char *value );
extern LPBYTE getRegValue( WinLibEnv *libEnv, int root, const char *key , const char *value, DWORD *type, DWORD *length);
extern void deleteRegValue(WinLibEnv *libEnv, int root, const char *key, const char *value );
extern void deleteRegKey(WinLibEnv *libEnv, int root, const char *key );
extern jboolean isKeyEmpty(WinLibEnv *libEnv, int root, const char *key );
extern void determineCounts( WinLibEnv *libEnv, int root, const char *key, DWORD *subkeys, DWORD *values );
extern char *getSubkeyName( WinLibEnv *libEnv, int root, const char *key , int valueId );
extern char *getValueName( WinLibEnv *libEnv, int root, const char *key , int keyId );
extern int getValueNames( WinLibEnv *libEnv, int root, const char *key , char ***names  );
extern int getSubkeyNames( WinLibEnv *libEnv, int root, const char *key , char ***names  );








