/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               RegistryDefaultHandler.java
 *  Description :        The default handler for registry related
 *                       stuff at installation time (Active only on windows). 
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

import com.izforge.izpack.util.TargetFactory;

/**
 * This class provides on windows a registry handler. All classes which needs registry access should
 * be use only one handler.
 * 
 * @author Klaus Bartz
 *  
 */
public class RegistryDefaultHandler
{

    private static RegistryHandler registryHandler = null;

    private static boolean initialized = false;

    /**
     * Default constructor. No instance of this class should be created.
     */
    private RegistryDefaultHandler()
    {
        super();
    }

    public synchronized static final RegistryHandler getInstance()
    {
        if (!initialized)
        {
            try
            {
                // Load the system dependant handler.
                registryHandler = (RegistryHandler) (TargetFactory.getInstance()
                        .makeObject("com.izforge.izpack.util.os.RegistryHandler"));
                // Switch to the default handler to use one for complete logging.
                registryHandler = registryHandler.getDefaultHandler();
            }
            catch (Throwable exception)
            {
                registryHandler = null; // 
            }
            initialized = true;
        }
        if (registryHandler != null && (!registryHandler.good() || !registryHandler.doPerform()))
                registryHandler = null;

        return (registryHandler);
    }
}