/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               UninstallData.java
 * Description :        Uninstaller data.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.installer;

import com.izforge.izpack.*;

import java.net.*;
import java.util.*;

import java.awt.*;

import net.n3.nanoxml.*;

public class UninstallData
{
    //.....................................................................
    
    // The fields
    private static UninstallData instance = null;
    private ArrayList filesList;
    private ArrayList executablesList;
    private String uninstallerJarFilename;
    private String uninstallerPath;
    
    // The constructor
    private UninstallData()
    {
        filesList = new ArrayList();
    }
    
    //.....................................................................

    // The methods
    
    // Returns the instance (it is a singleton)
    public static UninstallData getInstance()
    {
        if (instance == null)
            instance = new UninstallData();
        return instance;
    }
    
    // Adds a file to the data
    public void addFile(String path)
    {
        filesList.add(path);
    }
    
    // Returns the files list
    public ArrayList getFilesList()
    {
        return filesList;
    }
    
    // Adds an executable to the data
    public void addExecutable(String path)
    {
        executablesList.add(path);
    }
    
    // Returns the executables list
    public ArrayList getExecutablesList()
    {
        return executablesList;
    }

    // Returns the uninstaller jar filename
    public String getUninstallerJarFilename()
    {
        return uninstallerJarFilename;
    }
    
    // Sets the uninstaller jar filename
    public void setUninstallerJarFilename(String name)
    {
        uninstallerJarFilename = name;
    }
    
    // Returns the path to the uninstaller
    public String getUninstallerPath()
    {
        return uninstallerPath;
    }
    
    // Sets the uninstaller path
    public void setUninstallerPath(String path)
    {
        uninstallerPath = path;
    }
    
    //.....................................................................    
}
