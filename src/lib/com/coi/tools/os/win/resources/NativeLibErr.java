/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               NativeLibErr.java
 *  Description :        "Global" ListResourceBundle for NativeLibException.
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
package com.coi.tools.os.win.resources;

import java.util.ListResourceBundle;

/**
 * "Global" (English) resource bundle for NativLibException.
 * 
 * @author Klaus Bartz
 *  
 */
public class NativeLibErr extends ListResourceBundle
{

    private static final Object[][] contents = {
            { "libInternal.OsErrNumPraefix", " System error number is: "},
            { "libInternal.OsErrStringPraefix", " System error text is: "},
            { "system.outOfMemory", "Out of memory in the native part."},

            { "functionFailed.RegOpenKeyEx", "Cannot open registry key {0}\\{1}."},
            { "functionFailed.RegCreateKeyEx", "Cannot create registry key {0}\\{1}."},
            { "functionFailed.RegDeleteKey", "Cannot delete registry key {0}\\{1}."},
            { "functionFailed.RegEnumKeyEx", "Not possible to determine sub keys for key {0}\\{1}."},
            { "functionFailed.RegEnumValue", "Not possible to determine value under key {0}\\{1}."},
            { "functionFailed.RegSetValueEx",
                    "Cannot create value {2} under registry key {0}\\{1}."},
            { "functionFailed.RegDeleteValue",
                    "Cannot delete value {2} under registry key {0}\\{1}."},
            { "functionFailed.RegQueryValueEx",
                    "No informations available for value {2} of registry key {0}\\{1}."},
            { "functionFailed.RegQueryInfoKey",
                    "No informations available for registry key {0}\\{1}."},

            { "registry.ValueNotFound", "Registry value not found."},
            { "registry.KeyNotFound", "Registry key not found."},
            { "registry.KeyExist", "Cannot create registry key {0}\\{1} because key exist already."}};

    /**
     * Default constructor.
     */
    public NativeLibErr()
    {
        super();
    }

    /**
     * Returns the contents array.
     * 
     * @return contents array
     */
    protected Object[][] getContents()
    {
        return contents;
    }

}