/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               NativeLibErr_de.java
 *  Description :        German ListResourceBundle for NativeLibException.
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
 * German resource bundle for NativLibException.
 * 
 * @author Klaus Bartz
 *  
 */
public class NativeLibErr_de extends ListResourceBundle
{

    private static final Object[][] contents = {
            { "libInternal.OsErrNumPraefix", " Fehlernummer des Betriebssystems: "},
            { "libInternal.OsErrStringPraefix", " Fehlertext des Betriebssystems: "},
            { "system.outOfMemory", "Out of memory in einer DLL."},
            { "functionFailed.RegOpenKeyEx",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht ge\u00f6ffnet werden."},
            { "functionFailed.RegCreateKeyEx",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht angelegt werden."},
            { "functionFailed.RegDeleteKey",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht gel\u00f6scht werden."},
            {
                    "functionFailed.RegEnumKeyEx",
                    "Zu dem Registry-Schl\u00fcssel {0}\\{1} konnte der angeforderte Unterschl\u00fcssel nicht ermittelt werden."},
            { "functionFailed.RegEnumValue",
                    "Zu dem Registry-Schl\u00fcssel {0}\\{1} konnte der angeforderte Wert nicht ermittelt werden."},
            { "functionFailed.RegSetValueEx",
                    "Der Wert {2} unter dem Registry-Schl\u00fcssel {0}\\{1} konnte nicht gesetzt werden."},
            { "functionFailed.RegDeleteValue",
                    "Der Wert {2} unter dem Registry-Schl\u00fcssel {0}\\{1} konnte nicht gel\u00f6scht werden."},
            {
                    "functionFailed.RegQueryValueEx",
                    "Zu dem Wert {2} unter dem Registry-Schl\u00fcssel {0}\\{1} konnten keine Informationen ermittelt werden."},
            { "functionFailed.RegQueryInfoKey",
                    "Die angeforderten Informationen zum Registry-Key {0}\\{1} konnten nicht ermittelt werden."},
            { "registry.ValueNotFound", "Der angeforderte Registry-Wert wurde nicht gefunden."},
            { "registry.KeyNotFound",
                    "Der angeforderte Registry-Schl\u00fcssel wurde nicht gefunden."},
            { "registry.KeyExist",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht angelegt werden, weil er bereits existierte."}};

    /**
     * Returns the contents array.
     * 
     * @return contents array
     */
    protected Object[][] getContents()
    {
        return contents;
    }

    /**
     * Default constructor.
     */
    public NativeLibErr_de()
    {
        super();
    }

}