/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               WebPackager.java
 *  Description :        The web installer packager class.
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
package com.izforge.izpack.compiler;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.Pack;

/**
 *  Web packager class.
 *
 * @author     Julien Ponge
 */
public class WebPackager extends Packager
{
  /**  The zipped output stream. */
  protected JarOutputStream outJar;

  /**  The web jar file output stream. */
  protected JarOutputStream webJar;


  /**
   *  The constructor.
   *
   * @param  outputFilename  The output filename.
   * @param  plistener       The packager listener.
   * @exception  Exception   Description of the Exception
   */
  public WebPackager(String outputFilename, PackagerListener plistener) throws Exception
  {
    packs = new ArrayList();
    langpacks = new ArrayList();
    setPackagerListener(plistener);

    sendStart();

    // Sets up the zipped output stream
    FileOutputStream outFile = new FileOutputStream(outputFilename);
    outJar = new JarOutputStream(outFile);
    outJar.setLevel(9);

    // Sets up the web output jar stream
    outputFilename = outputFilename.substring(0, outputFilename.length() - 4) + "_web.jar";
    outFile = new FileOutputStream(outputFilename);
    webJar = new JarOutputStream(outFile);
    webJar.setLevel(9);

    // Copies the skeleton installer
    sendMsg("Copying the skeleton installer ...");
    writeSkeletonInstaller(webJar);
  }


  /**
   *  Adds a pack (the compiler sends the merged data).
   *
   * @param  packNumber     The pack number.
   * @param  name           The pack name.
   * @param  osConstraints  The target operation system(s) of this pack.
   * @param  required       Is the pack required ?
   * @param  description    The pack description.
   * @return                Description of the Return Value
   * @exception  Exception  Description of the Exception
   */
  public ZipOutputStream addPack(int packNumber, String name, List osConstraints, boolean required,
                                 String description, boolean preselected) throws Exception
  {
    sendMsg("Adding pack #" + packNumber + " : " + name + " ...");

    // Adds it in the packs array
    Pack pack = new Pack(name, description, osConstraints, required, preselected);
    packs.add(packNumber, pack);

    // Returns the suiting output stream
    String entryName = "packs/pack" + packNumber;
    ZipEntry entry = new ZipEntry(entryName);
    webJar.putNextEntry(entry);
    return webJar;
  }


  /**
   *  Sets the GUI preferences.
   *
   * @param  prefs          The new gUIPrefs value
   * @exception  Exception  Description of the Exception
   */
  public void setGUIPrefs(GUIPrefs prefs) throws Exception
  {
    sendMsg("Setting the GUI preferences ...");

    outJar.putNextEntry(new ZipEntry("GUIPrefs"));
    ObjectOutputStream objOut = new ObjectOutputStream(outJar);
    objOut.writeObject(prefs);
    objOut.flush();
    outJar.closeEntry();
  }


  /**
   *  Adds a panel.
   *
   * @param  classFilename  The class filename.
   * @param  input          The stream to get the file data from.
   * @exception  Exception  Description of the Exception
   */
  public void addPanelClass(String classFilename, InputStream input) throws Exception
  {
    sendMsg("Adding the (sub)classes for " + classFilename + " ...");

    outJar.putNextEntry(new ZipEntry("com/izforge/izpack/panels/" + classFilename));
    copyStream(input, outJar);
    outJar.closeEntry();
  }


  /**
   *  Sets the panels order.
   *
   * @param  order          The ordered list of the panels.
   * @exception  Exception  Description of the Exception
   */
  public void setPanelsOrder(ArrayList order) throws Exception
  {
	sendMsg("Setting the panels order ...");

	outJar.putNextEntry(new ZipEntry("panelsOrder"));
	ObjectOutputStream objOut = new ObjectOutputStream(outJar);
	objOut.writeObject(order);
	objOut.flush();
	outJar.closeEntry();
  }


  /**
   *  Sets the informations related to this installation.
   *
   * @param  info           The info section.
   * @exception  Exception  Description of the Exception
   */
  public void setInfo(Info info) throws Exception
  {
    sendMsg("Setting the installer informations ...");

    outJar.putNextEntry(new ZipEntry("info"));
    ObjectOutputStream objOut = new ObjectOutputStream(outJar);
    objOut.writeObject(info);
    objOut.flush();
    outJar.closeEntry();
  }


  /**
   *  Adds Variable Declaration.
   *
   * @param  varDef         The variables definitions.
   * @exception  Exception  Description of the Exception
   */
  public void setVariables(Properties varDef) throws Exception
  {
    sendMsg("Setting  the variables ...");
    outJar.putNextEntry(new ZipEntry("vars"));
    ObjectOutputStream objOut = new ObjectOutputStream(outJar);
    objOut.writeObject(varDef);
    objOut.flush();
    outJar.closeEntry();
  }


  /**
   *  Adds a resource.
   *
   * @param  resId          The resource Id.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public void addResource(String resId, InputStream input) throws Exception
  {
    sendMsg("Adding resource : " + resId + " ...");

    outJar.putNextEntry(new ZipEntry("res/" + resId));
    copyStream(input, outJar);
    outJar.closeEntry();
    input.close();
  }


  /**
   *  Adds a native library.
   *
   * @param  name           The native library name.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public void addNativeLibrary(String name, InputStream input) throws Exception
  {
    sendMsg("Adding native library : " + name + " ...");

    outJar.putNextEntry(new ZipEntry("native/" + name));
    copyStream(input, outJar);
    outJar.closeEntry();
    input.close();
  }


  /**
   *  Adds a language pack.
   *
   * @param  iso3           The ISO3 code.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public void addLangPack(String iso3, InputStream input) throws Exception
  {
    sendMsg("Adding langpack : " + iso3 + " ...");

    langpacks.add(iso3);
    outJar.putNextEntry(new ZipEntry("langpacks/" + iso3 + ".xml"));
    copyStream(input, outJar);
    outJar.closeEntry();
    input.close();
  }



  /**
   *  Adds a jar file content to the installer.
   *
   * @param  file           The jar filename.
   * @exception  Exception  Description of the Exception
   */
  public void addJarContent(String file) throws Exception
  {
    sendMsg("Adding a jar file content ...");

    JarFile jar = new JarFile(file);
    Enumeration entries = jar.entries();
    while (entries.hasMoreElements())
    {
      // Puts a new entry
      ZipEntry zentry = (ZipEntry) entries.nextElement();
      try
      {
        InputStream zin = jar.getInputStream(zentry);
        outJar.putNextEntry(new ZipEntry(zentry.getName()));

        // Copy the data
        copyStream(zin, outJar);
        outJar.closeEntry();
        zin.close();
      }
      catch (ZipException zerr)
      {
      }
    }
  }


  /**
   *  Tells the packager to finish the job (misc writings, cleanups, closings ,
   *  ...).
   *
   * @exception  Exception  Description of the Exception
   */
  public void finish() throws Exception
  {
    // Usefull stuff
    DataOutputStream datOut;
    ObjectOutputStream objOut;
    int size;
    int i;

    sendMsg("Finishing the enpacking ...");

    // Writes the installation kind information
    outJar.putNextEntry(new ZipEntry("kind"));
    datOut = new DataOutputStream(outJar);
    datOut.writeUTF("web");
    datOut.flush();
    outJar.closeEntry();

    // Writes the packs informations
    outJar.putNextEntry(new ZipEntry("packs.info"));
    objOut = new ObjectOutputStream(outJar);
    size = packs.size();
    objOut.writeInt(size);
    for (i = 0; i < size; i++)
      objOut.writeObject(packs.get(i));
    objOut.flush();
    outJar.closeEntry();

    // Writes the langpacks informations
    outJar.putNextEntry(new ZipEntry("langpacks.info"));
    datOut = new DataOutputStream(outJar);
    size = langpacks.size();
    datOut.writeInt(size);
    for (i = 0; i < size; i++)
      datOut.writeUTF((String) langpacks.get(i));
    datOut.flush();
    outJar.closeEntry();

    // Closes the stream
    outJar.flush();
    outJar.close();
    webJar.flush();
    webJar.close();

    sendStop();
  }
}

