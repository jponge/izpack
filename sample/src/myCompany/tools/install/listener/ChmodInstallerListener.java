/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               ChmodInstallerListener.java
 *  Description :        Example for custom action for install time.
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

package com.myCompany.tools.install.listener;

import java.io.File;
import java.io.IOException;

import com.izforge.izpack.PackFile;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.event.SimpleInstallerListener;
import com.izforge.izpack.installer.InstallerException;

/**
 * <p>InstallerListener for file and directory permissions
 * on Unix.</p>
 *
 * @author  Klaus Bartz
 *
 */
public class ChmodInstallerListener extends SimpleInstallerListener
{
  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.InstallerListener#isFileListener()
   */
  public boolean isFileListener()
  {
    // This is a file related listener.
    return true;
  }
  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#handleFile(java.io.File, com.izforge.izpack.PackFile)
   */
  public void afterFile(File filePath, PackFile pf) throws Exception
  {
    if( pf.getAdditionals()  == null )  
      return;
    Object file = pf.getAdditionals().get("permission.file");
    int fileVal = -1;
    if( file != null && file instanceof Integer )
      fileVal = ((Integer) file).intValue();
    if( fileVal != -1)
      chmod(filePath, fileVal);
  }

  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.InstallerListener#handleDir(java.io.File, com.izforge.izpack.PackFile)
   */
  public void afterDir(File dirPath, PackFile pf) throws Exception
  {
    if( pf.getAdditionals() == null )  
      return;
    if( dirPath == null )
      return;
    Object dir = pf.getAdditionals().get("permission.dir");
    int dirVal = -1;
    if( dir != null &&dir instanceof Integer )
      dirVal = ((Integer) dir).intValue();
    if( dirVal != -1)
    {
      if( (dirVal & 0x000001C0) < 0x000001C0 )
        throw new InstallerException( "Bad owner permission for directory " 
          + dirPath.getAbsolutePath() +"; at installation time the owner needs full rights" );
      chmod(dirPath, dirVal);
    }
  }

  private void chmod(File path, int permissions) throws IOException
  {
    String pathSep = System.getProperty("path.separator");
    if(OsVersion.IS_WINDOWS)
    {
      throw new IOException("Sorry, chmod not supported yet on windows; use this class OS dependant.");
    }
    if( path == null )
    // Oops this is an error, but in this example we ignore it ...
      return;
    String permStr = Integer.toOctalString(permissions);
    String[] params = {"chmod", permStr, path.getAbsolutePath()};
    String[] output = new String[2];
    FileExecutor fe = new FileExecutor();
    fe.executeCommand(params, output);
  }
}
