/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               IoHelper.java
 *  Description :        Helper for XML specifications.
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

package com.izforge.izpack.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.izforge.izpack.installer.VariableSubstitutor;

/**
 * <p>Class  with some IO related helper.</p>
 *
 */
public class IoHelper
{

	/**
	 * Default constructor
	 */
  public IoHelper()
  {
    super();
  }

  /**
   * Creates an in- and output stream for the given File objects and
   * copies all the data from the specified input  to the specified
   * output.
   * @param  inFile               File object for input
   * @param  outFile              File object for output
   * @exception  IOException  if an I/O error occurs
   */
  public static void copyFile(File inFile, File outFile)
    throws IOException
  {
    copyFile(inFile, outFile, null, null);
  }
  
  /**
   * Creates an in- and output stream for the given File objects and
   * copies all the data from the specified input  to the specified
   * output.
   * If permissions is not null, a chmod will
   * be done on the output file.
   * @param  inFile               File object for input
   * @param  outFile              File object for output
   * @param permissions           permissions for the output file 
   * @exception  IOException  if an I/O error occurs
   */
  public static void copyFile(File inFile, File outFile, String permissions)
    throws IOException
  {
    copyFile(inFile, outFile, permissions, null);
  }
  
  /**
   * Creates an in- and output stream for the given File objects and
   * copies all the data from the specified input  to the specified
   * output. If the VariableSubstitutor is not null, a substition will
   * be done during copy.
   *
   * @param  inFile               File object for input
   * @param  outFile              File object for output
   * @param vss                   substitutor which is used during copying
   * @exception  IOException  if an I/O error occurs
   */
  public static void copyFile(File inFile, File outFile, VariableSubstitutor vss)
    throws IOException
  {
    copyFile(inFile, outFile, null, vss);
  }
  
  /**
   * Creates an in- and output stream for the given File objects and
   * copies all the data from the specified input  to the specified
   * output. If the VariableSubstitutor is not null, a substition will
   * be done during copy. If permissions is not null, a chmod will
   * be done on the output file.
   *
   * @param  inFile               File object for input
   * @param  outFile              File object for output
   * @param permissions           permissions for the output file 
   * @param vs                    substitutor which is used during copying
   * @exception  IOException  if an I/O error occurs
   */
  public static void copyFile(File inFile, File outFile, String permissions,
    VariableSubstitutor vs)
    throws IOException
  {
    FileOutputStream out = new FileOutputStream(outFile, true);
    FileInputStream in  = new FileInputStream(inFile);
    if( vs == null )
    {
      byte[] buffer = new byte[5120];
      long bytesCopied = 0;
      int bytesInBuffer;
      while ((bytesInBuffer = in.read(buffer)) != -1)
      {
        out.write(buffer, 0, bytesInBuffer);
        bytesCopied += bytesInBuffer;
      }
      in.close();
      out.close();
    }
    else
    {
      BufferedInputStream bin = new BufferedInputStream(in, 5120);
      BufferedOutputStream bout = new BufferedOutputStream(out, 5120);
      vs.substitute(bin, bout, null, null);
      bin.close();
      bout.close();
    }
    if( permissions != null)
    {
      chmod(outFile.getAbsolutePath(), permissions );
    }
    return;
  }

  /**
   * Changes the permissions of the given file to the given POSIX permissions.
   * @param file the file for which the permissions should be changed
   * @param permissions POSIX permissions to be set
   * @throws IOException   if an I/O error occurs
   */
  public static void chmod(File file, String permissions) throws IOException
  {
    chmod( file.getAbsolutePath(), permissions);
  }
  
  /**
   * Changes the permissions of the given file to the given POSIX permissions.
   * This method will be raised an exception, if the OS is not UNIX.
   * @param path the absolute path of the file for which the permissions should be changed
   * @param permissions POSIX permissions to be set
   * @throws IOException   if an I/O error occurs
   */
  public static void chmod(String path, String permissions) throws IOException
  {
    String pathSep = System.getProperty("path.separator");
    String osName = System.getProperty("os.name").toLowerCase();
    // Perform UNIX
    if (pathSep.equals(":") && (!osName.startsWith("mac") ||
      osName.endsWith("x")))
    {
      String[] params = {"chmod", permissions, path};
      String[] output = new String[2];
      FileExecutor fe = new FileExecutor();
      fe.executeCommand(params, output);
    }
    else if( osName.startsWith("mac") )
    {
      throw new IOException("Sorry, chmod not supported yet on mac.");
    }
    else
    {
      throw new IOException("Sorry, chmod not supported yet on windows.");
    }
  }
}
