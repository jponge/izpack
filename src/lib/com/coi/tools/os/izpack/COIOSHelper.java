/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               COIOSHelper.java
 *  Description :        This class handles the COIOSHelper.dll.
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

import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.NativeLibraryClient;

/**
 * 
 * Base class to handle multiple native methods of multiple classes in one shared library. This is a
 * singelton class.
 * 
 * @author Klaus Bartz
 *  
 */
public class COIOSHelper
{

    private static COIOSHelper self = null;

    private static int used = 0;

    private static boolean destroyed = false;

    /**
     * This method is used to free the library at the end of progam execution. After this call, any
     * instance of this class will not be usable any more!
     */
    private native void FreeLibrary(String name);

    /**
     * Default constructor, do not use
     */
    private COIOSHelper()
    {
        super();
    }

    /**
     * Returns the one existent object of this class.
     */
    public static synchronized COIOSHelper getInstance()
    {
        if (self == null) self = new COIOSHelper();
        return (self);

    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is used to free the library at the end of progam execution. This is the method of
     * the helper class which will be called from other objects. After this call, any instance of
     * this class will not be usable any more! <b><i><u>Note that this method does NOT return </u>
     * at the first call, but at any other </i> </b> <br>
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
    /**
     * @param name
     */
    public void freeLibrary(String name)
    {
        used--;
        if (!destroyed)
        {
            FreeLibrary(name);
            destroyed = true;
        }
    }

    /**
     * Add a NativeLibraryClient as dependant to this object. The method tries to load the shared
     * library COIOSHelper which should contain native methods for the dependant.
     * 
     * @param dependant
     *            to be added
     */
    public void addDependant(NativeLibraryClient dependant) throws Exception
    {
        used++;
        try
        {
            Librarian.getInstance().loadLibrary("COIOSHelper", dependant);
        }
        catch (UnsatisfiedLinkError exception)
        {
            throw (new Exception("could not locate native library"));
        }

    }

}