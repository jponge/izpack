/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               Unpacker.java
 *  Description :        The unpacker class.
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
package com.izforge.izpack.installer;

import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

/**
 *  Unpacker class.
 *
 * @author     Julien Ponge
 * @author     Johannes Lehtinen
 * @created    October 27, 2002
 */
public class Unpacker extends Thread
{
  /**  The installdata. */
  private InstallData idata;

  /**  The installer listener. */
  private InstallListener listener;

  /**  The uninstallation data. */
  private UninstallData udata;

  /**  The jar location. */
  private String jarLocation;

  /**  The variables substitutor. */
  private VariableSubstitutor vs;

  /**  The instances of the unpacker objects. */
  private static ArrayList instances = new ArrayList();


  /**
   *  The constructor.
   *
   * @param  idata     The installation data.
   * @param  listener  The installation listener.
   */
  public Unpacker(InstallData idata, InstallListener listener)
  {
    super("IzPack - Unpacker thread");

    this.idata = idata;
    this.listener = listener;

    // Initialize the variable substitutor
    vs = new VariableSubstitutor(idata.getVariableValueMap());
  }


  /**
   *  Returns the active unpacker instances.
   *
   * @return    The active unpacker instances.
   */
  public static ArrayList getRunningInstances()
  {
    return instances;
  }


  /**  The run method.  */
  public void run()
  {
    instances.add(this);
    try
    {
      listener.startUnpack();
      String currentOs = System.getProperty("os.name").toLowerCase();
      //
      // Initialisations
      FileOutputStream out = null;
      ArrayList parsables = new ArrayList();
      ArrayList executables = new ArrayList();
      List packs = idata.selectedPacks;
      int npacks = packs.size();
      udata = UninstallData.getInstance();

      // Specific to the web installers
      if (idata.kind.equalsIgnoreCase("web") ||
        idata.kind.equalsIgnoreCase("web-kunststoff"))
      {
        InputStream kin = getClass().getResourceAsStream("/res/WebInstallers.url");
        BufferedReader kreader = new BufferedReader(new InputStreamReader(kin));
        jarLocation = kreader.readLine();
      }

      // We unpack the selected packs
      for (int i = 0; i < npacks; i++)
      {
        // We get the pack stream
        int n = idata.availablePacks.indexOf(packs.get(i));
        ObjectInputStream objIn
           = new ObjectInputStream(getPackAsStream(n));

        // We unpack the files
        int nfiles = objIn.readInt();
        listener.changeUnpack(0, nfiles, ((Pack) packs.get(i)).name);
        for (int j = 0; j < nfiles; j++)
        {
          // We read the header
          PackFile pf = (PackFile) objIn.readObject();
          if (null == pf.os || currentOs.indexOf(pf.os.toLowerCase()) > -1)
          {
            // We translate & build the path
            String path = translatePath(pf.targetPath);
            File pathFile = new File(path);
            String fname = pathFile.getName();
            int z = fname.length();
            File dest = pathFile.getParentFile();
            if (!dest.exists())
              dest.mkdirs();

            // We add the path to the log,
            udata.addFile(path);

            listener.progressUnpack(j, path);

            //if this file exists and shouldnot override skip this file
            if (((pf.override == false) && (pathFile.exists())))
            {
              objIn.skip(pf.length);
              continue;
            }

            // We copy the file
            out = new FileOutputStream(path);
            byte[] buffer = new byte[5120];
            long bytesCopied = 0;
            while (bytesCopied < pf.length)
            {
              int maxBytes =
                (pf.length - bytesCopied < buffer.length ?
                (int) (pf.length - bytesCopied) : buffer.length);
              int bytesInBuffer = objIn.read(buffer, 0, maxBytes);
              if (bytesInBuffer == -1)
                throw new IOException("Unexpected end of stream");

              out.write(buffer, 0, bytesInBuffer);

              bytesCopied += bytesInBuffer;
            }
            // Cleanings
            out.close();

            // Empty dirs restoring
            String _n = pathFile.getName();
            if (_n.startsWith("izpack-keepme") && _n.endsWith(".tmp"))
              pathFile.delete();

          }
          else
            objIn.skip(pf.length);

        }

        // Load information about parsable files
        int numParsables = objIn.readInt();
        int k;
        for (k = 0; k < numParsables; k++)
        {
          ParsableFile pf = (ParsableFile) objIn.readObject();
          pf.path = translatePath(pf.path);
          parsables.add(pf);
        }

        // Load information about executable files
        int numExecutables = objIn.readInt();
        for (k = 0; k < numExecutables; k++)
        {
          ExecutableFile ef = (ExecutableFile) objIn.readObject();
          ef.path = translatePath(ef.path);
          if (null != ef.argList && !ef.argList.isEmpty())
          {
            String arg = null;
            for (int j = 0; j < ef.argList.size(); j++)
            {
              arg = (String) ef.argList.get(j);
              arg = translatePath(arg);
              ef.argList.set(j, arg);
            }
          }
          executables.add(ef);
          if(ef.executionStage == ExecutableFile.UNINSTALL)
          {
            udata.addExecutable(ef);
          }
        }
        objIn.close();
      }

      // We use the scripts parser
      ScriptParser parser = new ScriptParser(parsables, vs);
      parser.parseFiles();

      // We use the file executor
      FileExecutor executor = new FileExecutor(executables);
      if (executor.executeFiles(ExecutableFile.POSTINSTALL) != 0)
        javax.swing.JOptionPane.showMessageDialog(
          null,
          "The installation was not completed.",
          "Installation warning",
          javax.swing.JOptionPane.WARNING_MESSAGE);

      // We put the uninstaller
      putUninstaller();

      // The end :-)
      listener.stopUnpack();
    }
    catch (Exception err)
    {
      listener.stopUnpack();
      listener.errorUnpack(err.toString());
    }
    instances.remove(instances.indexOf(this));
  }


  /**
   *  Puts the uninstaller.
   *
   * @exception  Exception  Description of the Exception
   */
  private void putUninstaller() throws Exception
  {
    // Me make the .uninstaller directory
    String dest = translatePath("$INSTALL_PATH") + File.separator +
      "Uninstaller";
    String jar = dest + File.separator + "uninstaller.jar";
    File pathMaker = new File(dest);
    pathMaker.mkdirs();

    // We log the uninstaller deletion information
    UninstallData udata = UninstallData.getInstance();
    udata.setUninstallerJarFilename(jar);
    udata.setUninstallerPath(dest);

    // We open our final jar file
    FileOutputStream out = new FileOutputStream(jar);
    ZipOutputStream outJar = new ZipOutputStream(out);
    idata.uninstallOutJar = outJar;
    outJar.setLevel(9);
    udata.addFile(jar);

    // We copy the uninstaller
    InputStream in = getClass().getResourceAsStream("/res/IzPack.uninstaller");
    ZipInputStream inRes = new ZipInputStream(in);
    ZipEntry zentry = inRes.getNextEntry();
    while (zentry != null)
    {
      // Puts a new entry
      outJar.putNextEntry(new ZipEntry(zentry.getName()));

      // Byte to byte copy
      int unc = inRes.read();
      while (unc != -1)
      {
        outJar.write(unc);
        unc = inRes.read();
      }

      // Next one please
      inRes.closeEntry();
      outJar.closeEntry();
      zentry = inRes.getNextEntry();
    }
    inRes.close();

    // We put the langpack
    in = getClass().getResourceAsStream("/langpacks/" + idata.localeISO3 + ".xml");
    outJar.putNextEntry(new ZipEntry("langpack.xml"));
    int read = in.read();
    while (read != -1)
    {
      outJar.write(read);
      read = in.read();
    }
    outJar.closeEntry();
  }


  /**
   *  Returns a stream to a pack, depending on the installation kind.
   *
   * @param  n              The pack number.
   * @return                The stream.
   * @exception  Exception  Description of the Exception
   */
  private InputStream getPackAsStream(int n) throws Exception
  {
    InputStream in = null;

    if (idata.kind.equalsIgnoreCase("standard") ||
      idata.kind.equalsIgnoreCase("standard-kunststoff"))
      in = getClass().getResourceAsStream("/packs/pack" + n);

    else
      if (idata.kind.equalsIgnoreCase("web") ||
      idata.kind.equalsIgnoreCase("web-kunststoff"))
    {
      URL url = new URL("jar:" + jarLocation + "!/packs/pack" + n);
      JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
      in = jarConnection.getInputStream();
    }
    return in;
  }


  /**
   *  Translates a relative path to a local system path.
   *
   * @param  destination  The path to translate.
   * @return              The translated path.
   */
  private String translatePath(String destination)
  {
    // Parse for variables
    destination = vs.substitute(destination, null);

    // Convert the file separator characters
    return destination.replace('/', File.separatorChar);
  }
}

