#ifndef WINLIBENV_H
#define  WINLIBENV_H

/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               WinLibEnv.h
 *  Description :        Header file with environment classes for exception handling etc.
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


#define STD_ARRAY_LENGTH 16

enum WinLibEnvStatus_e
{
	WLES_UNKNOWN = 0,
	WLES_INITIALIZED,
	WLES_OK,
	WLES_WARNING,
	WLES_ERROR
};

class ExceptionNameRecord
{
	private:
		char	*shortName;
		char	*longName;
		int		signatureId;
		int		typeId;
	
	public:
		char	*getShortName() { return(shortName);};
		char	*getLongName() { return(longName);};
		int		getSignatureId() { return( signatureId);};
		int		getTypeId() { return( typeId );};
		ExceptionNameRecord( char *sn, char *ln, int sid, int tid ) {shortName = sn; longName = ln; signatureId = sid; typeId = tid;};
		~ExceptionNameRecord( ) {};
	
};

class WinLibEnv
{
	private:
		static char *ExceptionSignatureMap[];
		static ExceptionNameRecord ExceptionNameMap[];
		unsigned long	win32Error;
		unsigned long	externCode;
		char	*win32ErrorText;
		int		winLibError;
		char	*winLibErrorText;
		JNIEnv 	*jniEnv;
		jobject jniObj;
		WinLibEnvStatus_e	status;
		char	*exceptionTypeName;
		char    *args[STD_ARRAY_LENGTH];
		int		currentArg;
		void getOSMessage();
		ExceptionNameRecord *getExceptionNameRecord( char *exName );	
		void	initialize();
	protected:
	public:
		WinLibEnv(JNIEnv *env, jobject obj);
		virtual ~WinLibEnv();
		jboolean	good() { return(status < WLES_WARNING ? true : false);};
		JNIEnv *getJNIEnv() { return(jniEnv);};

		void	setError( char *err, char *errType);
		void	setError( char *err) { setError(err, ExceptionNameMap[1].getShortName());};
		void	setError( char *err,  unsigned long errCode) { setError( err ); win32Error = errCode;};
		void	setError( int err) 
			{ winLibError = err; status = WLES_ERROR;exceptionTypeName = ExceptionNameMap[1].getShortName();};
		
		void	setErrorWithOS( int err ) { setError(err); win32Error = GetLastError();};
		void	setErrorWithOS( char *err) { setErrorWithOS( err, ExceptionNameMap[1].getShortName() );};
		void	setErrorWithOS( char *err,  char *errType) 
			{ setError( err, errType );win32Error = GetLastError();};

		void	addArg( const char *arg1 );
		void	addArgs( const char *arg1, const char *arg2) { addArg(arg1); addArg(arg2); };
		void	addArgs( const char *arg1, const char *arg2, const char *arg3) { addArgs(arg1, arg2); addArg(arg3); };
		void	addArgs( const char *arg1, const char *arg2, const char *arg3, const char *arg4) 
			{ addArgs(arg1, arg2); addArgs(arg3, arg4); };

		void	reset();
		WinLibEnv *clone();
		void takeAcross( WinLibEnv *from);

		jboolean	verifyAndThrowAtError();
		jboolean	verifyNullObjects(jobject obj1, jobject obj2, jobject obj3, jobject obj4);
		jboolean	verifyNullObjects(jobject obj1, jobject obj2, jobject obj3)
			{ return( verifyNullObjects( obj1, obj2, obj3, (jobject) 47 ));};
		jboolean	verifyNullObjects(jobject obj1, jobject obj2)
			{ return( verifyNullObjects( obj1, obj2,  (jobject) 47, (jobject) 47 ));};
		jboolean	verifyNullObjects(jobject obj1)
			{ return( verifyNullObjects( obj1,  (jobject) 47, (jobject) 47, (jobject) 47 ));};
		
};
#endif
