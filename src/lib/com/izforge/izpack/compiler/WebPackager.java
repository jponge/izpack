/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
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
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.Pack;

/**
 *  Web packager class.
 *
 * @author     Julien Ponge
 */
public class WebPackager extends Packager
{
  /**  The web jar file output stream. */
  protected JarOutputStream webJar;

  /**
   *  The constructor.
   *
   * @param  outputFilename  The output filename.
   * @param  plistener       The packager listener.
   * @exception  Exception   Description of the Exception
   */
  public WebPackager(String outputFilename, PackagerListener plistener)
    throws Exception
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
    outputFilename =
      outputFilename.substring(0, outputFilename.length() - 4) + "_web.jar";
    outFile = new FileOutputStream(outputFilename);
    webJar = new JarOutputStream(outFile);
    webJar.setLevel(9);

    // Copies the skeleton installer
    sendMsg("Copying the skeleton installer ...");
    writeSkeletonInstaller(outJar);
  }

  public boolean allowPackFileBackReferences()
  {
    return false;
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
  public ZipOutputStream addPack(
    int packNumber,
    String name,
    List osConstraints,
    boolean required,
    String description,
    boolean preselected)
    throws Exception
  {
    sendMsg("Adding pack #" + packNumber + " : " + name + " ...");

    // Adds it in the packs array
    Pack pack =
      new Pack(name, description, osConstraints, required, preselected);
    packs.add(packNumber, pack);

    // Returns the suiting output stream
    String entryName = "packs/pack" + packNumber;
    ZipEntry entry = new ZipEntry(entryName);
    webJar.putNextEntry(entry);
    return webJar;
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
