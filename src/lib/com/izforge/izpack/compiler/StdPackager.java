/*
 * $Id$ IzPack
 * Copyright (C) 2001-2004 Julien Ponge
 * 
 * File : StdPackager.java Description : The standard installer packager class.
 * Author's email : julien@izforge.com Author's Website : http://www.izforge.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.izforge.izpack.compiler;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.Pack;

/**
 * Standard packager.
 * 
 * @author Julien Ponge
 */
public class StdPackager extends Packager
{

  /**
   * The constructor.
   * 
   * @param outputFilename The output filename.
   * @param plistener The packager listener.
   * @exception Exception Description of the Exception
   */
  public StdPackager(String outputFilename, PackagerListener plistener)
      throws Exception
  {
    packs = new ArrayList();
    langpacks = new ArrayList();
    setPackagerListener(plistener);

    sendStart();

    // Sets up the zipped output stream
    FileOutputStream outFile = new FileOutputStream(outputFilename);
    outJar = new JarOutputStream(outFile);
    outJar.setLevel(Deflater.BEST_COMPRESSION);

    // Copies the skeleton installer
    sendMsg("Copying the skeleton installer ...");
    writeSkeletonInstaller(outJar);
  }

  public boolean allowPackFileBackReferences()
  {
    return true;
  }

  /**
   * Adds a pack (the compiler sends the merged data).
   * 
   * @param packNumber The pack number.
   * @param name The pack name.
   * @param required Is the pack required ?
   * @param osConstraints The target operation system(s) of this pack.
   * @param description The pack description.
   * @return Description of the Return Value
   * @exception Exception Description of the Exception
   */
  public ZipOutputStream addPack(int packNumber, String name,
      List osConstraints, boolean required, String description,
      boolean preselected) throws Exception
  {
    sendMsg("Adding pack #" + packNumber + " : " + name + " ...");

    // Adds it in the packs array
    Pack pack = new Pack(name, description, osConstraints, required,
        preselected);
    packs.add(packNumber, pack);

    // Returns the suiting output stream
    String entryName = "packs/pack" + packNumber;
    ZipEntry entry = new ZipEntry(entryName);
    outJar.putNextEntry(entry);
    return outJar;
  }

  /**
   * Tells the packager to finish the job (misc writings, cleanups, closings ,
   * ...).
   * 
   * @exception Exception Description of the Exception
   */
  public void finish() throws Exception
  {
    sendMsg("Finishing the enpacking ...");
    writeInstallationKind("standard");
    writePacksInfo();
    writeLangPacksInfo();
    closeStream();
    sendStop();
  }

  /**
   * Writes the installation kind.
   * 
   * @param kind The installation kind.
   * @throws IOException
   */
  protected void writeInstallationKind(String kind) throws IOException
  {
    DataOutputStream datOut;
    outJar.putNextEntry(new ZipEntry("kind"));
    datOut = new DataOutputStream(outJar);
    datOut.writeUTF(kind);
    datOut.flush();
    outJar.closeEntry();
  }

  /**
   * Closes the Jar stream.
   * 
   * @throws IOException
   */
  protected void closeStream() throws IOException
  {
    outJar.flush();
    outJar.close();
  }

  /**
   * Writes the langpacks informations.
   * 
   * @throws IOException
   */
  protected void writeLangPacksInfo() throws IOException
  {
    DataOutputStream datOut;
    int size;
    int i;

    outJar.putNextEntry(new ZipEntry("langpacks.info"));
    datOut = new DataOutputStream(outJar);
    size = langpacks.size();
    datOut.writeInt(size);
    for (i = 0; i < size; i++)
      datOut.writeUTF((String) langpacks.get(i));
    datOut.flush();
    outJar.closeEntry();
  }

  /**
   * Writes the packs informations.
   * 
   * @throws IOException
   */
  protected void writePacksInfo() throws IOException
  {
    ObjectOutputStream objOut;
    int size;
    int i;

    outJar.putNextEntry(new ZipEntry("packs.info"));
    objOut = new ObjectOutputStream(outJar);
    size = packs.size();
    objOut.writeInt(size);
    for (i = 0; i < size; i++)
      objOut.writeObject(packs.get(i));
    objOut.flush();
    outJar.closeEntry();
  }
}