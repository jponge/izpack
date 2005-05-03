/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               Registry.java
 *  Description :        Wrapper class for com.coi.tools.os.win.RegistryImpl.
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
package com.coi.tools.os.izpack;

import com.coi.tools.os.win.RegistryImpl;
import com.izforge.izpack.util.NativeLibraryClient;

/**
 * Wrapper class for com.coi.tools.os.win.RegistryImpl for using it with IzPack. This class
 * implements only the methods of interface NativeLibraryClient. All other methods are used directly
 * from RegistryImpl.
 * 
 * @author Klaus Bartz
 *  
 */
public class Registry extends RegistryImpl implements NativeLibraryClient
{

    /**
     * Default constructor.
     */
    public Registry() throws Exception
    {
        super();
        initialize();
    }

    /**
     * Initialize native part of this class and other settings.
     * 
     * @exception Exception
     *                if problems are encountered
     */
    /*--------------------------------------------------------------------------*/
    private void initialize() throws Exception
    {
        COIOSHelper.getInstance().addDependant(this);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is used to free the library at the end of progam execution. This class has no own
     * library else it shares it in the COI common lib. To free the library, the helper class is
     * called. After this call, any instance of this class will not be usable any more! <b><i>
     * <u>Note that this method does NOT return </u> at the first call, but at any other </i> </b>
     * <br>
     * <br>
     * <b>DO NOT CALL THIS METHOD DIRECTLY! </b> <br>
     * It is used by the librarian to free the native library before physically deleting it from its
     * temporary loaction. A call to this method will freeze the application irrecoverably!
     * 
     * @param name
     *            the name of the library to free. Use only the name and extension but not the path.
     * 
     * @see com.izforge.izpack.util.NativeLibraryClient#freeLibrary
     */
    /*--------------------------------------------------------------------------*/
    public void freeLibrary(String name)
    {

        COIOSHelper.getInstance().freeLibrary(name);
    }

}