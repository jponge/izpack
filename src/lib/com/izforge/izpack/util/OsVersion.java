/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Hani Suleiman
 *
 *  File :               OsVersion.java
 *  Description :        Helper for OS version handling.
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

/**
 * Date: Nov 9, 2004 Time: 8:53:22 PM
 * 
 * @author hani
 */
public final class OsVersion
{

    public static final String OS_NAME = System.getProperty("os.name");

    private static boolean startsWith(String str, String prefix)
    {
        return str != null && str.startsWith(prefix);
    }

    private static boolean startsWithIgnoreCase(String str, String prefix)
    {
        return str != null && str.toUpperCase().startsWith(prefix.toUpperCase());
    }

    /**
     * True if this is FreeBSD.
     */
    public static final boolean IS_FREEBSD = startsWithIgnoreCase(OS_NAME, "FreeBSD");

    /**
     * True if this is Linux.
     */
    public static final boolean IS_LINUX = startsWithIgnoreCase(OS_NAME, "Linux");

    /**
     * True if this is HP-UX.
     */
    public static final boolean IS_HPUX = startsWithIgnoreCase(OS_NAME, "HP-UX");

    /**
     * True if this is AIX.
     */
    public static final boolean IS_AIX = startsWithIgnoreCase(OS_NAME, "AIX");

    /**
     * True if this is SunOS.
     */
    public static final boolean IS_SUNOS = startsWithIgnoreCase(OS_NAME, "SunOS");

    /**
     * True if this is OS/2.
     */
    public static final boolean IS_OS2 = startsWith(OS_NAME, "OS/2");

    /**
     * True if this is the Mac OS X.
     */
    public static final boolean IS_OSX = startsWith(OS_NAME, "Mac") && OS_NAME.endsWith("X");

    /**
     * True if this is Windows.
     */
    public static final boolean IS_WINDOWS = startsWith(OS_NAME, "Windows");

    /**
     * True if this is some variant of Unix (OSX, Linux, Solaris, FreeBSD, etc).
     */
    public static final boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS;
}