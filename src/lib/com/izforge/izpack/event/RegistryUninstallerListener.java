/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               RegistryUninstallerListener.java
 *  Description :        Custom action for handle registry related 
 *                       stuff at uninstall time.
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
package com.izforge.izpack.event;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * Uninstaller custom action for handling registry entries. The needed configuration data are
 * written at installation time from the corresponding installer custom action. An external
 * definiton is not needed.
 * 
 * @author Klaus Bartz
 *  
 */
public class RegistryUninstallerListener extends NativeUninstallerListener
{

    /**
     * Default constructor
     */
    public RegistryUninstallerListener()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDeletion(java.util.List,
     *      com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Load the defined actions.
        InputStream in = getClass().getResourceAsStream("/registryEntries");
        if (in == null)
        { // No actions, nothing todo.
            return;
        }
        ObjectInputStream objIn = new ObjectInputStream(in);
        List allActions = (List) objIn.readObject();
        objIn.close();
        in.close();
        if (allActions == null || allActions.size() < 1) return;
        try
        {
            RegistryHandler registryHandler = initializeRegistryHandler();
            if (registryHandler == null) return;
            registryHandler.activateLogging();
            registryHandler.setLoggingInfo(allActions);
            registryHandler.rewind();
        }
        catch (Exception e)
        {
            if (e instanceof NativeLibException)
            {
                throw new WrappedNativeLibException(e);
            }
            else
                throw e;
        }
    }

    private RegistryHandler initializeRegistryHandler() throws Exception
    {
        RegistryHandler registryHandler = null;
        try
        {
            registryHandler = (RegistryHandler) (TargetFactory.getInstance()
                    .makeObject("com.izforge.izpack.util.os.RegistryHandler"));
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
            registryHandler = null; // Do nothing, do not set permissions ...
        }
        if (registryHandler != null && (!registryHandler.good() || !registryHandler.doPerform()))
        {
            System.out.println("initializeRegistryHandler is Bad " + registryHandler.good()
                    + registryHandler.doPerform());
            registryHandler = null;
        }
        return (registryHandler);
    }

}