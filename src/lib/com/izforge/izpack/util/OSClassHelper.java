/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               OSClassHelper.java
 *  Description :        Base class for system dependant
 *                       classes. 
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

package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;

/*---------------------------------------------------------------------------*/
/**
 * This class is the system independent base class for helpers which are system dependent in its
 * subclasses.
 * 
 * @author Klaus Bartz
 */
/*---------------------------------------------------------------------------*/
public class OSClassHelper
{

    protected AutomatedInstallData installdata;

    protected Class workerClass = null;

    protected Object worker = null;

    /**
     * Default constructor
     */
    public OSClassHelper()
    {
        super();
    }

    /**
     * Creates an object which contains as worker an object of the given class name if possible. If
     * not possible, only the stack trace will be printed, no exception will be raised. To determine
     * the state, there is the method good.
     * 
     * @param className
     *            full qualified class name of the needed worker
     */
    public OSClassHelper(String className)
    {
        super();

        try
        {
            workerClass = Class.forName(className);
            worker = workerClass.newInstance();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            // Do nothing, class not bound.
        }
        Debug.trace("Ctor ReflectionHelper for " + className + " is good: " + good());

    }

    public boolean good()
    {
        return (worker != null ? true : false);
    }

    public boolean verify(AutomatedInstallData idata) throws Exception
    {
        installdata = idata;
        return (false);
    }

}