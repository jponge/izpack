/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               UninstallData.java
 *  Description :        Uninstaller data.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
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
package com.izforge.izpack.installer;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.ExecutableFile;

/**
 *  Holds uninstallation data. Implemented as a singleton.
 *
 * @author     Julien Ponge
 * created    October 27, 2002
 */
public class UninstallData
{
  /**  The uninstall data object. */
  private static UninstallData instance = null;

  /**  The files list. */
  private List filesList;

  /**  The executables list. */
  private List executablesList;

  /**  The uninstaller jar filename. */
  private String uninstallerJarFilename;

  /**  The uninstaller path. */
  private String uninstallerPath;


  /**  The constructor.  */
  private UninstallData()
  {
    filesList = new ArrayList();
    executablesList = new ArrayList();
  }


  /**
   *  Returns the instance (it is a singleton).
   *
   * @return    The instance.
   */
  public synchronized static UninstallData getInstance()
  {
    if (instance == null)
      instance = new UninstallData();
    return instance;
  }


  /**
   *  Adds a file to the data.
   *
   * @param  path  The file to add.
   */
  public synchronized void addFile(String path)
  {
    filesList.add(path);
  }


  /**
   *  Returns the files list.
   *
   * @return    The files list.
   */
  public List getFilesList()
  {
    return filesList;
  }


  /**
   *  Adds an executable to the data.
   *
   * @param file The executable file.
   */
  public synchronized void addExecutable(ExecutableFile file)
  {
    executablesList.add(file);
  }


  /**
   *  Returns the executables list.
   *
   * @return    The executables list.
   */
  public List getExecutablesList()
  {
    return executablesList;
  }


  /**
   *  Returns the uninstaller jar filename.
   *
   * @return    The uninstaller jar filename.
   */
  public String getUninstallerJarFilename()
  {
    return uninstallerJarFilename;
  }


  /**
   *  Sets the uninstaller jar filename.
   *
   * @param  name  The uninstaller jar filename.
   */
  public synchronized void setUninstallerJarFilename(String name)
  {
    uninstallerJarFilename = name;
  }


  /**
   *  Returns the path to the uninstaller.
   *
   * @return    The uninstaller filename path.
   */
  public String getUninstallerPath()
  {
    return uninstallerPath;
  }


  /**
   *  Sets the uninstaller path.
   *
   * @param  path  The uninstaller path.
   */
  public void setUninstallerPath(String path)
  {
    uninstallerPath = path;
  }
}

