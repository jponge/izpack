/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

import org.apache.tools.ant.DirectoryScanner;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.Panel;
import com.izforge.izpack.ParsableFile;
import com.izforge.izpack.UpdateCheck;
import com.izforge.izpack.installer.VariableSubstitutor;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsConstraint;

/**
 *  The IzPack compiler class.
 *
 * @author     Julien Ponge
 * @author     Tino Schwarze
 * @author     Chadwick McHenry
 */
public class Compiler extends Thread
{
  /**  The compiler version. */
  public final static String VERSION = "1.0";

  /**  The IzPack version. */
  public final static String IZPACK_VERSION = "3.6.0-RC2 (build 2004.07.03)";

  /**  Standard installer. */
  public final static String STANDARD = "standard";

  /**  Web installer. */
  public final static String WEB = "web";

  /**  The IzPack home directory. */
  public static String IZPACK_HOME = ".";
  
  /** Constant for checking attributes. */
  private static boolean YES = true;

  /** Constant for checking attributes. */
  private static boolean NO = false;

  /**  The IzPack home directory specified or found on startup. */
  private static File home = new File (IZPACK_HOME);

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

  /** Collects and packs files into installation jars, as told. */
  private Packager packager = null;
  
  /** Error code, set to true if compilation succeeded. */
  private boolean compileFailed = true;

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
      executeCompiler(); // Execute the compiler - may send info to System.out
    } catch (CompilerException ce)
    {
      System.out.println(ce.getMessage() + "\n");
    } catch (Exception e)
    {
      if (Debug.stackTracing())
      {
        e.printStackTrace();
      } else
      {
        System.out.println("ERROR: " + e.getMessage());
      }
    }
  }

  /**
   *  Compiles the installation.
   *
   * @exception  Exception  Description of the Exception
   */
  public void executeCompiler() throws Exception
  {
    // normalize and test: TODO: may allow failure if we require write access
    File base = new File(basedir).getAbsoluteFile();
    if (!base.canRead() || !base.isDirectory())
      throw new CompilerException("Invalid base directory: " + base);

    // We get the XML data tree
    XMLElement data = getXMLTree();

    // We create the Packager
    packager = new Packager();
    packager.setPackagerListener(packagerListener);

    // We add all the information
    addVariables(data);
    addInfo(data);
    addGUIPrefs(data);
    addLangpacks(data);
    addResources(data);
    addNativeLibraries(data);
    addJars(data);
    addPanels(data);
    addPacks(data);
    
    // We ask the packager to create the installer
    packager.createInstaller(new File(output));
    this.compileFailed = false;
  }

  public boolean wasSuccessful()
  {
    return !this.compileFailed;
  }


  /**
   *  Returns the GUIPrefs.
   *
   * @param  data           The XML data.
   * @return                The GUIPrefs.
   * @exception  Exception  Description of the Exception
   */
  protected void addGUIPrefs(XMLElement data) throws CompilerException
  {
    // We get the XMLElement & the attributes
    XMLElement gp = data.getFirstChildNamed("guiprefs");
    GUIPrefs prefs = new GUIPrefs();
    if (gp != null)
    {
      prefs.resizable = requireYesNoAttribute(gp, "resizable");
      prefs.width = requireIntAttribute(gp, "width");
      prefs.height = requireIntAttribute(gp, "height");

      // Look and feel mappings
      Iterator it = gp.getChildrenNamed("laf").iterator();
      while (it.hasNext())
      {
        XMLElement laf = (XMLElement)it.next();
        String lafName = requireAttribute(laf, "name");
        requireChildNamed(laf, "os");

        Iterator oit = laf.getChildrenNamed("os").iterator();
        while (oit.hasNext())
        {
          XMLElement os = (XMLElement)oit.next(); 
          String osName = requireAttribute(os, "family");
          prefs.lookAndFeelMapping.put(osName, lafName);
        }

        Iterator pit = laf.getChildrenNamed("param").iterator();
        Map params = new TreeMap();
        while (pit.hasNext())
        {
          XMLElement param = (XMLElement)pit.next();
          String name  = requireAttribute(param, "name");
          String value = requireAttribute(param, "value");
          params.put(name, value);
        }
        prefs.lookAndFeelParams.put(lafName, params);
      }

      // make sure jar contents of each are available in installer
      // map is easier to read/modify than if tree
      HashMap lafMap = new HashMap();
      lafMap.put("liquid",    "liquidlnf.jar");
      lafMap.put("kunststoff","kunststoff.jar");
      lafMap.put("metouia",   "metouia.jar");
      lafMap.put("looks",     "looks.jar");
      
      // is this really what we want? a double loop? needed, since above, it's
      // the /last/ lnf for an os which is used, so can't add during initial
      // loop
      Iterator kit = prefs.lookAndFeelMapping.keySet().iterator();
      while (kit.hasNext())
      {
        String lafName = (String)prefs.lookAndFeelMapping.get(kit.next());
        String lafJarName = (String) lafMap.get(lafName);
        if (lafJarName == null)
          parseError(gp, "Unrecognized Look and Feel: " + lafName);

        URL lafJarURL = findIzPackResource("lib/" + lafJarName,
                                            "Look and Feel Jar file", gp);
        packager.addJarContent(lafJarURL);
      }
    }
    packager.setGUIPrefs(prefs);
  }

  /**
   * Add project specific external jar files to the installer.
   *
   * @param  data           The XML data.
   */
  protected void addJars(XMLElement data) throws CompilerException
  {
    Iterator iter = data.getChildrenNamed("jar").iterator();
    while (iter.hasNext())
    {
      XMLElement el = (XMLElement) iter.next();
      String src = requireAttribute(el, "src");
      URL url = findProjectResource(src, "Jar file", el);
      packager.addJarContent(url);
    }
  }

  /**
   * Add native libraries to the installer.
   *
   * @param  data           The XML data.
   */
  protected void addNativeLibraries(XMLElement data) throws Exception
  {
    Iterator iter = data.getChildrenNamed("native").iterator();
    while (iter.hasNext())
    {
      XMLElement el = (XMLElement) iter.next();
      String type = requireAttribute(el, "type");
      String name = requireAttribute(el, "name");
      String path = "bin/native/" + type + "/" + name;
      URL url = findIzPackResource(path, "Native Library", el);
      packager.addNativeLibrary(name, url);
    }
  }

  /**
   *  Add packs and their contents to the installer.
   *
   * @param  data           The XML data.
   */
  protected void addPacks(XMLElement data) throws CompilerException
  {
    // Initialisation
    XMLElement root = requireChildNamed(data, "packs");

    // at least one pack is required
    Vector packElements = root.getChildrenNamed("pack");
    if (packElements.isEmpty())
      parseError(root, "<packs> requires a <pack>");
    
    Iterator packIter = packElements.iterator();
    while (packIter.hasNext())
    {
      XMLElement el = (XMLElement) packIter.next();

      // Trivial initialisations
      String name = requireAttribute(el, "name");
      String id = el.getAttribute("id");
      String description = requireChildNamed(el, "description").getContent();
      boolean required = requireYesNoAttribute(el, "required");

      PackInfo pack = new PackInfo(name, id, description, required);
      pack.setOsConstraints(OsConstraint.getOsList(el)); // TODO: unverified
      pack.setPreselected(validateYesNoAttribute(el, "preselected", YES));

      // We get the parsables list
      Iterator iter = el.getChildrenNamed("parsable").iterator();
      while (iter.hasNext())
      {
        XMLElement p = (XMLElement) iter.next();
        String target = requireAttribute(p, "targetfile");
        String type = p.getAttribute("type", "plain");
        String encoding = p.getAttribute("encoding", null);
        List osList = OsConstraint.getOsList(p); // TODO: unverified

        pack.addParsable(new ParsableFile(target, type, encoding, osList));
      }

      // We get the executables list
      iter = el.getChildrenNamed("executable").iterator();
      while (iter.hasNext())
      {
        XMLElement e = (XMLElement) iter.next();
        ExecutableFile executable = new ExecutableFile();
        String val; // temp value

        executable.path = requireAttribute(e, "targetfile");

        // when to execute this executable
        val = e.getAttribute("stage", "never");
        if ("postinstall".equalsIgnoreCase(val))
          executable.executionStage = ExecutableFile.POSTINSTALL;
        else if ("uninstall".equalsIgnoreCase(val))
          executable.executionStage = ExecutableFile.UNINSTALL;

        // main class  of this executable
        String executeClass = e.getAttribute("class");

        // type of this executable
        val = e.getAttribute("type", "bin");
        if ("jar".equalsIgnoreCase(val))
          executable.type = ExecutableFile.JAR;

        // what to do if execution fails
        val = e.getAttribute("failure", "ask");
        if ("abort".equalsIgnoreCase(val))
          executable.onFailure = ExecutableFile.ABORT;
        else if ("warn".equalsIgnoreCase(val))
          executable.onFailure = ExecutableFile.WARN;

        // whether to keep the executable after executing it
        val = e.getAttribute("keep");
        executable.keepFile = "true".equalsIgnoreCase(val);

        // get arguments for this executable
        XMLElement args = e.getFirstChildNamed("args");
        if (null != args)
        {
          Iterator argIterator = args.getChildrenNamed("arg").iterator();
          while (argIterator.hasNext())
          {
            XMLElement arg = (XMLElement) argIterator.next();
            executable.argList.add(requireAttribute(arg, "value"));
          }
        }

        executable.osList = OsConstraint.getOsList(e); // TODO: unverified

        pack.addExecutable(executable);
      }

      // We get the files list
      iter = el.getChildrenNamed("file").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        String src = requireAttribute(f, "src");
        String targetdir = requireAttribute(f, "targetdir");
        List osList = OsConstraint.getOsList(f); // TODO: unverified
        int override = getOverrideValue(f);

        File file = new File(src);
        if (! file.isAbsolute())
          file = new File(basedir, src);

        try
        {
          addRecursively(file, targetdir, osList, override, pack);
        } catch (Exception x)
        {
          parseError(f, x.getMessage(), x);
        }
      }

      // We get the singlefiles list
      iter = el.getChildrenNamed("singlefile").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        String src = requireAttribute(f, "src");
        String target = requireAttribute(f, "target");
        List osList = OsConstraint.getOsList(f); // TODO: unverified
        int override = getOverrideValue(f);

        File file = new File(src);
        if (! file.isAbsolute())
          file = new File(basedir, src);

        try
        {
          pack.addFile(file, target, osList, override);
        } catch (FileNotFoundException x)
        {
          parseError(f, x.getMessage(), x);
        }
      }

      // We get the fileset list
      iter = el.getChildrenNamed("fileset").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        String dir_attr = requireAttribute(f, "dir");

        File dir = new File(dir_attr);
        if (! dir.isAbsolute())
          dir = new File(basedir, dir_attr);
        if (! dir.isDirectory()) // also tests '.exists()'
          parseError(f, "Invalid directory 'dir': " + dir_attr);

        boolean casesensitive = validateYesNoAttribute(f, "casesensitive", YES);
        String targetdir = requireAttribute(f, "targetdir");
        List osList = OsConstraint.getOsList(f); // TODO: unverified
        int override = getOverrideValue(f);
        
        //  get includes and excludes
        Vector xcludesList = null;
        String[] includes = null;
        xcludesList = f.getChildrenNamed("include");
        if (! xcludesList.isEmpty())
        {
          includes = new String[xcludesList.size()];
          for (int j = 0; j < xcludesList.size(); j++)
          {
            XMLElement xclude = (XMLElement) xcludesList.get(j);
            includes[j] = requireAttribute(xclude, "name");
          }
        }
        String[] excludes = null;
        xcludesList = f.getChildrenNamed("exclude");
        if (! xcludesList.isEmpty())
        {
          excludes = new String[xcludesList.size()];
          for (int j = 0; j < xcludesList.size(); j++)
          {
            XMLElement xclude = (XMLElement) xcludesList.get(j);
            excludes[j] = requireAttribute(xclude, "name");
          }
        }

        // scan and add fileset
        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.addDefaultExcludes();
        ds.setBasedir(dir);
        ds.setCaseSensitive(casesensitive);
        ds.scan();

        String[] files = ds.getIncludedFiles();
        String[] dirs = ds.getIncludedDirectories();

        // Directory scanner has done recursion, add files and directories
        for (int i = 0; i < files.length; ++i)
        {
          try
          {
            String target = new File(targetdir, files[i]).getPath();
            pack.addFile(new File(dir, files[i]), target, osList, override);
          } catch (FileNotFoundException x)
          {
            parseError(f, x.getMessage(), x);
          }
        }
        for (int i = 0; i < dirs.length; ++i)
        {
          try
          {
            String target = new File(targetdir, dirs[i]).getPath();
            pack.addFile(new File(dir, dirs[i]), target, osList, override);
          } catch (FileNotFoundException x)
          {
            parseError(f, x.getMessage(), x);
          }
        }
      }

      // get the updatechecks list
      iter = el.getChildrenNamed("updatecheck").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();

        String casesensitive = f.getAttribute("casesensitive");

        //  get includes and excludes
        ArrayList includesList = new ArrayList();
        ArrayList excludesList = new ArrayList();

        //  get includes and excludes
        Iterator include_it = f.getChildrenNamed("include").iterator();
        while (include_it.hasNext())
        {
          XMLElement inc_el = (XMLElement) include_it.next();
          includesList.add(requireAttribute(inc_el, "name"));
        }

        Iterator exclude_it = f.getChildrenNamed("exclude").iterator();
        while (exclude_it.hasNext())
        {
          XMLElement excl_el = (XMLElement) exclude_it.next();
          excludesList.add(requireAttribute(excl_el, "name"));
        }

        pack.addUpdateCheck(
          new UpdateCheck(includesList, excludesList, casesensitive));
      }

      // We add the pack
      packager.addPack(pack);
    }
  }

  /**
   *  Recursive method to add files in a pack.
   *
   * @param  file           The file to add.
   * @param  targetdir      The relative path to the parent.
   * @param  osList         The target OS constraints.
   * @param  override       Overriding behaviour.
   * @param  pack           Pack to be packed into
   * @exception FileNotFoundException if the file does not exist
   */
  protected void addRecursively(File file, String targetdir,
                                List osList, int override, PackInfo pack)
    throws IOException
  {
    String targetfile = targetdir + "/" + file.getName();
    if (! file.isDirectory())
      pack.addFile(file, targetfile, osList, override);
    else
    {
      File[] files = file.listFiles();
      if (files.length == 0) // The directory is empty so must be added
        pack.addFile(file, targetfile, osList, override);
      else
      {
        // new targetdir = targetfile;
        for (int i = 0; i < files.length; i++)
          addRecursively(files[i], targetfile, osList, override, pack);
      }
    }
  }

  /**
   * Parse panels and their paramters, locate the panels resources and add to
   * the Packager.
   *
   * @param  data           The XML data.
   * @exception  Exception  Description of the Exception
   */
  protected void addPanels(XMLElement data) throws CompilerException
  {
    XMLElement root = requireChildNamed(data, "panels");

    // at least one panel is required
    Vector panels = root.getChildrenNamed("panel");
    if (panels.isEmpty())
      parseError(root, "<panels> requires a <panel>");
      
    // We process each panel markup
    Iterator iter = panels.iterator();
    while (iter.hasNext())
    {
      XMLElement xmlPanel = (XMLElement) iter.next();
      
      // create the serialized Panel data
      Panel panel = new Panel();
      panel.osConstraints = OsConstraint.getOsList(xmlPanel);
      panel.className = xmlPanel.getAttribute("classname");

      // Panel files come in jars packaged w/ IzPack
      String jarPath = "bin/panels/" + panel.className + ".jar";
      URL url = findIzPackResource(jarPath, "Panel jar file", xmlPanel);

      // insert into the packager
      packager.addPanelJar(panel, url);
    }
  }

  /**
   *  Adds the resources.
   *
   * @param  data           The XML data.
   * @exception  CompilerException  Description of the Exception
   */
  protected void addResources(XMLElement data) throws CompilerException
  {
    XMLElement root = data.getFirstChildNamed("resources");
    if (root == null)
      return;

    // We process each res markup
    Iterator iter = root.getChildrenNamed("res").iterator();
    while (iter.hasNext())
    {
      XMLElement res = (XMLElement) iter.next();
      String id = requireAttribute(res, "id");
      String src = requireAttribute(res, "src");
      boolean parse = validateYesNoAttribute(res, "parse", NO);

      // basedir is not prepended if src is already an absolute path
      URL url = findProjectResource(src, "Resource", res);

      // substitute variable values in the resource if parsed
      if (parse)
      {
        if (packager.getVariables().isEmpty())
        {
          parseWarn(res, "No variables defined. " +
                    url.getPath() + " not parsed.");
        } else
        {
          String type = res.getAttribute("type");
          String encoding = res.getAttribute("encoding");
          File parsedFile = null;

          try
          {
            // make the substitutions into a temp file
            InputStream bin = new BufferedInputStream(url.openStream());

            parsedFile = File.createTempFile("izpp", null);
            parsedFile.deleteOnExit();
            FileOutputStream outFile = new FileOutputStream(parsedFile);
            BufferedOutputStream bout = new BufferedOutputStream(outFile);

            VariableSubstitutor vs = new VariableSubstitutor(packager.getVariables());
            vs.substitute(bin, bout, type, encoding);
            bin.close();
            bout.close();

            // and specify the substituted file to be added to the packager
            url = parsedFile.toURL();
          } catch (IOException x)
          {
            parseError(res, x.getMessage(), x);
          }
        }
      }

      packager.addResource(id, url);
    }
  }

  /**
   *  Adds the ISO3 codes of the langpacks and associated resources.
   *
   * @param  data           The XML data.
   * @exception  CompilerException  Description of the Exception
   */
  protected void addLangpacks(XMLElement data)
    throws CompilerException
  {
    XMLElement root = requireChildNamed(data, "locale");

    // at least one langpack is required
    Vector locals = root.getChildrenNamed("langpack");
    if (locals.isEmpty())
      parseError(root, "<locale> requires a <langpack>");
      
    // We process each langpack markup
    Iterator iter = locals.iterator();
    while (iter.hasNext())
    {
      XMLElement el = (XMLElement) iter.next();
      String iso3 = requireAttribute(el, "iso3");
      String path;

      path = "bin/langpacks/installer/" + iso3 + ".xml";
      URL iso3xmlURL = findIzPackResource(path, "ISO3 file", el);

      path = "bin/langpacks/flags/" + iso3 + ".gif";
      URL iso3FlagURL = findIzPackResource(path, "ISO3 flag image", el);
      
      packager.addLangPack(iso3, iso3xmlURL, iso3FlagURL);
    }
  }

  /**
   *  Builds the Info class from the XML tree.
   *
   * @param  data           The XML data.
   * @return                The Info.
   * @exception  Exception  Description of the Exception
   */
  protected void addInfo(XMLElement data) throws Exception
  {
    // Initialisation
    XMLElement root = requireChildNamed(data, "info");

    Info info = new Info();
    String temp = null;
    info.setAppName(requireContent(requireChildNamed(root, "appname")));
    info.setAppVersion(requireContent(requireChildNamed(root, "appversion")));

    // validate and insert app URL
    URL appURL = requireURLContent(requireChildNamed(root, "url"));
    info.setAppURL(appURL.toString());

    // We get the authors list
    XMLElement authors = root.getFirstChildNamed("authors");
    if (authors != null)
    {
      Iterator iter = authors.getChildrenNamed("author").iterator();
      while (iter.hasNext())
      {
        XMLElement author = (XMLElement) iter.next();
        String name = requireAttribute(author, "name");
        String email = requireAttribute(author, "email");
        info.addAuthor(new Info.Author(name, email));
      }
    }

    // We get the java version required
    XMLElement javaVersion = root.getFirstChildNamed("javaversion");
    if (javaVersion != null)
      info.setJavaVersion(requireContent(javaVersion));

    // validate and insert (and require if -web kind) web dir
    XMLElement webDirURL = root.getFirstChildNamed("webdir");
    if (webDirURL != null)
      info.setWebDirURL(requireURLContent(webDirURL).toString());
    if (kind != null)
    {
      if (kind.equalsIgnoreCase(WEB) && webDirURL == null)
      {
        parseError(root, "<webdir> required when \"WEB\" installer requested");
      }
      else if (kind.equalsIgnoreCase(STANDARD) && webDirURL != null)
      {
        // Need a Warning? parseWarn(webDirURL, "Not creating web installer.");
        info.setWebDirURL(null);
      }
    }

    // Add the uninstaller as a resource if specified
    XMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
    if (validateYesNoAttribute(uninstallInfo, "write", YES))
    {
      URL url = findIzPackResource("lib/uninstaller.jar", "Uninstaller", root);
      packager.addResource("IzPack.uninstaller", url);
    }

    packager.setInfo(info);
  }

  /**
   *  Variable declaration is a fragment of the xml file.  For example:
   *  <pre>
   *    &lt;variables&gt;
   *      &lt;variable name="nom" value="value"/&gt;
   *      &lt;variable name="foo" value="pippo"/&gt;
   *    &lt;/variables&gt;
   *  </pre>
   *  variable declared in this can be referred to in parsable files.
   *
   * @param  data           The XML data.
   * @exception  Exception  Description of the Exception
   */
  protected void addVariables(XMLElement data) throws CompilerException
  {
    // We get the varible list
    XMLElement root = data.getFirstChildNamed("variables");
    if (root == null)
      return;

    Properties variables = packager.getVariables();

    Iterator iter = root.getChildrenNamed("variable").iterator();
    while (iter.hasNext())
    {
      XMLElement var = (XMLElement) iter.next();
      String name = requireAttribute(var, "name");
      String value = requireAttribute(var, "value");
      if (variables.contains(name))
        parseWarn(var, "Variable '" + name + "' being overwritten");
      variables.setProperty(name, value);
    }
  }

  /**
   *  Returns the XMLElement representing the installation XML file.
   *
   * @return                The XML tree.
   * @exception  CompilerException  For problems with the installation file
   * @exception  IOException  for errors reading the installation file
   */
  protected XMLElement getXMLTree() throws CompilerException, IOException
  {
    // Initialises the parser
    StdXMLParser parser = new StdXMLParser();
    parser.setBuilder(new StdXMLBuilder());
    parser.setReader(new StdXMLReader(new FileInputStream(filename)));
    parser.setValidator(new NonValidator());

    // We get it
    XMLElement data = null;
    try
    {
      data = (XMLElement) parser.parse();
    } catch (Exception x)
    {
      throw new CompilerException("Error parsing installation file", x);
    }

    // We check it
    if (!"installation".equalsIgnoreCase(data.getName()))
      parseError(data, "this is not an IzPack XML installation file");
    if (!requireAttribute(data, "version").equalsIgnoreCase(VERSION))
      parseError(
        data,
        "the file version is different from the compiler version");

    // We finally return the tree
    return data;
  }

  protected int getOverrideValue(XMLElement f)
    throws CompilerException
  {
    int override = PackFile.OVERRIDE_UPDATE;

    String override_val = f.getAttribute("override");
    if (override_val != null)
    {
      if (override_val.equalsIgnoreCase("true"))
      {
        override = PackFile.OVERRIDE_TRUE;
      } else if (override_val.equalsIgnoreCase("false"))
      {
        override = PackFile.OVERRIDE_FALSE;
      } else if (override_val.equalsIgnoreCase("asktrue"))
      {
        override = PackFile.OVERRIDE_ASK_TRUE;
      } else if (override_val.equalsIgnoreCase("askfalse"))
      {
        override = PackFile.OVERRIDE_ASK_FALSE;
      } else if (override_val.equalsIgnoreCase("update"))
      {
        override = PackFile.OVERRIDE_UPDATE;
      }
      else
        parseError(f, "invalid value for attribute \"override\"");
    }

    return override;
  }

  
  /**
   * Look for a project specified resources, which, if not absolute, are sought
   * relative to the projects basedir. The path should use '/' as the
   * fileSeparator. If the resource is not found, a CompilerException is thrown
   * indicating fault in the parent element.
   *
   * @param path the relative path (using '/' as separator) to the resource.
   * @param desc the description of the resource used to report errors
   * @param parent the XMLElement the resource is specified in, used to
   *               report errors
   * @return a URL to the resource.
   */
  private URL findProjectResource(String path, String desc, XMLElement parent)
    throws CompilerException
  {
    URL url = null;
    File resource = new File(path);
    if (! resource.isAbsolute())
      resource = new File(basedir, path);

    if (! resource.exists()) // fatal
      parseError(parent, desc + " not found: " + resource);

    try
    {
      url = resource.toURL();
    } catch(MalformedURLException how)
    {
      parseError(parent, desc + "(" + resource + ")", how);
    }

    return url;
  }

  /**
   * Look for an IzPack resource either in the compiler jar, or within
   * IZPACK_HOME.  The path must not be absolute. The path must use '/' as the
   * fileSeparator (it's used to access the jar file). If the resource is not
   * found, a CompilerException is thrown indicating fault in the parent
   * element.
   *
   * @param path the relative path (using '/' as separator) to the resource.
   * @param desc the description of the resource used to report errors
   * @param parent the XMLElement the resource is specified in, used to
   *               report errors
   * @return a URL to the resource.
   */
  private URL findIzPackResource(String path, String desc, XMLElement parent)
    throws CompilerException
  {
    URL url = getClass().getResource("/" + path);
    if (url == null)
    {
      File resource = new File(path);
      if (! resource.isAbsolute())
        resource = new File(IZPACK_HOME, path);

      if (! resource.exists()) // fatal
        parseError(parent, desc + " not found: " + resource);

      try
      {
        url = resource.toURL();
      } catch(MalformedURLException how)
      {
        parseError(parent, desc + "(" + resource + ")", how);
      }
    }

    return url;
  }

  /**
   * Create parse error with consistent messages. Includes file name and line #
   * of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the error occured
   * @param message Brief message explaining error
   */
  protected void parseError(XMLElement parent, String message)
    throws CompilerException
  {
    this.compileFailed = true;
    throw new CompilerException(
      filename + ":" + parent.getLineNr() + ": " + message);
  }

  /**
   * Create a chained parse error with consistent messages. Includes file name
   * and line # of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the error occured
   * @param message Brief message explaining error
   */
  protected void parseError(XMLElement parent, String message, Throwable cause)
    throws CompilerException
  {
    this.compileFailed = true;
    throw new CompilerException(
      filename + ":" + parent.getLineNr() + ": " + message,
      cause);
  }

  /**
   * Create a parse warning with consistent messages. Includes file name
   * and line # of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the warning occured
   * @param message Warning message
   */
  protected void parseWarn(XMLElement parent, String message)
  {
    System.out.println(filename + ":" + parent.getLineNr() + ": " + message);
  }

  /**
   * Call getFirstChildNamed on the parent, producing a meaningful error
   * message on failure. It is an error for 'parent' to be null.
   *
   * @param parent  The element to search for a child
   * @param name    Name of the child element to get
   */
  protected XMLElement requireChildNamed(XMLElement parent, String name)
    throws CompilerException
  {
    XMLElement child = parent.getFirstChildNamed(name);
    if (child == null)
      parseError(
        parent,
        "<" + parent.getName() + "> requires child <" + name + ">");
    return child;
  }

  /**
   * Call getContent on an element, producing a meaningful error message if not
   * present, or empty, or a valid URL. It is an error for 'element' to be
   * null.
   *
   * @param element   The element to get content of
   */
  protected URL requireURLContent(XMLElement element)
    throws CompilerException
  {
    URL url = null;
    try
    {
      url = new URL(requireContent(element));
    }
    catch (MalformedURLException x)
    {
      parseError(element, "<" + element.getName() + "> requires valid URL", x);
    }
    return url;
  }

  /**
   * Call getContent on an element, producing a meaningful error message if not
   * present, or empty. It is an error for 'element' to be null.
   *
   * @param element   The element to get content of
   */
  protected String requireContent(XMLElement element) throws CompilerException
  {
    String content = element.getContent();
    if (content == null || content.length() == 0)
      parseError(element, "<" + element.getName() + "> requires content");
    return content;
  }

  /**
   * Call getAttribute on an element, producing a meaningful error message if
   * not present, or empty. It is an error for 'element' or 'attribute' to be null.
   *
   * @param element   The element to get the attribute value of
   * @param attribute The name of the attribute to get
   */
  protected String requireAttribute(XMLElement element, String attribute)
    throws CompilerException
  {
    String value = element.getAttribute(attribute);
    if (value == null)
      parseError(
        element,
        "<" + element.getName() + "> requires attribute '" + attribute + "'");
    return value;
  }

  /**
   * Get a required attribute of an element, ensuring it is an integer.  A
   * meaningful error message is generated as a CompilerException if not
   * present or parseable as an int. It is an error for 'element' or
   * 'attribute' to be null.
   *
   * @param element   The element to get the attribute value of
   * @param attribute The name of the attribute to get
   */
  protected int requireIntAttribute(XMLElement element, String attribute)
    throws CompilerException
  {
    String value = element.getAttribute(attribute);
    if (value == null || value.length() == 0)
      parseError(
        element,
        "<" + element.getName() + "> requires attribute '" + attribute + "'");
    try
    {
      return Integer.parseInt(value);
    } catch (NumberFormatException x)
    {
      parseError(element, "'" + attribute + "' must be an integer");
    }
    return 0; // never happens
  }

  /**
   * Call getAttribute on an element, producing a meaningful error message if
   * not present, or one of "yes" or "no". It is an error for 'element' or
   * 'attribute' to be null.
   *
   * @param element        The element to get the attribute value of
   * @param attribute      The name of the attribute to get
   */
  protected boolean requireYesNoAttribute(XMLElement element, String attribute)
    throws CompilerException
  {
    String value = requireAttribute(element, attribute);
    if (value.equalsIgnoreCase("yes"))
      return true;
    if (value.equalsIgnoreCase("no"))
      return false;

    parseError(
      element,
      "<" + element.getName() + "> invalid attribute '"
      + attribute + "': Expected (yes|no)");

    return false; // never happens
  }

  /**
   * Call getAttribute on an element, producing a meaningful warning if not
   * "yes" or "no". If the 'element' or 'attribute' are null, the default value
   * is returned.
   *
   * @param element        The element to get the attribute value of
   * @param attribute      The name of the attribute to get
   * @param defaultValue   Value returned if attribute not present or invalid
   */
  protected boolean validateYesNoAttribute(
    XMLElement element,
    String attribute,
    boolean defaultValue)
  {
    if (element == null)
      return defaultValue;

    String value =
      element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
    if (value.equalsIgnoreCase("yes"))
      return true;
    if (value.equalsIgnoreCase("no"))
      return false;

    // TODO: should this be an error if it's present but "none of the above"?
    parseWarn(
      element,
      "<" + element.getName() + "> invalid attribute '"
      + attribute + "': Expected (yes|no) if present");

    return defaultValue;
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
    System.out.println("- Copyright (C) 2001-2004 Julien Ponge");
    System.out.println(
      "- Visit http://www.izforge.com/ for the latests releases");
    System.out.println(
      "- Released under the terms of the GNU GPL either version 2");
    System.out.println("  of the licence, or any later version.");
    System.out.println("");

    // exit code 1 means: error
    int exitCode = 1;

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
      } else
      {
        stdArgsIndex = 0;
        String izHome = System.getProperty("IZPACK_HOME");
        if (izHome != null)
          IZPACK_HOME = izHome;
      }
      
      home = new File(IZPACK_HOME);
      if (! home.exists() && home.isDirectory())
      {
        System.err.println("IZPACK_HOME (" + IZPACK_HOME + ") doesn't exist");
        System.exit(-1);
      }

      // The users wants to know the command line parameters
      if (args[stdArgsIndex].equalsIgnoreCase("-?"))
      {
        System.out.println(
          "-> Command line parameters are : (xml file) [args]");
        System.out.println(
          "   (xml file): the xml file describing the installation");
        System.out.println(
          "   -b (base) : indicates the base path that the compiler will use for filenames");
        System.out.println("               default is the current path");
        System.out.println(
          "   -k (kind) : indicates the kind of installer to generate");
        System.out.println("               default is standard");
        System.out.println("   -o (out)  : indicates the output file name");
        System.out.println("               default is the xml file name\n");

        System.out.println(
          "   When using vm option -DSTACKTRACE=true there is all kind of debug info ");
        System.out.println("");
      } else
      {
        // We can parse the other parameters & try to compile the installation

        // We get the input file name and we initialize the output file name
        filename = args[stdArgsIndex];
        // default jar files names are based on input file name
        output = filename.substring(0, filename.length() - 3) + "jar";

        // We parse the other ones
        int pos = stdArgsIndex + 1;
        while (pos < nArgs)
          if ((args[pos].startsWith("-")) && (args[pos].length() == 2))
          {
            switch (args[pos].toLowerCase().charAt(1))
            {
              case 'b' :
                if ((pos + 1) < nArgs)
                {
                  pos++;
                  base = args[pos];
                } else
                  throw new Exception("base argument missing");
                break;
              case 'k' :
                if ((pos + 1) < nArgs)
                {
                  pos++;
                  kind = args[pos];
                } else
                  throw new Exception("kind argument missing");
                break;
              case 'o' :
                if ((pos + 1) < nArgs)
                {
                  pos++;
                  output = args[pos];
                } else
                  throw new Exception("output argument missing");
                break;
              default :
                throw new Exception("unknown argument");
            }
            pos++;
          } else
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
          Thread.sleep(100);

        if (compiler.wasSuccessful())
          exitCode = 0;

        System.out.println("Build time: " + new Date());
      }
    } catch (Exception err)
    {
      // Something bad has happened
      System.err.println("-> Fatal error :");
      System.err.println("   " + err.getMessage());
      err.printStackTrace();
      System.err.println("");
      System.err.println("(tip : use -? to get the commmand line parameters)");
    }

    // Closes the JVM
    System.exit(exitCode);
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
