/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               NativeInstallerListener.java
 *  Description :        Base class for  custom action listener 
 *                       implementations with native parts for install time.
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

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * This class implements some methods which are needed by installer custom actions with native
 * parts.
 * 
 * @author Klaus Bartz
 *  
 */
public class NativeInstallerListener extends SimpleInstallerListener
{

    /**
     * Default constructor
     */
    public NativeInstallerListener()
    {
        super();
    }

    /**
     * Constructs a native installer listener. If useSpecHelper is true, a specification helper will
     * be created.
     * 
     * @param useSpecHelper
     *  
     */
    public NativeInstallerListener(boolean useSpecHelper)
    {
        super(useSpecHelper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     *      int, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);

        if (SimpleInstallerListener.langpack != null)
        { // Initialize WrappedNativeLibException with the langpack for error messages.
            WrappedNativeLibException.setLangpack(SimpleInstallerListener.langpack);
        }

    }

}