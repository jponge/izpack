/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               WrappedNativeLibException.java
 *  Description :        Wrapper class for NativeLibException of COIOSHelper.
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
package com.izforge.izpack.util.os;

import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.LocaleDatabase;

/**
 * This class allows it to define error messages for <code>NativeLibException</code> s in the
 * IzPack locale files. The getMessage methode searches in the current langpack for entries which
 * are corresponding to that one which are received from native part. If the langpack do not contain
 * the entry, the resource boundle is used.
 * 
 * @author Klaus Bartz
 *  
 */
public class WrappedNativeLibException extends Exception
{

    /** The packs locale database. */
    protected static LocaleDatabase langpack = null;

    /**
     * Default constructor.
     */
    public WrappedNativeLibException()
    {
        super();
    }

    /**
     * @param message
     */
    public WrappedNativeLibException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public WrappedNativeLibException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public WrappedNativeLibException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage()
    {
        StringBuffer retval = new StringBuffer();
        boolean next = false;
        boolean ok = false;
        if (getCause() instanceof NativeLibException)
        {
            NativeLibException nle = (NativeLibException) getCause();
            if (langpack != null)
            {
                while (true)
                {
                    if (nle.getLibMessage() != null)
                    {
                        String val = (String) langpack.get("NativeLibException."
                                + nle.getLibMessage());
                        if (val == null) break;
                        retval.append(val);
                        next = true;
                    }
                    else if (nle.getLibErr() != 0)
                    {
                        String val = (String) langpack.get("NativeLibException.libErrNumber."
                                + Integer.toString(nle.getLibErr()));
                        if (val == null) break;
                        if (next) retval.append("\n");
                        next = true;
                        retval.append(val);
                    }
                    if (nle.getOsErr() != 0)
                    {
                        String val = (String) langpack
                                .get("NativeLibException.libInternal.OsErrNumPraefix")
                                + Integer.toString(nle.getOsErr());
                        if (val == null) break;
                        if (next) retval.append("\n");
                        next = true;
                        retval.append(val);
                    }
                    if (nle.getOsMessage() != null)
                    {
                        String val = (String) langpack
                                .get("NativeLibException.libInternal.OsErrStringPraefix")
                                + nle.getOsMessage();
                        if (val == null) break;
                        if (next) retval.append("\n");
                        next = true;
                        retval.append(val);
                    }
                    ok = true;
                    break;
                }
            }
            if (ok && retval.length() > 0)
                return (nle.reviseMsgWithArgs(retval.toString()));
            else
                return (nle.getMessage());

        }
        else
            return (super.getMessage());
    }

    /**
     * Returns the langpack.
     * 
     * @return Returns the langpack.
     */
    public static LocaleDatabase getLangpack()
    {
        return langpack;
    }

    /**
     * Sets the langpack to the given locale database.
     * 
     * @param langpack
     *            the langpack to set.
     */
    public static void setLangpack(LocaleDatabase langpack)
    {
        WrappedNativeLibException.langpack = langpack;
    }
}