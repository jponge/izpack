/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
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
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.event.UninstallerListener;
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
  public Destroyer(
    String installPath,
    boolean forceDestroy,
    AbstractUIProgressHandler handler)
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
      // We get the list of uninstaller listeners
      List [] listeners = getListenerLists(); 
      // We get the list of the files to delete
      ArrayList executables = getExecutablesList();


      FileExecutor executor = new FileExecutor(executables);
      executor.executeFiles(ExecutableFile.UNINSTALL, this.handler);

      ArrayList files = getFilesList();
      int size = files.size();

      // Custem action listener stuff --- beforeDeletion ----
      informListeners(listeners[0], UninstallerListener.BEFORE_DELETION,
        files, handler);

      handler.startAction("destroy", size);

      // We destroy the files
      for (int i = 0; i < size; i++)
      {
        File file = (File) files.get(i);
        // Custem action listener stuff --- beforeDelete ----
        informListeners(listeners[1], UninstallerListener.BEFORE_DELETE,
          file, handler);

        file.delete();

        // Custem action listener stuff --- afterDelete ----
        informListeners(listeners[1], UninstallerListener.AFTER_DELETE,
          file, handler);

       handler.progress(i, file.getAbsolutePath());
      }

      // Custem action listener stuff --- afterDeletion ----
      informListeners(listeners[0], UninstallerListener.AFTER_DELETION,
        files, handler);

      // We make a complementary cleanup
      handler.progress(size, "[ cleanups ]");
      cleanup(new File(installPath));

      handler.stopAction();
    } catch (Exception err)
    {
      handler.stopAction();
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
    InputStream in = Destroyer.class.getResourceAsStream("/jarlocation.log");
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
    InputStream in = Destroyer.class.getResourceAsStream("/install.log");
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
    ObjectInputStream in =
      new ObjectInputStream(Destroyer.class.getResourceAsStream("/executables"));
    int num = in.readInt();
    for (int i = 0; i < num; i++)
    {
      ExecutableFile file = (ExecutableFile) in.readObject();
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
    } else if (forceDestroy)
      file.delete();

  }

  // CUSTOM ACTION STUFF -------------- start -----------------

  /**
   * Load the defined uninstall listener objects.
   * @return a list with the defined uninstall listeners
   * @throws Exception
   */
  private List [] getListenerLists() throws Exception
  {
    ArrayList [] uninstaller = new ArrayList[] {new ArrayList(),new ArrayList()};
    // Load listeners if exist
    InputStream in;
    ObjectInputStream objIn;
    in = Destroyer.class.getResourceAsStream("/uninstallerListeners");
    if( in != null )
    {
      objIn = new ObjectInputStream(in);
      List listeners = (List)objIn.readObject();
      objIn.close();
      Iterator iter = listeners.iterator();
      while( iter != null && iter.hasNext())
      {
        Class clazz = Class.forName(((String) iter.next()));
        UninstallerListener ul = (UninstallerListener) clazz.newInstance();
        if( ul.isFileListener())
          uninstaller[1].add( ul );
        uninstaller[0].add( ul );
     }
    }
    return uninstaller;
  }
  /**
   * Informs all listeners.
   * @param listeners list with the listener objects
   * @param action identifier which callback should be called
   * @param param parameter for the call
   * @param handler the current progress handler
   */

  private void informListeners(List listeners, int action, 
    Object param, AbstractUIProgressHandler handler)
  {
    // Iterate the action list.
    Iterator iter = listeners.iterator();
    UninstallerListener il = null;
    while( iter.hasNext())
    {
      try
      {
        il = (UninstallerListener) iter.next();
        switch( action )
        {
          case UninstallerListener.BEFORE_DELETION:
            il.beforeDeletion(  (List) param, handler );
            break;
          case UninstallerListener.AFTER_DELETION:
            il.afterDeletion(  (List) param, handler );
            break;
          case UninstallerListener.BEFORE_DELETE:
            il.beforeDelete(  (File) param, handler );
            break;
          case UninstallerListener.AFTER_DELETE:
            il.afterDelete(  (File) param, handler );
            break;
        }
      }
      catch(Throwable e)
      { // Catch it to prevent for a block of uninstallation.
        handler.emitError("Skipping custom action because exception caught during " 
          + il.getClass().getName(), e.toString());
      }
    }
  }

  // CUSTOM ACTION STUFF -------------- end -----------------

}
