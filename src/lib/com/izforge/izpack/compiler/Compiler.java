/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               Compiler.java
 *  Description :        The IzPack compiler.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (c) 2001 Johannes Lehtinen
 *  johannes.lehtinen@iki.fi
 *  http://www.iki.fi/jle/
 *
 *  Portions are Copyright (c) 2002 Paul Wilkinson
 *  paulw@wilko.com
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

import net.n3.nanoxml.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.izforge.izpack.*;

import org.apache.tools.ant.DirectoryScanner;
import com.izforge.izpack.installer.VariableValueMapImpl;
import com.izforge.izpack.installer.VariableSubstitutor;

/**
 *  The IzPack compiler class.
 *
 * @author     Julien Ponge
 * created    October 26, 2002
 */
public class Compiler extends Thread
{
  /**  The compiler version. */
  public final static String VERSION = "1.0";

  /**  The IzPack version. */
  public final static String IZPACK_VERSION = "3.0.8a (build 2003.05.02)";

  /**  Standard installer. */
  public final static String STANDARD = "standard";

  /**  Standard-Kunststoff installer. */
  public final static String STANDARD_KUNSTSTOFF = "standard-kunststoff";

  /**  Web installer. */
  public final static String WEB = "web";

  /**  Web-Kunsstoff installer. */
  public final static String WEB_KUNSTSTOFF = "web-kunststoff";

  /**  The IzPack home directory. */
  public static String IZPACK_HOME = ".";

  /**  The XML filename. */
  protected String filename;

  /**  The base directory. */
  protected String basedir;

  /**  The installer kind. */
  protected String kind;

  /**  The output jar filename. */
  protected String output;

  /**  The packager listener. */
  protected PackagerListener packagerListener;

  /**  The variables map. */
  protected VariableValueMapImpl varMap;

  /** The directory-keeping special file. */
  protected File keepDirFile;


  /**
   *  The constructor.
   *
   * @param  filename  The XML filename.
   * @param  basedir   The base directory.
   * @param  kind      The installer kind.
   * @param  output    The installer filename.
   */
  public Compiler(String filename, String basedir, String kind, String output)
  {
    // Default initialisation
    this.filename = filename;
    this.basedir = basedir;
    this.kind = kind;
    this.output = output;

    // Creates a temporary temp file for keeping empty directories
    try
    {
      keepDirFile = File.createTempFile("izpack-keepme", ".tmp");
      keepDirFile.deleteOnExit();
    }
    catch (Exception err)
    {
      err.printStackTrace();
    }
  }


  /**
   *  Sets the packager listener.
   *
   * @param  listener  The listener.
   */
  public void setPackagerListener(PackagerListener listener)
  {
    packagerListener = listener;
  }


  /**  Compiles. */
  public void compile()
  {
    start();
  }


  /**  The run() method. */
  public void run()
  {
    try
    {
      executeCompiler();// Execute the compiler - may send info to System.out
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }


  /**
   *  Compiles the installation.
   *
   * @exception  Exception  Description of the Exception
   */
  public void executeCompiler() throws Exception
  {

    // Usefull variables
    int i;
    String str;
    InputStream inStream;

    // We get the XML data tree
    XMLElement data = getXMLTree();

    // We get the Packager
    Packager packager = getPackager();

    // We add the variable declaration
    packager.setVariables(getVariables(data));

    // We add the info
    packager.setInfo(getInfo(data));

    // We add the GUIPrefs
    packager.setGUIPrefs(getGUIPrefs(data));

    // We add the language packs
    ArrayList langpacks = getLangpacksCodes(data);
    Iterator iter = langpacks.iterator();
    while (iter.hasNext())
    {
      str = (String) iter.next();

      // The language pack
      inStream = new FileInputStream(Compiler.IZPACK_HOME + "bin" + File.separator + "langpacks" +
        File.separator + "installer" + File.separator + str + ".xml");
      packager.addLangPack(str, inStream);

      // The flag
      inStream = new FileInputStream(Compiler.IZPACK_HOME + "bin" + File.separator + "langpacks" +
        File.separator + "flags" + File.separator + str + ".gif");
      packager.addResource("flag." + str, inStream);
    }

    // We add the resources
    ArrayList resources = getResources(data);
    iter = resources.iterator();

    while (iter.hasNext())
    {
      Resource res = (Resource) iter.next();
      if (res.parse)
      {
        if (null != varMap)
        {
          File resFile = new File(res.src);
          FileInputStream inFile = new FileInputStream(resFile);
          BufferedInputStream bin = new BufferedInputStream(inFile, 5120);

          File parsedFile = File.createTempFile("izpp", null, resFile.getParentFile());
          FileOutputStream outFile = new FileOutputStream(parsedFile);
          BufferedOutputStream bout = new BufferedOutputStream(outFile, 5120);

          VariableSubstitutor vs = new VariableSubstitutor(varMap);
          vs.substitute(bin, bout, res.type, res.encoding);
          bin.close();
          bout.close();
          inFile = new FileInputStream(parsedFile);
          packager.addResource(res.id, inFile);
          inFile.close();
          parsedFile.delete();
        }
        else
        {
          System.err.println("ERROR: no variable is defined. " + res.src + " is not parsed.");
          inStream = new FileInputStream(res.src);
          packager.addResource(res.id, inStream);
        }
      }
      else
      {
        inStream = new FileInputStream(res.src);
        packager.addResource(res.id, inStream);
      }
    }

    // We add the native libraries
    ArrayList natives = getNativeLibraries(data);
    iter = natives.iterator();
    while (iter.hasNext())
    {
      NativeLibrary nat = (NativeLibrary) iter.next();
      inStream = new FileInputStream(nat.path);
      packager.addNativeLibrary(nat.name, inStream);
    }

    // We add the additionnal jar files content
    ArrayList jars = getJars(data);
    iter = jars.iterator();
    while (iter.hasNext())
      packager.addJarContent((String) iter.next());

    // We add the panels
    ArrayList panels = getPanels(data);
    ArrayList panelsOrder = new ArrayList(panels.size());
    TreeSet panelsCache = new TreeSet();
    iter = panels.iterator();
    while (iter.hasNext())
    {
      // We locate the panel classes directory
      str = (String) iter.next();
      File dir = new File(Compiler.IZPACK_HOME + "bin" + File.separator + "panels" + File.separator + str);
      if (!dir.exists())
        throw new Exception(str + " panel does not exist");

      // We add the panel in the order array
      panelsOrder.add(str);

      // We add each file in the panel folder
      if (panelsCache.contains(str)) continue;
      panelsCache.add(str);
      File[] files = dir.listFiles();
      int nf = files.length;
      for (int j = 0; j < nf; j++)
      {
        if (files[j].isDirectory())
          continue;
        FileInputStream inClass = new FileInputStream(files[j]);
        packager.addPanelClass(files[j].getName(), inClass);
      }
    }

    // We set the panels order
    packager.setPanelsOrder(panelsOrder);

    // We add the packs
    i = 0;
    ArrayList packs = getPacks(data);
    iter = packs.iterator();
    while (iter.hasNext())
    {
      Pack pack = (Pack) iter.next();
      ZipOutputStream zipOut = packager.addPack(i++, pack.name, pack.os, pack.required,
        pack.description);
      ObjectOutputStream objOut = new ObjectOutputStream(zipOut);

      // We write the pack data
      objOut.writeInt(pack.packFiles.size());
      Iterator iter2 = pack.packFiles.iterator();
      long packageBytes = 0;
      while (iter2.hasNext())
      {
        // Initialisations
        PackSource p = (PackSource) iter2.next();
        File f = new File(p.src);
        FileInputStream in = new FileInputStream(f);
        long nbytes = f.length();

        if(!p.targetdir.endsWith(File.separator)) {
          p.targetdir = p.targetdir+File.separatorChar;
        }        
        String targetFilename = p.targetdir + f.getName();

        // pack paths in canonical (unix) form regardless of current host o/s:
        if('/' != File.separatorChar)
        {
            targetFilename = targetFilename.replace(File.separatorChar, '/');
        }

        // Writing
        objOut.writeObject(new PackFile(targetFilename, p.os, nbytes, p.override));
        byte[] buffer = new byte[5120];
        long bytesWritten = 0;
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1)
        {
          objOut.write(buffer, 0, bytesInBuffer);
          bytesWritten += bytesInBuffer;
        }
        if (bytesWritten != nbytes)
          throw new IOException
            ("File size mismatch when reading " + f);
        packageBytes += bytesWritten;
        in.close();
      }
      packager.packAdded(i - 1, packageBytes);

      // Write out information about parsable files
      objOut.writeInt(pack.parsables.size());
      iter2 = pack.parsables.iterator();
      while (iter2.hasNext())
        objOut.writeObject(iter2.next());

      // Write out information about executable files
      objOut.writeInt(pack.executables.size());
      iter2 = pack.executables.iterator();
      while (iter2.hasNext())
      {
        objOut.writeObject(iter2.next());
      }

      // Cleanup
      objOut.flush();
      zipOut.closeEntry();
    }

    // We ask the packager to finish
    packager.finish();
  }


  /**
   *  Returns the GUIPrefs.
   *
   * @param  data           The XML data.
   * @return                The GUIPrefs.
   * @exception  Exception  Description of the Exception
   */
  protected GUIPrefs getGUIPrefs(XMLElement data) throws Exception
  {
    // We get the XMLElement & the values
    XMLElement gp = data.getFirstChildNamed("guiprefs");
    Integer integer;
    GUIPrefs p = new GUIPrefs();
    p.resizable = gp.getAttribute("resizable").equalsIgnoreCase("yes");
    integer = new Integer(gp.getAttribute("width"));
    p.width = integer.intValue();
    integer = new Integer(gp.getAttribute("height"));
    p.height = integer.intValue();

    // We return the GUIPrefs
    return p;
  }


  /**
   *  Returns a list of the jar files to add.
   *
   * @param  data           The XML data.
   * @return                The jar files list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getJars(XMLElement data) throws Exception
  {
    // Initialisation
    ArrayList jars = new ArrayList();
    Vector v = data.getChildrenNamed("jar");

    // We add each jar to the list
    Iterator iter = v.iterator();
    while (iter.hasNext())
    {
      XMLElement el = (XMLElement) iter.next();
      jars.add(basedir + File.separator + el.getAttribute("src"));
    }

    // We return
    return jars;
  }


  /**
   *  Returns a list of the native libraries to add.
   *
   * @param  data           The XML data.
   * @return                The native libraries list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getNativeLibraries(XMLElement data) throws Exception
  {
    // Initialisation
    ArrayList natives = new ArrayList();
    Vector v = data.getChildrenNamed("native");

    // We add each native lib path to the list
    Iterator iter = v.iterator();
    while (iter.hasNext())
    {
      XMLElement el = (XMLElement) iter.next();
      NativeLibrary nat = new NativeLibrary();
      nat.path = IZPACK_HOME + "bin" + File.separator +
        "native" + File.separator +
        el.getAttribute("type") + File.separator +
        el.getAttribute("name");
      nat.name = el.getAttribute("name");
      natives.add(nat);
    }

    // We return the paths to the native libraries
    return natives;
  }


  /**
   *  Returns a list of the packs to add.
   *
   * @param  data           The XML data.
   * @return                The packs to add list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getPacks(XMLElement data) throws Exception
  {
    // Initialisation
    ArrayList packs = new ArrayList();
    XMLElement root = data.getFirstChildNamed("packs");

    // We process each pack markup
    int npacks = root.getChildrenCount();
    for (int i = 0; i < npacks; i++)
    {
      XMLElement el = root.getChildAtIndex(i);

      // Trivial initialisations
      Pack pack = new Pack();
      pack.number = i;
      pack.name = el.getAttribute("name");
      pack.os = el.getAttribute("os");
      pack.required = el.getAttribute("required").equalsIgnoreCase("yes");
      pack.description = el.getFirstChildNamed("description").getContent();

      // We get the parsables list
      Iterator iter = null;
      Vector children = el.getChildrenNamed("parsable");
      if (null != children && !children.isEmpty())
      {
        iter = children.iterator();
        while (iter.hasNext())
        {
          XMLElement p = (XMLElement) iter.next();
      String targetFile = p.getAttribute("targetfile");
          pack.parsables.add
            (new ParsableFile(targetFile,
            p.getAttribute("type", "plain"),
            p.getAttribute("encoding", null)));
        }
      }

      // We get the executables list
      children = el.getChildrenNamed("executable");
      if (null != children && !children.isEmpty())
      {
        iter = children.iterator();
        while (iter.hasNext())
        {
          XMLElement e = (XMLElement) iter.next();

          // when to execute this executable
          int executeOn = ExecutableFile.NEVER;
          String val = e.getAttribute("stage", "never");
          if ("postinstall".compareToIgnoreCase(val) == 0)
            executeOn = ExecutableFile.POSTINSTALL;
          else if ("uninstall".compareToIgnoreCase(val) == 0)
            executeOn = ExecutableFile.UNINSTALL;

          // main class  of this executable
          String executeClass = e.getAttribute("class");

          // type of this executable
          int executeType = ExecutableFile.BIN;
          val = e.getAttribute("type", "bin");
          if ("jar".compareToIgnoreCase(val) == 0)
            executeType = ExecutableFile.JAR;

          // what to do if execution fails
          int onFailure = ExecutableFile.ASK;
          val = e.getAttribute("failure", "ask");
          if ("abort".compareToIgnoreCase(val) == 0)
            onFailure = ExecutableFile.ABORT;
          else if ("warn".compareToIgnoreCase(val) == 0)
            onFailure = ExecutableFile.WARN;

          // get arguments for this executable
          ArrayList argList = new ArrayList();
          XMLElement args = e.getFirstChildNamed("args");
          if (null != args)
          {
            argList = new ArrayList();
            Iterator argIterator = args.getChildrenNamed("arg").iterator();
            while (argIterator.hasNext())
            {
              XMLElement arg = (XMLElement) argIterator.next();
              argList.add(arg.getAttribute("value"));
            }
          }

          // get os info on this executable
          ArrayList osList = new ArrayList();
          Iterator osIterator = e.getChildrenNamed("os").iterator();
          while (osIterator.hasNext())
          {
            XMLElement os = (XMLElement) osIterator.next();
            osList.add
              (new com.izforge.izpack.util.Os(os.getAttribute("family", null),
              os.getAttribute("name", null),
              os.getAttribute("version", null),
              os.getAttribute("arch", null)));
          }
      String targetFile = e.getAttribute("targetfile");
          pack.executables.add(new ExecutableFile(targetFile,
            executeType, executeClass,
            executeOn, onFailure, argList, osList));
        }
      }

      // We get the files list
      iter = el.getChildrenNamed("file").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();

	// Do not prepend basedir if already an absolute path
	String path = f.getAttribute("src");
	if (!new File(path).isAbsolute())
	   path = basedir + File.separator + path;
        File file = new File(path);

        boolean override = true;
        if (f.getAttribute("override") != null)
          override = f.getAttribute("override").equalsIgnoreCase("true");

        addFile(file,
          f.getAttribute("targetdir"),
          f.getAttribute("os"),
          override,
          pack.packFiles);
      }

      // We get the fileset list
      iter = el.getChildrenNamed("fileset").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();

	// Do not prepend basedir if already an absolute path
	String path = f.getAttribute("dir");
	if (!new File(path).isAbsolute())
	   path = basedir + File.separator + path;
        String casesensitive = f.getAttribute("casesensitive");
        //  get includes and excludes
        Vector xcludesList = f.getChildrenNamed("include");
        String[] includes = null;
        XMLElement xclude = null;
        if (xcludesList.size() > 0)
        {
          includes = new String[xcludesList.size()];
          for (int j = 0; j < xcludesList.size(); j++)
          {
            xclude = (XMLElement) xcludesList.get(j);
            includes[j] = xclude.getAttribute("name");
          }
        }

        xcludesList = f.getChildrenNamed("exclude");
        String[] excludes = null;
        xclude = null;
        if (xcludesList.size() > 0)
        {
          excludes = new String[xcludesList.size()];
          for (int j = 0; j < xcludesList.size(); j++)
          {
            xclude = (XMLElement) xcludesList.get(j);
            excludes[j] = xclude.getAttribute("name");
          }
        }

    String targetDir = f.getAttribute("targetdir");
        addFileSet(path, includes, excludes,
          targetDir,
          f.getAttribute("os"),
          pack.packFiles,
          casesensitive);
      }

      // We add the pack
      packs.add(pack);
    }

    // We return the ArrayList
    return packs;
  }


  /**
   *  Adds a Ant fileset.
   *
   * @param  path           The path.
   * @param  includes       The includes rules.
   * @param  excludes       The excludes rules.
   * @param  relPath        The relative path.
   * @param  targetOs       The target os.
   * @param  list           The files list.
   * @param  casesensitive  Case-sensitive stuff.
   * @exception  Exception  Description of the Exception
   */
  protected void addFileSet(String path, String[] includes, String[] excludes,
                            String relPath, String targetOs, ArrayList list, String casesensitive)
     throws Exception
  {
    boolean bCasesensitive = false;

    File test = new File(path);
    if (test.isDirectory())
    {
      if (casesensitive != null)
        bCasesensitive = casesensitive.equalsIgnoreCase("Yes");

      DirectoryScanner ds = new DirectoryScanner();
      ds.setIncludes(includes);
      ds.setExcludes(excludes);
      ds.setBasedir(new File(path));
      ds.setCaseSensitive(bCasesensitive);
      ds.scan();

      String[] files = ds.getIncludedFiles();
      String[] dirs = ds.getIncludedDirectories();

      /* Old buggy code
      String newRelativePath = null;

      String absolutBasePath = test.getParentFile().getAbsolutePath();
      String absolutPath = test.getAbsolutePath();
      String absolutFilePath = null;
      int copyPathFrom = absolutBasePath.length() + 1;
      for (int i = 0; i < files.length; i++)
      {
        File file = new File(absolutPath + File.separator + files[i]);

        absolutFilePath = file.getParentFile().getAbsolutePath();

        newRelativePath = relPath + File.separator + absolutFilePath.substring(copyPathFrom);
        //FIX ME: the override for fileset is by default true, needs to be changed
        addFile(file, newRelativePath, targetOs, true, list);
      }
      */

      // New working code (for files)
      String filePath, instPath, expPath;
      int pathLimit;
      File file;
      for (int i = 0; i < files.length; ++i)
      {
        filePath = path + File.separator + files[i];
        expPath = relPath + File.separator + files[i];
        file = new File(filePath);
        pathLimit = expPath.indexOf(file.getName());
        if (pathLimit > 0)
          instPath = expPath.substring(0, pathLimit);
        else
          instPath = relPath;
        addFile(file, instPath, targetOs, true, list);
      }

      // Empty directories are left by the previous code section, so we need to
      // take care of them
      for (int i = 0; i < dirs.length; ++i)
      {
        expPath = path + File.separator + dirs[i];
        File dir = new File(expPath);
        if (dir.list().length == 0)
        {
          instPath = relPath + File.separator + dirs[i];
          pathLimit = instPath.indexOf(dir.getName());
          instPath = instPath.substring(0, pathLimit);
          addFile(dir, instPath, targetOs, true, list);
        }
      }
    }
    else
      throw new Exception("\"dir\" attribute of fileset is not valid: " + path);
  }


  /**
   *  Recursive method to add files in a pack.
   *
   * @param  file           The file to add.
   * @param  relPath        The relative path.
   * @param  targetOs       The target OS.
   * @param  override       Overriding behaviour.
   * @param  list           The files list.
   * @exception  Exception  Description of the Exception
   */
  protected void addFile(File file, String relPath, String targetOs,
                         boolean override, ArrayList list) throws Exception
  {
    // We check if 'file' is correct
    if (!file.exists())
      throw new Exception(file.toString() + " does not exist");

    // Recursive part
    if (file.isDirectory())
    {
      File[] files = file.listFiles();
      if (files.length == 0) // The directory is empty
      {
        // We add a special file so that the empty directory will be written
        // anyway
        files = new File[1];
        files[0] = keepDirFile;
      }
      int size = files.length;
      String np = relPath + "/" + file.getName();
      for (int i = 0; i < size; i++)
        addFile(files[i], np, targetOs, override, list);
    }
    else
    {
      PackSource nf = new PackSource();
      nf.src = file.getAbsolutePath();
      nf.targetdir = relPath;
      nf.os = targetOs;
      nf.override = override;
      list.add(nf);
    }
  }


  /**
   *  Returns a list of the panels names to add.
   *
   * @param  data           The XML data.
   * @return                The panels list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getPanels(XMLElement data) throws Exception
  {
    // Initialisation
    ArrayList panels = new ArrayList();
    XMLElement root = data.getFirstChildNamed("panels");

    // We process each langpack markup
    Iterator iter = root.getChildren().iterator();
    while (iter.hasNext())
    {
      XMLElement panel = (XMLElement) iter.next();
      panels.add(panel.getAttribute("classname"));
    }

    // We return the ArrayList
    return panels;
  }


  /**
   *  Returns a list of the resources to include.
   *
   * @param  data           The XML data.
   * @return                The resources list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getResources(XMLElement data) throws Exception
  {
    // Initialisation
    ArrayList resources = new ArrayList();
    XMLElement root = data.getFirstChildNamed("resources");

    // We process each res markup
    Iterator iter = root.getChildren().iterator();
    String parse = null;
    boolean blParse = false;
    while (iter.hasNext())
    {
      XMLElement res = (XMLElement) iter.next();
      blParse = false;
      parse = res.getAttribute("parse");
      if (null != parse)
        blParse = parse.equalsIgnoreCase("yes");

      // Do not prepend basedir if already an absolute path
      String path = res.getAttribute("src");
      if (!new File(path).isAbsolute())
	 path = basedir + File.separator + path;

      resources.add(new Resource(res.getAttribute("id"),
        path,
        blParse,
        res.getAttribute("type"),
        res.getAttribute("encoding")));
    }

    // We add the uninstaller as a resource
    resources.add(new Resource("IzPack.uninstaller", Compiler.IZPACK_HOME +
      "lib" + File.separator + "uninstaller.jar"));

    // We return the ArrayList
    return resources;
  }


  /**
   *  Returns a list of the ISO3 codes of the langpacks to include.
   *
   * @param  data           The XML data.
   * @return                The ISO 3 codes list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getLangpacksCodes(XMLElement data) throws Exception
  {
    // Initialisation
    ArrayList langpacks = new ArrayList();
    XMLElement root = data.getFirstChildNamed("locale");

    // We process each langpack markup
    Iterator iter = root.getChildren().iterator();
    while (iter.hasNext())
    {
      XMLElement pack = (XMLElement) iter.next();
      langpacks.add(pack.getAttribute("iso3"));
    }

    // We return the ArrayList
    return langpacks;
  }


  /**
   *  Builds the Info class from the XML tree.
   *
   * @param  data           The XML data.
   * @return                The Info.
   * @exception  Exception  Description of the Exception
   */
  protected Info getInfo(XMLElement data) throws Exception
  {
    // Initialisation
    Info info = new Info();
    XMLElement root = data.getFirstChildNamed("info");

    // We get the name, version and URL
    info.setAppName(root.getFirstChildNamed("appname").getContent());
    info.setAppVersion(root.getFirstChildNamed("appversion").getContent());
    info.setAppURL(root.getFirstChildNamed("url").getContent());

    // We get the authors list
    XMLElement authors = root.getFirstChildNamed("authors");
    int size = authors.getChildrenCount();
    for (int i = 0; i < size; i++)
    {
      XMLElement author = authors.getChildAtIndex(i);
      Info.Author newAuthor = new Info.Author(author.getAttribute("name"),
        author.getAttribute("email"));
      info.addAuthor(newAuthor);
    }

    // We get the java version required
    XMLElement javaVersion = root.getFirstChildNamed("javaversion");
    if (javaVersion != null)
      info.setJavaVersion(javaVersion.getContent());

    // We return the suitable Info object
    return info;
  }


  /**
   *  Returns the suitable Packager, depending of the kind variable.
   *
   * @return                The packager.
   * @exception  Exception  Description of the Exception
   */
  protected Packager getPackager() throws Exception
  {
    if (kind.equalsIgnoreCase(STANDARD))
      return new StdPackager(output, packagerListener);
    else
      if (kind.equalsIgnoreCase(STANDARD_KUNSTSTOFF))
      return new StdKunststoffPackager(output, packagerListener);
    else
      if (kind.equalsIgnoreCase(WEB))
      return new WebPackager(output, packagerListener);
    else
      if (kind.equalsIgnoreCase(WEB_KUNSTSTOFF))
      return new WebKunststoffPackager(output, packagerListener);
    else
      throw new Exception("unknown installer kind");
  }


  /**
   *  Variable declaration is a fragmention in install.xml like : <variables>
   *  <variable name="nom" value="value"/> <variable name="foo" value="pippo"/>
   *  </variables> variable declared in this can be referd in parsable files
   *
   * @param  data           The XML data.
   * @return                The Properties.
   * @exception  Exception  Description of the Exception
   */
  protected Properties getVariables(XMLElement data) throws Exception
  {
    Properties retVal = null;

    // We get the varible list
    XMLElement root = data.getFirstChildNamed("variables");
    if (root == null)
      return retVal;

    retVal = new Properties();
    List variables = root.getChildrenNamed("variable");

    int size = variables.size();
    varMap = new VariableValueMapImpl();
    for (int i = 0; i < size; i++)
    {
      XMLElement var = (XMLElement) variables.get(i);
      retVal.setProperty(var.getAttribute("name"), var.getAttribute("value"));
      varMap.setVariable(var.getAttribute("name"), var.getAttribute("value"));
    }

    return retVal;
  }


  /**
   *  Returns the XMLElement representing the installation XML file.
   *
   * @return                The XML tree.
   * @exception  Exception  Description of the Exception
   */
  protected XMLElement getXMLTree() throws Exception
  {
    // Initialises the parser
    StdXMLParser parser = new StdXMLParser();
    parser.setBuilder(new StdXMLBuilder());
    parser.setReader(new StdXMLReader(new FileInputStream(filename)));
    parser.setValidator(new NonValidator());

    // We get it
    XMLElement data = (XMLElement) parser.parse();

    // We check it
    if (!data.getName().equalsIgnoreCase("installation"))
      throw new Exception("this is not an IzPack XML installation file");
    if (!data.getAttribute("version").equalsIgnoreCase(VERSION))
      throw new Exception("the file version is different from the compiler version");

    // We finally return the tree
    return data;
  }


  /**
   *  Represents a resource.
   *
   * @author     julien
   * created    October 26, 2002
   */
  class Resource
  {
    /**  The source. */
    public String src;

    /**  The Id. */
    public String id;

    /**  Shall we parse it ? */
    public boolean parse = false;

    /**  The type. */
    public String type;

    /**  The encoding. */
    public String encoding;


    /**
     *  The constructor.
     *
     * @param  id   The Id.
     * @param  src  The source.
     */
    public Resource(String id, String src)
    {
      this.src = src;
      this.id = id;
    }


    /**
     *  The constructor.
     *
     * @param  id        The Id.
     * @param  src       The source.
     * @param  parse     true if it must be parsed.
     * @param  type      The type.
     * @param  encoding  The encoding.
     */
    public Resource(String id, String src, boolean parse, String type, String encoding)
    {
      this.src = src;
      this.id = id;
      this.parse = parse;
      this.type = type;
      this.encoding = encoding;
    }

  }


  /**
   *  Represents a pack.
   *
   * @author     julien
   * created    October 26, 2002
   */
  class Pack
  {
    /**  The pack number. */
    public int number;

    /**  The pack name. */
    public String name;

    /**  Is the pack required ? */
    public boolean required;

    /**  The pack description. */
    public String description;

    /**  The files list. */
    public ArrayList packFiles;

    /**  The parsable files list. */
    public ArrayList parsables;

    /**  The executable files list. */
    public ArrayList executables;

      /**  The target operation system of this file */
      public String os;


    /**  The constructor. */
    public Pack()
    {
      packFiles = new ArrayList();
      parsables = new ArrayList();
      executables = new ArrayList();
    }
  }


  /**
   *  Represents a pack data source.
   *
   * @author     julien
   * created    October 26, 2002
   */
  class PackSource
  {
    /**  The source. */
    public String src;

    /**  The target directory. */
    public String targetdir;

    /**  Shall we override the file ? */
    public boolean override = true;

    /**  The target operation system of this file */
    public String os;
  }


  /**
   *  Represents a native library.
   *
   * @author     julien
   * created    October 26, 2002
   */
  class NativeLibrary
  {
    /**  The native library name. */
    public String name;

    /**  The library path. */
    public String path;
  }


  /**
   *  The main method if the compiler is invoked by a command-line call.
   *
   * @param  args  The arguments passed on the command-line.
   */
  public static void main(String[] args)
  {
    // Outputs some informations
    System.out.println("");
    System.out.println(".::  IzPack - Version " + IZPACK_VERSION + " ::.");
    System.out.println("");
    System.out.println("< compiler specifications version : " + VERSION + " >");
    System.out.println("");
    System.out.println("- Copyright (C) 2001-2003 Julien Ponge");
    System.out.println("- Visit http://www.izforge.com/ for the latests releases");
    System.out.println("- Released under the terms of the GNU GPL either version 2");
    System.out.println("  of the licence, or any later version.");
    System.out.println("");

    // We analyse the command line parameters
    try
    {
      // Our arguments
      String filename;
      String base = ".";
      String kind = "standard";
      String output;

      // First check
      int nArgs = args.length;
      if (nArgs < 3)
        throw new Exception("no arguments given");

      // We get the IzPack home directory
      int stdArgsIndex;
      if (args[0].equalsIgnoreCase("-HOME"))
      {
        stdArgsIndex = 2;
        IZPACK_HOME = args[1];
      }
      else
        stdArgsIndex = 0;

      if (!IZPACK_HOME.endsWith(File.separator))
        IZPACK_HOME = IZPACK_HOME + File.separator;

      // The users wants to know the command line parameters
      if (args[stdArgsIndex].equalsIgnoreCase("-?"))
      {
        System.out.println("-> Command line parameters are : (xml file) [args]");
        System.out.println("   (xml file): the xml file describing the installation");
        System.out.println("   -b (base) : indicates the base path that the compiler will use for filenames");
        System.out.println("               default is the current path");
        System.out.println("   -k (kind) : indicates the kind of installer to generate");
        System.out.println("               default is standard");
        System.out.println("   -o (out)  : indicates the output file name");
        System.out.println("               default is the xml file name");
        System.out.println("");
      }
      else
      {// We can parse the other parameters & try to compile the installation

        // We get the input file name and we initialize the output file name
        filename = args[stdArgsIndex];
        output = filename.substring(0, filename.length() - 3) + "jar";

        // We parse the other ones
        int pos = stdArgsIndex + 1;
        while (pos < nArgs)
          if ((args[pos].startsWith("-")) && (args[pos].length() == 2))
          {
            switch (args[pos].toLowerCase().charAt(1))
            {
                case 'b':
                  if ((pos + 1) < nArgs)
                  {
                    pos++;
                    base = args[pos];
                  }
                  else
                    throw new Exception("base argument missing");
                  break;
                case 'k':
                  if ((pos + 1) < nArgs)
                  {
                    pos++;
                    kind = args[pos];
                  }
                  else
                    throw new Exception("kind argument missing");
                  break;
                case 'o':
                  if ((pos + 1) < nArgs)
                  {
                    pos++;
                    output = args[pos];
                  }
                  else
                    throw new Exception("output argument missing");
                  break;
                default:
                  throw new Exception("unknown argument");
            }
            pos++;
          }
          else
            throw new Exception("bad argument");

        // Outputs what we are going to do
        System.out.println("-> Processing : " + filename);
        System.out.println("-> Output     : " + output);
        System.out.println("-> Base path  : " + base);
        System.out.println("-> Kind       : " + kind);
        System.out.println("");

        // Calls the compiler
        Compiler compiler = new Compiler(filename, base, kind, output);
        CmdlinePackagerListener listener = new CmdlinePackagerListener();
        compiler.setPackagerListener(listener);
        compiler.compile();

        // Waits
        while (compiler.isAlive())
          Thread.yield();
      }
    }
    catch (Exception err)
    {
      // Something bad has happened
      System.err.println("-> Fatal error :");
      System.err.println("   " + err.getMessage());
      err.printStackTrace();
      System.err.println("");
      System.err.println("(tip : use -? to get the commmand line parameters)");
    }

    System.out.println("Build time: "+new Date());
    // Closes the JVM
    System.exit(0);
  }


  /**
   *  Used to handle the packager messages in the command-line mode.
   *
   * @author     julien
   * created    October 26, 2002
   */
  static class CmdlinePackagerListener implements PackagerListener
  {
    /**
     *  Called as the packager sends messages.
     *
     * @param  info  The information.
     */
    public void packagerMsg(String info)
    {
      System.out.println(info);
    }


    /**  Called when the packager starts. */
    public void packagerStart()
    {
      System.out.println("[ Begin ]");
      System.out.println("");
    }


    /**  Called when the packager stops. */
    public void packagerStop()
    {
      System.out.println("");
      System.out.println("[ End ]");
    }
  }
}

