/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               Packager.java
 *  Description :        Packs files and data into an installer
 *  Author's email :     julien@izforge.com
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.CustomActionData;
import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.Panel;

/**
 * The packager class. The packager is used by the compiler to put files into an
 * installer, and create the actual installer files.
 *
 * @author     Julien Ponge
 * @author     Chadwick McHenry
 */
public class Packager
{
  /** Path to the skeleton installer. */
  public static final String SKELETON_SUBPATH = "lib/installer.jar";

  /** Base file name of all jar files. This has no ".jar" suffix. */
  private File baseFile = null;

  /** Executable zipped output stream. First to open, last to close. */
  private JarOutputStream primaryJarStream;

  /** Basic installer info. */
  private Info info = null;

  /** Gui preferences of instatller. */
  private GUIPrefs guiPrefs = null;

  /** The variables used in the project */
  private Properties variables = new Properties();

  /** The ordered panels informations. */
  private List panelList = new ArrayList();

  /** The ordered packs informations (as PackInfo objects). */
  private List packsList = new ArrayList();

  /** The ordered langpack ISO3 names. */
  private List langpackNameList = new ArrayList();

  /** The ordered custom actions informations. */
  private List customActionsList = new ArrayList();

  /** The langpack URLs keyed by ISO3 name. */
  private Map installerResourceURLMap = new HashMap();

  /** Jar file URLs who's contents will be copied into the installer. */
  private Set includedJarURLs = new HashSet();

  /** Each pack is created in a separte jar if webDirURL is non-null. */
  private boolean packJarsSeparate = false;
  
  /** The listeners. */
  private PackagerListener listener;

  /** The constructor. */
  public Packager() {}

  /**
   * Create the installer, beginning with the specified jar. If the name
   * specified does not end in ".jar", it is appended. If secondary jars are
   * created for packs (if the Info object added has a webDirURL set), they are
   * created in the same directory, named sequentially by inserting ".pack#"
   * (where '#' is the pack number) ".jar" suffix: e.g. "foo.pack1.jar". If any
   * file exists, it is overwritten.
   */
  public void createInstaller(File primaryFile) throws IOException
  {
    // preliminary work
    String baseName = primaryFile.getName();
    if (baseName.endsWith(".jar"))
    {
      baseName = baseName.substring(0, baseName.length()-4);
      baseFile = new File(primaryFile.getParentFile(), baseName);
    }
    else
      baseFile = primaryFile;
    
    info.setInstallerBase(baseFile.getName());
    packJarsSeparate = (info.getWebDirURL() != null);
    
    // primary (possibly only) jar. -1 indicates primary
    primaryJarStream = getJarOutputStream(baseFile.getName() + ".jar");

    sendStart();

    // write the primary jar. MUST be first so manifest is not overwritten by
    // an included jar
    writeSkeletonInstaller();

    writeInstallerObject("info", info);
    writeInstallerObject("vars", variables);
    writeInstallerObject("GUIPrefs", guiPrefs);
    writeInstallerObject("panelsOrder", panelList);
    writeInstallerObject("customActions", customActionsList);
    writeInstallerObject("langpacks.info", langpackNameList);
    writeInstallerResources();
    writeIncludedJars();

    // Pack File Data  may be written to separate jars
    writePacks();
    
    // finish up
    primaryJarStream.close();

    sendStop();
  }

  /* **********************************************************************
   * Listener assistance
   * **********************************************************************/

  /**
   * Adds a listener.
   *
   * @param  listener  The listener.
   */
  public void setPackagerListener(PackagerListener listener)
  {
    this.listener = listener;
  }

  /**
   * Dispatches a message to the listeners.
   *
   * @param  job  The job description.
   */
  private void sendMsg(String job)
  {
    if (listener != null)
      listener.packagerMsg(job);
  }

  /** Dispatches a start event to the listeners.  */
  private void sendStart()
  {
    if (listener != null)
      listener.packagerStart();
  }

  /** Dispatches a stop event to the listeners.  */
  private void sendStop()
  {
    if (listener != null)
      listener.packagerStop();
  }


  /* **********************************************************************
   * Public methods to add data to the Installer being packed
   * **********************************************************************/

  /**
   * Sets the informations related to this installation.
   *
   * @param  info           The info section.
   * @exception  Exception  Description of the Exception
   */
  public void setInfo(Info info) throws Exception
  {
    sendMsg("Setting the installer informations ...");
    this.info = info;
  }

  /**
   * Sets the GUI preferences.
   *
   * @param  prefs          The new gUIPrefs value
   * @exception  Exception  Description of the Exception
   */
  public void setGUIPrefs(GUIPrefs prefs)
  {
    sendMsg("Setting the GUI preferences ...");
    guiPrefs = prefs;
  }

  /**
   * Allows access to add, remove and update the variables for the project,
   * which are maintained in the packager.
   *
   * @return map of variable names to values
   */
  public Properties getVariables()
  {
    return variables;
  }

  /**
   * Add a panel, where order is important. Only one copy of the class files
   * neeed are inserted in the installer.
   */
  public void addPanelJar(Panel panel, URL jarURL )
  {
    panelList.add(panel);  // serialized to keep order/variables correct
    addJarContent(jarURL); // each included once, no matter how many times added
  }


  /**
   * Add a custom action, where order is important. Only one copy of the class files
   * neeed are inserted in the installer.
   * @param ca custom action object
   * @param url the URL to include once
   */
  public void addCustomActionJar(CustomActionData ca, URL url)
  {
    customActionsList.add(ca);  // serialized to keep order/variables correct
    addJarContent(url); // each included once, no matter how many times added
  }
  /**
   * Adds a pack, order is mostly irrelevant.
   *
   * @param  pack contains all the files and items that go with a pack
   */
  public void addPack(PackInfo pack)
  {
    packsList.add(pack);
  }

  /**
   * Adds a language pack.
   *
   * @param  iso3           The ISO3 code.
   * @param  xmlURL         The location of the xml local info
   * @param  flagURL        The location of the flag image resource
   * @exception  Exception  Description of the Exception
   */
  public void addLangPack(String iso3, URL xmlURL, URL flagURL)
  {
    sendMsg("Adding langpack : " + iso3 + " ...");
    // put data & flag as entries in installer, and keep array of iso3's names
    langpackNameList.add(iso3);
    addResource("flag." + iso3, flagURL);
    installerResourceURLMap.put("langpacks/" + iso3 + ".xml", xmlURL);
  }

  /**
   * Adds a resource.
   *
   * @param  resId          The resource Id.
   * @param  url            The location of the data
   * @exception  Exception  Description of the Exception
   */
  public void addResource(String resId, URL url)
  {
    sendMsg("Adding resource : " + resId + " ...");
    installerResourceURLMap.put("res/" + resId, url);
  }

  /**
   * Adds a native library.
   *
   * @param  name           The native library name.
   * @param  url            The url to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public void addNativeLibrary(String name, URL url) throws Exception
  {
    sendMsg("Adding native library : " + name + " ...");
    installerResourceURLMap.put("native/" + name, url);
  }

  /**
   * Adds a jar file content to the installer. Package structure is
   * maintained. Need mechanism to copy over signed entry information.
   *
   * @param jarURL          The url of the jar to add to the installer. We use
   *                        a URL so the jar may be nested within another.
   */
  public void addJarContent(URL jarURL)
  {
    sendMsg("Adding content of jar : " + jarURL.getFile() + " ...");
    includedJarURLs.add(jarURL);
  }


  /* **********************************************************************
   * Private methods used when writing out the installer to jar files.
   * **********************************************************************/

  /**
   * Write skeleton installer to primary jar. It is just an included jar,
   * except that we copy the META-INF as well.
   */
  private void writeSkeletonInstaller() throws IOException
  {
    sendMsg("Copying the skeleton installer ...");

    InputStream is = Packager.class.getResourceAsStream("/" + SKELETON_SUBPATH);
    if (is == null)
    {
      File skeleton = new File(Compiler.IZPACK_HOME, SKELETON_SUBPATH);
      is = new FileInputStream(skeleton);
    }
    ZipInputStream inJarStream = new ZipInputStream(is);
    copyZip(inJarStream, primaryJarStream);
  }

  /**
   * Write an arbitrary object to primary jar.
   */
  private void writeInstallerObject(String entryName, Object object)
    throws IOException
  {
    primaryJarStream.putNextEntry(new ZipEntry(entryName));
    ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
    out.writeObject(object);
    out.flush();
    primaryJarStream.closeEntry();
  }

  /** Write the data referenced by URL to primary jar. */
  private void writeInstallerResources() throws IOException
  {
    sendMsg("Copying " + installerResourceURLMap.size() +
            " files into installer ...");
    
    Iterator i = installerResourceURLMap.keySet().iterator();
    while (i.hasNext())
    {
      String name = (String) i.next();
      InputStream in = ((URL)installerResourceURLMap.get(name)).openStream();
      primaryJarStream.putNextEntry(new ZipEntry(name));
      copyStream(in, primaryJarStream);
      primaryJarStream.closeEntry();
      in.close();
    }
  }

  /** Copy included jars to primary jar. */
  private void writeIncludedJars() throws IOException
  {
    sendMsg("Copying contents of " + includedJarURLs.size() +
            " jars into installer ...");
    
    Iterator i = includedJarURLs.iterator();
    while (i.hasNext())
    {
      InputStream is = ((URL)i.next()).openStream();
      ZipInputStream inJarStream = new ZipInputStream(is);
      copyZip(inJarStream, primaryJarStream);
    }
  }

  /**
   * Write Packs to primary jar or each to a separate jar.
   */
  private void writePacks() throws IOException
  {
    sendMsg("Writing Packs ...");
    
    // Map to remember pack number and bytes offsets of back references
    Map storedFiles = new HashMap();
    
    // First write the serialized files and file metadata data for each pack
    // while counting bytes.
    
    int packNumber = 0;
    Iterator packIter = packsList.iterator();
    while (packIter.hasNext())
    {
      PackInfo packInfo = (PackInfo) packIter.next();
      Pack pack = packInfo.getPack();
      pack.nbytes = 0;

      // create a pack specific jar if required
      JarOutputStream packStream = primaryJarStream;
      if (packJarsSeparate)
      {
        // See installer.Unpacker#getPackAsStream for the counterpart
        String name = baseFile.getName() + ".pack" + packNumber + ".jar";
        packStream = getJarOutputStream(name);
      }
      
      sendMsg("Writing Pack #" + packNumber + " : " + pack.name);

      // Retrieve the correct output stream
      ZipEntry entry = new ZipEntry("packs/pack" + packNumber);
      packStream.putNextEntry(entry);
      packStream.flush(); // flush before we start counting
      
      ByteCountingOutputStream dos = new ByteCountingOutputStream(packStream);
      ObjectOutputStream objOut = new ObjectOutputStream(dos);

      // We write the actual pack files
      long packageBytes = 0;
      objOut.writeInt(packInfo.getPackFiles().size());
      
      Iterator iter = packInfo.getPackFiles().iterator();
      while (iter.hasNext())
      {
        boolean addFile = !pack.loose;
        PackFile pf = (PackFile) iter.next();
        File file = packInfo.getFile(pf);

        // use a back reference if file was in previous pack, and in same jar
        long[] info = (long[]) storedFiles.get(file);
        if (info != null && ! packJarsSeparate)
        {
          pf.setPreviousPackFileRef((int) info[0], info[1]);
          addFile = false;
        }
        
        objOut.writeObject(pf); // base info
        objOut.flush(); //make sure it is written

        if (addFile && ! pf.isDirectory())
        {
          long pos = dos.getByteCount(); //get the position

          FileInputStream inStream = new FileInputStream(file);
          long bytesWritten = copyStream(inStream, objOut);

          if (bytesWritten != pf.length())
            throw new IOException("File size mismatch when reading " + file);

          inStream.close();
          storedFiles.put(file, new long[] { packNumber, pos });
        }
        
        // even if not written, it counts towards pack size
        pack.nbytes += pf.length();
      }

      // Write out information about parsable files
      objOut.writeInt(packInfo.getParsables().size());
      iter = packInfo.getParsables().iterator();
      while (iter.hasNext())
        objOut.writeObject(iter.next());

      // Write out information about executable files
      objOut.writeInt(packInfo.getExecutables().size());
      iter = packInfo.getExecutables().iterator();
      while (iter.hasNext())
        objOut.writeObject(iter.next());

      // Write out information about updatecheck files
      objOut.writeInt(packInfo.getUpdateChecks().size());
      iter = packInfo.getUpdateChecks().iterator();
      while (iter.hasNext())
        objOut.writeObject(iter.next());

      // Cleanup
      objOut.flush();
      packStream.closeEntry();

      // close pack specific jar if required
      if (packJarsSeparate)
        packStream.close();
      
      packNumber++;
    }

    // Now that we know sizes, write pack metadata to primary jar.
    primaryJarStream.putNextEntry(new ZipEntry("packs.info"));
    ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
    out.writeInt(packsList.size());

    Iterator i = packsList.iterator();
    while (i.hasNext())
    {
      PackInfo pack = (PackInfo) i.next();
      out.writeObject(pack.getPack());
    }
    out.flush();
    primaryJarStream.closeEntry();
  }
  

  /* **********************************************************************
   * Stream utilites for creation of the installer.
   * **********************************************************************/

  /** Return a stream for the next jar. */
  private JarOutputStream getJarOutputStream(String name) throws IOException
  {
    File file = new File(baseFile.getParentFile(), name);
    sendMsg("Building installer jar: " + file.getAbsolutePath());

    JarOutputStream jar = new JarOutputStream(new FileOutputStream(file));
    jar.setLevel(Deflater.BEST_COMPRESSION);
    
    return jar;
  }

  /**
   * Copies contents of one jar to another.
   *
   * <p>TODO: it would be useful to be able to keep signature information from
   * signed jar files, can we combine manifests and still have their content
   * signed?
   *
   * @see #copyStream(InputStream, OutputStream)
   */
  private void copyZip(ZipInputStream zin, ZipOutputStream out)
    throws IOException
  {
    ZipEntry zentry;
    while ( (zentry = zin.getNextEntry()) != null)
    {
      try
      {
        out.putNextEntry(new ZipEntry(zentry.getName()));
        copyStream(zin, out);
        out.closeEntry();
        zin.closeEntry();
      } catch (ZipException x)
      {
        // This avoids any problem that can occur with duplicate
        // directories. for instance all META-INF data in jars
      }
    }
  }

  /**
   *  Copies all the data from the specified input stream to the specified
   *  output stream.
   *
   * @param  in               the input stream to read
   * @param  out              the output stream to write
   * @return                  the total number of bytes copied
   * @exception  IOException  if an I/O error occurs
   */
  private long copyStream(InputStream in, OutputStream out)
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
}
