/*
 * $Id$
 * IzPack
 * Copyright (C) 2001-2004 Julien Ponge
 * 
 * File : StdKunststoffPackager.java
 * Description : The standard Kunststoff installer packager class.
 * Author's email : julien@izforge.com
 * Author's Website : http://www.izforge.com
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The standard installer class, using the Kunststoff L&F.
 * 
 * @author Julien Ponge
 */
public class StdKunststoffPackager extends StdPackager
{

  /**
   * The constructor.
   * 
   * @param outputFilename The output filename.
   * @param plistener The packager listener.
   * @exception Exception Description of the Exception
   */
  public StdKunststoffPackager(String outputFilename, PackagerListener plistener)
      throws Exception
  {
    super(outputFilename, plistener);

    // Copies the Kunststoff library
    sendMsg("Copying the Kunststoff library ...");
    InputStream istream = getClass().getResourceAsStream("/lib/kunststoff.jar");
    ZipInputStream skeleton_is;
    if (istream != null)
    {
      skeleton_is = new ZipInputStream(istream);
    }
    else
    {
      skeleton_is = new JarInputStream(new FileInputStream(Compiler.IZPACK_HOME
          + "lib" + File.separator + "kunststoff.jar"));
    }

    ZipEntry zentry;

    while ((zentry = skeleton_is.getNextEntry()) != null)
    {
      // ugly hack: may not add a directory twice, therefore add no directories
      if (zentry.isDirectory()) continue;

      // Puts a new entry
      outJar.putNextEntry(new ZipEntry(zentry.getName()));

      // Copy the data
      copyStream(skeleton_is, outJar);

      outJar.closeEntry();
      skeleton_is.closeEntry();
    }

  }

  /**
   * Tells the packager to finish the job.
   * 
   * @exception Exception Description of the Exception
   */
  public void finish() throws Exception
  {
    sendMsg("Finishing the enpacking ...");
    writeInstallationKind("standard-kunststoff");
    writePacksInfo();
    writeLangPacksInfo();
    closeStream();
    sendStop();
  }
}