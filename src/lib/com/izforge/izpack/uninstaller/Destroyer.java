/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               Destroyer.java
 *  Description :        The destroyer.
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
package com.izforge.izpack.uninstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.FileExecutor;

/**
 *  The files destroyer class.
 *
 * @author     Julien Ponge
 */
public class Destroyer extends Thread
{
  /**  True if the destroyer must force the recursive deletion. */
  private boolean forceDestroy;

  /**  The installation path. */
  private String installPath;

  /**  the destroyer listener. */
  private AbstractUIProgressHandler handler;


  /**
   *  The constructor.
   *
   * @param  installPath   The installation path.
   * @param  forceDestroy  Shall we force the recursive deletion.
   * @param  handler       The destroyer listener.
   */
  public Destroyer(String installPath, boolean forceDestroy, AbstractUIProgressHandler handler)
  {
    super("IzPack - Destroyer");

    this.installPath = installPath;
    this.forceDestroy = forceDestroy;
    this.handler = handler;
  }


  /**  The run method.  */
  public void run()
  {
    try
    {
      // We get the list of the files to delete
      ArrayList executables = getExecutablesList();
      FileExecutor executor = new FileExecutor(executables);
      executor.executeFiles(ExecutableFile.UNINSTALL, this.handler);

      ArrayList files = getFilesList();
      int size = files.size();

      handler.startAction ("destroy", size);

      // We destroy the files
      for (int i = 0; i < size; i++)
      {
        File file = (File) files.get(i);
        file.delete();
        handler.progress(i, file.getAbsolutePath());
      }

      // We make a complementary cleanup
      handler.progress(size, "[ cleanups ]");
      cleanup(new File(installPath));

      handler.stopAction ();
    }
    catch (Exception err)
    {
      handler.stopAction ();
      err.printStackTrace();
      handler.emitError("exception caught", err.toString());
    }
  }


  /**
   *  Asks the JVM for the uninstaller deletion.
   *
   * @exception  Exception  Description of the Exception
   */
  private void askUninstallerRemoval() throws Exception
  {
    // Initialisations
    InputStream in = getClass().getResourceAsStream("/jarlocation.log");
    InputStreamReader inReader = new InputStreamReader(in);
    BufferedReader reader = new BufferedReader(inReader);

    // We delete
    File jar = new File(reader.readLine());
    File path = new File(reader.readLine());
    File inst = new File(installPath);
    jar.deleteOnExit();
    path.deleteOnExit();
    inst.deleteOnExit();
  }


  /**
   *  Returns an ArrayList of the files to delete.
   *
   * @return                The files list.
   * @exception  Exception  Description of the Exception
   */
  private ArrayList getFilesList() throws Exception
  {
    // Initialisations
    TreeSet files = new TreeSet(Collections.reverseOrder());
    InputStream in = getClass().getResourceAsStream("/install.log");
    InputStreamReader inReader = new InputStreamReader(in);
    BufferedReader reader = new BufferedReader(inReader);

    // We skip the first line (the installation path)
    reader.readLine();

    // We read it
    String read = reader.readLine();
    while (read != null)
    {
      files.add(new File(read));
      read = reader.readLine();
    }

    // We return it
    return new ArrayList(files);
  }

  private ArrayList getExecutablesList() throws Exception
  {
    ArrayList executables = new ArrayList();
    ObjectInputStream in = new ObjectInputStream(getClass().getResourceAsStream("/executables"));
    int num = in.readInt();
    for(int i=0;i<num;i++)
    {
      ExecutableFile file = (ExecutableFile)in.readObject();
      executables.add(file);
    }
    return executables;
  }

  /**
   *  Makes some reccursive cleanups.
   *
   * @param  file           The file to wipe.
   * @exception  Exception  Description of the Exception
   */
  private void cleanup(File file) throws Exception
  {
    if (file.isDirectory())
    {
      File[] files = file.listFiles();
      int size = files.length;
      for (int i = 0; i < size; i++)
        cleanup(files[i]);
      file.delete();
    }
    else
      if (forceDestroy)
        file.delete();

  }
}

