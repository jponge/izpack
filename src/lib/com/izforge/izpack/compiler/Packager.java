/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               Packager.java
 *  Description :        The abstract class for the packagers.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (c) 2001 Johannes Lehtinen
 *  johannes.lehtinen@iki.fi
 *  http://www.iki.fi/jle/
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
package com.izforge.izpack.compiler;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import com.izforge.izpack.*;

/**
 *  The packager class. A packager is used by the compiler to actually do the
 *  packaging job.
 *
 * @author     Julien Ponge
 * @created    October 26, 2002
 */
public abstract class Packager
{
  /**  The path to the skeleton installer. */
  public final static String SKELETON_PATH = Compiler.IZPACK_HOME + "lib" +
    File.separator + "installer.jar";

  /**  The packs informations. */
  protected ArrayList packs;

  /**  The langpacks ISO3 names. */
  protected ArrayList langpacks;

  /**  The listeners. */
  protected PackagerListener listener;


  /**
   *  Adds a listener.
   *
   * @param  listener  The listener.
   */
  public void setPackagerListener(PackagerListener listener)
  {
    this.listener = listener;
  }


  /**
   *  Dispatches a message to the listeners.
   *
   * @param  job  The job description.
   */
  protected void sendMsg(String job)
  {
    listener.packagerMsg(job);
  }


  /**  Dispatches a start event to the listeners.  */
  protected void sendStart()
  {
    listener.packagerStart();
  }


  /**  Dispatches a stop event to the listeners.  */
  protected void sendStop()
  {
    listener.packagerStop();
  }


  /**
   *  Adds a pack (the compiler sends the merged data).
   *
   * @param  packNumber     The pack number.
   * @param  name           The pack name.
   * @param  required       Is the pack required ?
   * @param  description    The pack description.
   * @return                Description of the Return Value
   * @exception  Exception  Description of the Exception
   */
  public abstract ZipOutputStream addPack(int packNumber, String name,
                                          boolean required, String description)
     throws Exception;


  /**
   *  Adds a panel.
   *
   * @param  classFilename  The class filename.
   * @param  input          The stream to get the file data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addPanelClass(String classFilename, InputStream input)
     throws Exception;


  /**
   *  Sets the GUI preferences.
   *
   * @param  prefs          The new gUIPrefs value
   * @exception  Exception  Description of the Exception
   */
  public abstract void setGUIPrefs(GUIPrefs prefs) throws Exception;


  /**
   *  Sets the panels order.
   *
   * @param  order          The ordered list of the panels.
   * @exception  Exception  Description of the Exception
   */
  public abstract void setPanelsOrder(ArrayList order) throws Exception;


  /**
   *  Sets the informations related to this installation.
   *
   * @param  info           The info section.
   * @exception  Exception  Description of the Exception
   */
  public abstract void setInfo(Info info) throws Exception;

  /**
   *  Adds Variable Declaration.
   *
   * @param  varDef         The variables definitions.
   * @exception  Exception  Description of the Exception
   */
  public abstract void setVariables(Properties varDef) throws Exception;


  /**
   *  Adds a resource.
   *
   * @param  resId          The resource Id.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addResource(String resId, InputStream input) throws Exception;


  /**
   *  Adds a language pack.
   *
   * @param  iso3           The ISO3 code.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addLangPack(String iso3, InputStream input) throws Exception;


  /**
   *  Adds a native library.
   *
   * @param  name           The native library name.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addNativeLibrary(String name, InputStream input) throws Exception;


  /**
   *  Adds a jar file content to the installer.
   *
   * @param  file           The jar filename.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addJarContent(String file) throws Exception;


  /**
   *  Tells the packager to finish the job (misc writings, cleanups, closings ,
   *  ...).
   *
   * @exception  Exception  Description of the Exception
   */
  public abstract void finish() throws Exception;


  /**
   *  Copies all the data from the specified input stream to the specified
   *  output stream. This is an utility method which may be used by the
   *  subclasses. by Johannes Lehtinen
   *
   * @param  in               the input stream to read
   * @param  out              the output stream to write
   * @return                  the total number of bytes copied
   * @exception  IOException  if an I/O error occurs
   */
  protected long copyStream(InputStream in, OutputStream out)
     throws IOException
  {
    byte[] buffer = new byte[5120];
    long bytesCopied = 0;
    int bytesInBuffer;
    while ((bytesInBuffer = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, bytesInBuffer);
      bytesCopied += bytesInBuffer;
    }
    return bytesCopied;
  }


  /**
   *  Called by the Compiler when the pack content adding is done. (JP)
   *
   * @param  number  the pack number
   * @param  nbytes  the number of bytes written
   */
  protected void packAdded(int number, long nbytes)
  {
    ((Pack) packs.get(number)).nbytes = nbytes;
  }
}

