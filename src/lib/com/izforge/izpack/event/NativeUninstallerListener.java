/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               NativeUninstallerListener.java
 *  Description :        Base class for  custom action listener 
 *                       implementations with native parts for uninstall time.
 *  Author's email :     bartzkau@users.berlios.de
 *  Website :            http://www.izforge.com
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

package com.izforge.izpack.event;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * This class implements some methods which are needed by installer custom actions with native
 * parts.
 * 
 * @author Klaus Bartz
 *  
 */
public class NativeUninstallerListener extends SimpleUninstallerListener
{

    /** The packs locale database. */
    protected static LocaleDatabase langpack = null;

    /**
     * Default constructor.
     */
    public NativeUninstallerListener()
    {
        super();
        // Create langpack for error messages.
        if (langpack == null)
        {
            // Load langpack. Do not stop uninstall if not found.
            try
            {
                NativeUninstallerListener.langpack = new LocaleDatabase(
                        NativeUninstallerListener.class.getResourceAsStream("/langpack.xml"));
                WrappedNativeLibException.setLangpack(NativeUninstallerListener.langpack);
            }
            catch (Throwable exception)
            {}
        }
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
}