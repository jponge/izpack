/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2002 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
 *
 *  File :               Debug.java
 *  Description :        a ButtonFactory.
 *  Author's email :     jblok@profdata.nl
 *  Author's Website :   http://www.profdata.nl
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
package com.izforge.izpack.util;

import java.io.*;

public class Debug
{
	private static final boolean TRACE;
	private static final boolean STACKTRACE;
	public static boolean LOG;
	
	static 
	{
		boolean st = false;
		try
		{
			 st = Boolean.getBoolean ("STACKTRACE");
		}
		catch(Exception ex)
		{
			//ignore
		}
		STACKTRACE = st;
		
		boolean t = false;
		try
		{
			if (STACKTRACE)
			{
				t = true;
			}
			else
			{
				t = Boolean.getBoolean ("TRACE");
			}
		}
		catch(Exception ex)
		{
			//ignore
		}
		TRACE = t;
	}
	
	public static void trace(Object s)
	{
		if (TRACE)
		{
//			console.println(s.toString());
			System.out.println(s);
			if(STACKTRACE && (s instanceof Throwable ))
			{
//				StringWriter sw = new StringWriter();
//				PrintWriter pw = new PrintWriter(sw);
//				((Throwable)s).printStackTrace(pw);
//				console.println(sw.toString());
				((Throwable)s).printStackTrace();
			}
			System.out.flush();
		}
	}

	public static void error(Object s)
	{
		trace(s);
		System.err.println(s);
		System.err.flush();
		
		if (LOG && logFile == null) createLogFile();
		if (LOG && logFile != null && s != null)
		{
			logFile.print(s);
			if(s instanceof Throwable )
			{
				((Throwable)s).printStackTrace(logFile);
			}
			logFile.flush();
		}
	}
	
	private static PrintWriter logFile;
	private static void createLogFile()
	{
		try
		{
			File out = new File(System.getProperty("user.dir"),".log.txt");
			if (out.canWrite())
			{
				if (out.exists()) out.delete();
				FileWriter fw = new FileWriter(out);
				logFile = new PrintWriter(fw);
			}
		}
		catch (Throwable e)
		{
			System.err.println(e);
			System.err.flush();
		}
	}
	
	public static boolean tracing()
	{
		return TRACE;
	}
}
