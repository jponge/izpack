/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               RegistryImpl.cxx
 *  Description :        Source file with JNI methods for registry access.
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

#include "com_coi_tools_os_win_RegistryImpl.h"
#include <windows.h>
#include "COIOSHelper.h"


// --------------------------------------------------------------------------
// A "lookup table" to  get signature for the methods of RegDataContainer
// --------------------------------------------------------------------------
static char *TYPE_INIT_SIGNATURE_TABLE[] =
{
	"",							// REG_NONE
	"(Ljava/lang/String;)V",	// REG_SZ
	"(Ljava/lang/String;)V",	// REG_EXPAND_SZ
	"([B)V",					// REG_BINARY
	"(J)V",						// REG_DWORD
	"",							// REG_DWORD_MSB
	"",							// REG_LINK
	"([Ljava/lang/String;)V"	// REG_MULTI_SZ
	
};

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    exist
 * Signature: (ILjava/lang/String;)Z
 * Returns whether a registry key exist or not.
 */
JNIEXPORT jboolean JNICALL Java_com_coi_tools_os_win_RegistryImpl_exist
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jboolean  retval = false;
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		retval = regKeyExist(&libEnv,  jRoot, key );

		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
	return( retval  );
	
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    createKeyN
 * Signature: (ILjava/lang/String;)V
 * Creates the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_createKeyN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		createRegKey(&libEnv,  jRoot, key );
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    setValueN
 * Signature: (ILjava/lang/String;Ljava/lang/String;Lcom/coi/tools/os/win/RegDataContainer;)V
 * Sets the given value into the given RegDataContainer . On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_setValueN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue, jobject jContents)
{
	WinLibEnv libEnv( env, obj );
	WinLibEnv *plibEnv = &libEnv;
	if( libEnv.verifyNullObjects(jKey, jValue, jContents ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		const char *value = env->GetStringUTFChars( jValue , 0);
		const char *buf = NULL;
		jint type	= 0;
				int i;
		LPBYTE contents = NULL;
		DWORD ddata = 0;
		DWORD length = 0;
		jstring  jStr = NULL;
		jbyteArray jBin = NULL;
		jobjectArray jObj = NULL;
		jboolean isCopy = false;
		jstring *jMultiStr = NULL;
		jint multiSize = 0;
		const char ** intermedMulti = NULL;;
		while( 1 )
		{
			jmethodID	mid = NULL;
			jclass clazz	= env->GetObjectClass( jContents );
			if( clazz == NULL )	// oops, class not bound ??
				ERROR_BREAK("registry.MissingRegDataContainer", plibEnv);
			if( (mid = env->GetMethodID( clazz, "getType", "()I" ) ) == NULL )
				ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
			type = env->CallIntMethod( jContents, mid );
			switch( type )
			{
			case REG_DWORD:
				if( (mid = env->GetMethodID( clazz, "getDwordData", "()J" ) ) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				ddata  = (DWORD) env->CallLongMethod( jContents, mid );
				contents = (LPBYTE) &ddata;
				length = 4;
				break;
			case REG_SZ:
			case REG_EXPAND_SZ:
				if( (mid = env->GetMethodID( clazz, "getStringData", "()Ljava/lang/String;" ) ) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				if( (jStr = (jstring) env->CallObjectMethod( jContents, mid )) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				buf = env->GetStringUTFChars( jStr , 0);
				contents = (LPBYTE) buf;
				length	= strlen( buf) + 1;
				break;
			case REG_BINARY:
				if( (mid = env->GetMethodID( clazz, "getBinData", "()[B" ) ) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				if( (jBin = (jbyteArray) env->CallObjectMethod( jContents, mid )) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				contents = (LPBYTE) env->GetByteArrayElements( jBin, &isCopy );
				length	= (DWORD) env->GetArrayLength( jBin );
				break;
			case REG_MULTI_SZ:
				if( (mid = env->GetMethodID( clazz, "getMultiStringData", "()[Ljava/lang/String;" ) ) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				if( (jObj = (jobjectArray) env->CallObjectMethod( jContents, mid)) == NULL )
					ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);
				multiSize = env->GetArrayLength( jObj );
				LPBYTE pos;
				jMultiStr = new jstring[multiSize];
				intermedMulti = new const char *[multiSize];
				length = 2;
				for( i = 0; i < multiSize; ++i )
				{
					jMultiStr[i] = (jstring) env->GetObjectArrayElement( jObj, i );
					intermedMulti[i] = env->GetStringUTFChars( jMultiStr[i], 0);
					length += strlen( intermedMulti[i] ) + 1;
				}
				pos = contents = (LPBYTE) new char[length];
				int monoLength; 
				for( i = 0; i < multiSize; ++i )
				{
					monoLength = strlen(intermedMulti[i] ) + 1;
					memcpy( pos, intermedMulti[i], monoLength);
					pos += monoLength;
				}
				*pos++	= 0;
				*pos	= 0;

				break;
			default:
				ERROR_BREAK("registry.DataTypeNotSupported", plibEnv);

			}
			if( ! libEnv.good() )	// Today not necessary, but may be someone add a line ...
				break;
			break;
		}
		if( libEnv.good() )
			setRegValue(plibEnv, jRoot, key, value, type, contents, length );

		if( buf )
			env->ReleaseStringUTFChars( jStr, buf);
		if( jBin )
		{
			env->ReleaseByteArrayElements( jBin, (jbyte *) contents, JNI_ABORT  );
		}
		if( jMultiStr )
		{
			for( i = 0; i < multiSize; ++i )
			{
				env->ReleaseStringUTFChars(jMultiStr[i], intermedMulti[i]);
				env->DeleteLocalRef( jMultiStr[i] );
			}
			delete [] jMultiStr;
			delete [] intermedMulti;
			delete contents;

		}
		env->ReleaseStringUTFChars( jKey, key);
		env->ReleaseStringUTFChars( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
	return;
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getValueType
 * Signature: (ILjava/lang/String;Ljava/lang/String;)I
 * Returns the value type of the given value. On error a NativeLibException will be thrown.
 */
JNIEXPORT jint JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueType
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue)
{
	WinLibEnv libEnv( env, obj );
	jint  retval = 0;
	if( libEnv.verifyNullObjects(jKey, jValue ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		const char *value = env->GetStringUTFChars( jValue , 0);
		retval = getRegValueType(&libEnv,  jRoot, key, value );
		env->ReleaseStringUTFChars( jKey, key);
		env->ReleaseStringUTFChars( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getValue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)Lcom/coi/tools/os/win/RegDataContainer;
 * Returns the contents of the given value. On error a NativeLibException will be thrown.
 */
JNIEXPORT jobject JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValue
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue)
{
	WinLibEnv libEnv( env, obj );
	WinLibEnv *plibEnv = &libEnv;
	jobject retval = NULL;
	if( libEnv.verifyNullObjects(jKey, jValue ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		const char *value = env->GetStringUTFChars( jValue , 0);
		DWORD type;
		DWORD length;

		LPBYTE winData = getRegValue(&libEnv,  jRoot, key, value , &type, &length);

		while( libEnv.good() )
		{
			jmethodID	mid = NULL;
			jclass 		clazz = NULL;
			if( (clazz = env->FindClass("com/coi/tools/os/win/RegDataContainer") ) == NULL )
				ERROR_BREAK("registry.MissingRegDataContainer", plibEnv);
			if( strlen( TYPE_INIT_SIGNATURE_TABLE[type] ) < 1 )
				ERROR_BREAK("registry.UnsupportedDataType", plibEnv);
			if( (mid = env->GetMethodID( clazz, "<init>", TYPE_INIT_SIGNATURE_TABLE[type]  ) ) == NULL )
				ERROR_BREAK("registry.MalformedRegDataContainer", plibEnv);

			switch( type )
			{
			case REG_DWORD:
				{	// A simple (jlong) *winData will only contains the value of the first byte.
					LPDWORD tDW = (LPDWORD) winData;
					retval =  env->NewObject( clazz, mid,  (jlong) *tDW ); 
				}
				break;
			case REG_SZ:
			case REG_EXPAND_SZ:
				retval = env->NewObject( clazz, mid,  env->NewStringUTF( (char *) winData) ); 
				break;
			case REG_BINARY:
				{
					jbyteArray jba = env->NewByteArray( length );
					env->SetByteArrayRegion( jba, 0, length , (jbyte *) winData );
					retval = env->NewObject( clazz, mid,  jba ); 
				}
				break;
			case REG_MULTI_SZ:
				{
					char *pos = (char *) winData;
					int count = 0;
					jclass	claString;
					while( *pos )
					{
						count++;
						pos += strlen(pos) + 1;
					}
					if( (claString = env->FindClass("java/lang/String") ) == NULL )
						ERROR_BREAK("registry.StringClassNotFound", plibEnv);
					jobjectArray joa = env->NewObjectArray( count, claString, 0 );
					pos = (char *) winData;
					for( int i = 0; i < count; ++i )
					{
						env->SetObjectArrayElement( joa, i, env->NewStringUTF( pos ) );
						pos += strlen(pos) + 1;
					}
					retval = env->NewObject( clazz, mid,  joa ); 
				}
				break;
			}
			if( ! libEnv.good() )	// Today not necessary, but may be someone add a line ...
				break;
			break;
		}
		if( winData )
			delete [] winData;
		env->ReleaseStringUTFChars( jKey, key);
		env->ReleaseStringUTFChars( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    deleteValueN
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 * Deletes the given value. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_deleteValueN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue)
{
	WinLibEnv libEnv( env, obj );
	if( libEnv.verifyNullObjects(jKey, jValue ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		const char *value = env->GetStringUTFChars( jValue , 0);
		deleteRegValue(&libEnv,  jRoot, key, value );
		env->ReleaseStringUTFChars( jKey, key);
		env->ReleaseStringUTFChars( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    deleteKeyN
 * Signature: (ILjava/lang/String;)V
 * Deletes the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_deleteKeyN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);

		deleteRegKey(&libEnv,  jRoot, key );
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    isKeyEmpty
 * Signature: (ILjava/lang/String;)Z
 * Returns whether the given key is empty or not. On error a NativeLibException will be thrown.
 */
JNIEXPORT jboolean JNICALL Java_com_coi_tools_os_win_RegistryImpl_isKeyEmpty
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jboolean retval = false;
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);

		retval = isKeyEmpty(&libEnv,  jRoot, key );
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyCount
 * Signature: (ILjava/lang/String;)I
 * Returns how much subkeys are under the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT jint JNICALL Java_com_coi_tools_os_win_RegistryImpl_getSubkeyCount
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	DWORD	subkeys = 0;
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		DWORD	values = 1;

		determineCounts(&libEnv,  jRoot, key, &subkeys, &values  );
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
	return( subkeys );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyCount
 * Signature: (ILjava/lang/String;)I
 * Returns how much values are under the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT jint JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueCount
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	DWORD	values = 0;
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		DWORD	subkeys = 1;

		determineCounts(&libEnv,  jRoot, key, &subkeys, &values  );
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
	return( values );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyName
 * Signature: (ILjava/lang/String;I)Ljava/lang/String;
 * Returns the name of the subkey of the given key identified with the id.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jstring JNICALL Java_com_coi_tools_os_win_RegistryImpl_getSubkeyName
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jint jKeyId)
{
	jstring	retval = NULL;
	WinLibEnv libEnv( env, obj );
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		char *name = getSubkeyName(&libEnv,  jRoot, key, jKeyId  );
		env->ReleaseStringUTFChars( jKey, key);
		libEnv.verifyAndThrowAtError();
		if( libEnv.good() )	
			retval = env->NewStringUTF( name );
		if( name )
			delete [] name;
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyName
 * Signature: (ILjava/lang/String;I)Ljava/lang/String;
 * Returns the name of the value of the given key identified with the id.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jstring JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueName
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jint jKeyId)
{
	jstring	retval = NULL;
	WinLibEnv libEnv( env, obj );
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		char *name = getValueName(&libEnv,  jRoot, key, jKeyId  );
		env->ReleaseStringUTFChars( jKey, key);
		libEnv.verifyAndThrowAtError();
		if( libEnv.good() )	
			retval = env->NewStringUTF( name );
		if( name )
			delete [] name;
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyNames
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 * Returns the names of all subkeys of the given key.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jobjectArray JNICALL Java_com_coi_tools_os_win_RegistryImpl_getSubkeyNames
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jobjectArray  newArr = NULL;
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		jclass clazz;
		char **names;
		LONG length = 0;
		jstring utf_str;
		int i;
		clazz = env->FindClass( "java/lang/String");
		if( ( length = getSubkeyNames( &libEnv, jRoot, key, &names ) ) > 0 )
		{
			newArr = env->NewObjectArray( length, clazz, NULL);
			for( i = 0; i < length; ++i )
			{
				utf_str = env->NewStringUTF( names[i] );
				env->SetObjectArrayElement( newArr, i, utf_str);
				env->DeleteLocalRef( utf_str );
				delete [] names[i];
				//LocalFree( groups[i] );
				names[i] = NULL;
			}
			delete [] names;
			//LocalFree( groups );
		}
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
	if( libEnv.good() )	
		return( newArr );
	return( NULL );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getValueNames
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 * Returns the names of all values under the the given key.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jobjectArray JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueNames
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jobjectArray  newArr = NULL;
	if( libEnv.verifyNullObjects(jKey ))
	{
		const char *key = env->GetStringUTFChars( jKey , 0);
		jclass clazz;
		char **names;
		LONG length = 0;
		jstring utf_str;
		int i;
		clazz = env->FindClass( "java/lang/String");
		if( ( length = getValueNames( &libEnv, jRoot, key, &names ) ) > 0 )
		{
			newArr = env->NewObjectArray( length, clazz, NULL);
			for( i = 0; i < length; ++i )
			{
				utf_str = env->NewStringUTF( names[i] );
				env->SetObjectArrayElement( newArr, i, utf_str);
				env->DeleteLocalRef( utf_str );
				delete [] names[i];
				//LocalFree( groups[i] );
				names[i] = NULL;
			}
			delete [] names;
			//LocalFree( groups );
		}
		env->ReleaseStringUTFChars( jKey, key);
	}
	libEnv.verifyAndThrowAtError();
	if( libEnv.good() )	
		return( newArr );
	return( NULL );
}




