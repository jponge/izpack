/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               SimpleUninstallerListener.java
 *  Description :        Simple custom action listener implementation 
 *                       for uninstall time.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de/
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

import java.io.File;
import java.util.List;

import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * <p>
 * This class implements all methods of interface UninstallerListener, but do
 * not do enything. It can be used as base class to save implementation of
 * unneeded methods.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public class SimpleUninstallerListener implements UninstallerListener
{

    /**
     * 
     */
    public SimpleUninstallerListener()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDeletion(java.util.List,
     *      com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
        ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDelete(java.io.File,
     *      com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
        ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDelete(java.io.File,
     *      com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
        ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDeletion(java.util.List,
     *      com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
        ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#isFileListener()
     */
    public boolean isFileListener()
    {
        return false;
    }

}
