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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import com.izforge.izpack.installer.VariableValueMapImpl;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsConstraint;

/**
 *  The IzPack compiler class.
 *
 * @author     Julien Ponge
 * @author     Tino Schwarze
 */
public class Compiler extends Thread
{
  /**  The compiler version. */
  public final static String VERSION = "1.0";

  /**  The IzPack version. */
  public final static String IZPACK_VERSION = "3.5.0-RC1 (build 2004.xx.yy)";

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

  /** Constant for checking attributes. */
  private static boolean YES = true;

  /** Constant for checking attributes. */
  private static boolean NO = false;

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
    catch (CompilerException ce)
    {
      System.out.println(ce.getMessage()+"\n");
    }
    catch (Exception e)
    {
      if (Debug.stackTracing ())
      {
         e.printStackTrace(); 
      }
      else
      {
        System.out.println ("ERROR: "+e.getMessage ()); 
      }
    }
  }

	private static class ByteCountingOutputStream extends OutputStream
	{
		private long count;
		private OutputStream os;
		public ByteCountingOutputStream(OutputStream os)
		{
			this.os = os;
		}

		public void write(byte[] b, int off, int len) throws IOException
		{
			os.write(b, off, len);
			count += len;
		}

		public void write(byte[] b) throws IOException
		{
			os.write(b);
			count += b.length;
		}

		public void write(int b) throws IOException
		{
			os.write(b);
			count++;
		}

		public void close() throws IOException
		{
			os.close();
		}

		public void flush() throws IOException
		{
			os.flush();
		}

		public long getByteCount()
		{
			return count;
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
    if (! base.canRead() || ! base.isDirectory())
      throw new CompilerException("Invalid base directory: "+base);
    
    // Usefull variables
    int i;
    String str;
    Iterator iter;
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
    iter = getLangpacksCodes(data).iterator();
    while (iter.hasNext())
    {
      str = (String) iter.next();

      // The language pack - first try to get stream directly (for standalone compiler)
      inStream = getClass().getResourceAsStream("/bin/langpacks/installer/"+str+".xml");
      if (inStream == null)
      {
        inStream = new FileInputStream(Compiler.IZPACK_HOME + "bin" + File.separator + "langpacks" +
          File.separator + "installer" + File.separator + str + ".xml");
      }
      packager.addLangPack(str, inStream);

      // The flag - try to get stream for standalone compiler
      inStream = getClass ().getResourceAsStream("/bin/langpacks/flags/"+str+".gif");
      if (inStream == null)
      {
        inStream = new FileInputStream(Compiler.IZPACK_HOME + "bin" + File.separator + "langpacks" +
          File.separator + "flags" + File.separator + str + ".gif");
      }
      packager.addResource("flag." + str, inStream);
      inStream.close ();
    }

    // We add the resources
    iter = getResources(data).iterator();
    while (iter.hasNext())
    {
      Resource res = (Resource) iter.next();
      if (res.parse)
      {
        if ((res.src == null) || (res.src_is != null))
        {
          System.err.println("ERROR: cannot parse resource from stream. (Internal error.)");
          packager.addResource(res.id, res.src_is);
        }
        
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
          inStream.close ();
        }
      }
      else
      {
        if (res.src != null)
        {
          inStream = new FileInputStream(res.src);
        }
        else
        {
          inStream = res.src_is;
        }
        packager.addResource(res.id, inStream);
        if (res.src != null)
          inStream.close ();
      }
    }

    // We add the native libraries
    iter = getNativeLibraries(data).iterator();
    while (iter.hasNext())
    {
      NativeLibrary nat = (NativeLibrary) iter.next();
      inStream = new FileInputStream(nat.path);
      packager.addNativeLibrary(nat.name, inStream);
    }

    // We add the additionnal jar files content
    iter = getJars(data).iterator();
    while (iter.hasNext())
      packager.addJarContent((String) iter.next());

    // We add the panels
    ArrayList panels = getPanels(data);

    TreeSet panelsCache = new TreeSet();
    iter = getPanels(data).iterator();
    while (iter.hasNext())
    {
	  Panel p = (Panel)iter.next();
      
      // We locate the panel classes directory
      str = p.className;
      
      // first try to get a Jar file for standalone compiler
      JarInputStream panel_is = null;
      
      try
      {
        InputStream jarInStream = getClass().getResourceAsStream("/bin/panels/"+str+".jar");
        if (jarInStream != null)
          panel_is = new JarInputStream (jarInStream);
      }
      catch (IOException e)
      {
        // for now, ignore this - try to read panel classes from filesystem
        panel_is = null; 
      }
      
      if (panel_is != null)
      {        
        if (panelsCache.contains(str)) continue;
        panelsCache.add(str);

        // now add files
        ZipEntry entry = null;
        
        while ((entry = panel_is.getNextEntry()) != null)
          packager.addPanelClass(entry.getName(), panel_is);        
      }
      else
      {
        File dir = new File(Compiler.IZPACK_HOME + "bin" + File.separator + "panels" + File.separator + str);
        if (!dir.exists())
          throw new Exception(str + " panel does not exist");

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
    }

    // We set the panels order
    packager.setPanelsOrder(panels);

	Map storedFiles = new HashMap(); 
    // We add the packs
    i = 0; //pack counter
    iter = getPacks(data).iterator();
    while (iter.hasNext())
    {
      Pack pack = (Pack) iter.next();
      ZipOutputStream zipOut = packager.addPack(i, pack.name, pack.osConstraints, pack.required, pack.description, pack.preselected);
	  zipOut.flush();//make sure buffers are flushed before we start counting  
	  ByteCountingOutputStream dos = new ByteCountingOutputStream(zipOut);//stream with byte counter
      ObjectOutputStream objOut = new ObjectOutputStream(dos);

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
        long mtime = f.lastModified();

        String targetFilename = p.getTargetFilename ();

        // pack paths in canonical (unix) form regardless of current host o/s:
        if('/' != File.separatorChar)
        {
            targetFilename = targetFilename.replace(File.separatorChar, '/');
        }

        // Writing
		PackFile pf = new PackFile(targetFilename, p.osConstraints, nbytes, mtime, p.override);
		long[] info = (long[])storedFiles.get(p.src);
		boolean addFile = true;
		if (info != null && packager.allowPackFileBackReferences())
		{
			pf.setPreviousPackFileRef((int)info[0],info[1]);
			addFile = false;
		}
        objOut.writeObject(pf);
		objOut.flush();//make sure it is written
        long pos = dos.getByteCount();//get the position
		if (addFile)
		{
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
			storedFiles.put(p.src,new long[]{i,pos});
		}
		packageBytes += nbytes;//aldo could be not really written we still want to know size.
		in.close();
      }
      packager.packAdded(i, packageBytes);

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

      // Write out information about executable files
      objOut.writeInt(pack.updatechecks.size());
      iter2 = pack.updatechecks.iterator();
      while (iter2.hasNext())
      {
        objOut.writeObject(iter2.next());
      }

      // Cleanup
      objOut.flush();
      zipOut.closeEntry();
      
      i++;
    }

    // We ask the packager to finish
    packager.finish();
    this.compileFailed = false;
  }

  public boolean wasSuccessful ()
  {
    return ! this.compileFailed;
  }

  /**
   *  Returns the GUIPrefs.
   *
   * @param  data           The XML data.
   * @return                The GUIPrefs.
   * @exception  Exception  Description of the Exception
   */
  protected GUIPrefs getGUIPrefs(XMLElement data) throws CompilerException
  {
    // We get the XMLElement & the values
    XMLElement gp = data.getFirstChildNamed("guiprefs");
    Integer integer;
    GUIPrefs p = new GUIPrefs();
    if (gp == null)
      return p;
    
    p.resizable = requireYesNoAttribute(gp, "resizable");
    p.width = requireIntAttribute(gp, "width");
    p.height = requireIntAttribute(gp, "height");
    
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
      jars.add(basedir + File.separator + requireAttribute(el, "src"));
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
  protected ArrayList getPacks(XMLElement data) throws CompilerException
  {
    // Initialisation
    ArrayList packs = new ArrayList();
    XMLElement root = requireChildNamed(data, "packs");

    // dummy variable used for values from XML
    String val;

    // We process each pack markup
    int packCounter = 0;
    Iterator packIter = root.getChildrenNamed("pack").iterator();
    while (packIter.hasNext())
    {
      XMLElement el = (XMLElement) packIter.next();

      // Trivial initialisations
      Pack pack = new Pack();
      pack.number = packCounter++;
      pack.name = requireAttribute(el, "name");
      pack.osConstraints = OsConstraint.getOsList (el); // TODO: unverified
      pack.required = requireYesNoAttribute(el, "required");
      pack.description = requireChildNamed(el, "description").getContent();
      pack.preselected = validateYesNoAttribute(el, "preselected", YES);

      // We get the parsables list
      Iterator iter = el.getChildrenNamed("parsable").iterator();
      while (iter.hasNext())
      {
          XMLElement p = (XMLElement) iter.next();
          String targetFile = requireAttribute(p, "targetfile");
          List osList = OsConstraint.getOsList (p); // TODO: unverified
          
          pack.parsables.add
            (new ParsableFile(targetFile,
                   p.getAttribute("type", "plain"),
                   p.getAttribute("encoding", null),
                   osList));
      }

      // We get the executables list
      iter = el.getChildrenNamed("executable").iterator();
      while (iter.hasNext())
      {
          XMLElement e = (XMLElement) iter.next();

          // when to execute this executable
          int executeOn = ExecutableFile.NEVER;
          val = e.getAttribute("stage", "never");
          if ("postinstall".equalsIgnoreCase(val))
            executeOn = ExecutableFile.POSTINSTALL;
          else if ("uninstall".equalsIgnoreCase(val))
            executeOn = ExecutableFile.UNINSTALL;

          // main class  of this executable
          String executeClass = e.getAttribute("class");

          // type of this executable
          int executeType = ExecutableFile.BIN;
          val = e.getAttribute("type", "bin");
          if ("jar".equalsIgnoreCase(val))
            executeType = ExecutableFile.JAR;

          // what to do if execution fails
          int onFailure = ExecutableFile.ASK;
          val = e.getAttribute("failure", "ask");
          if ("abort".equalsIgnoreCase(val))
            onFailure = ExecutableFile.ABORT;
          else if ("warn".equalsIgnoreCase(val))
            onFailure = ExecutableFile.WARN;

          // whether to keep the executable after executing it
          boolean keepFile = false;
          val = e.getAttribute ("keep");
          if ("true".equalsIgnoreCase(val))
            keepFile = true;

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
              argList.add(requireAttribute(arg, "value"));
            }
          }

          List osList = OsConstraint.getOsList(e); // TODO: unverified
          
          String targetfile_attr = requireAttribute(e, "targetfile");

          pack.executables.add(new ExecutableFile(targetfile_attr,
            executeType, executeClass,
            executeOn, onFailure, argList, osList, keepFile));
      }

      // We get the files list
      iter = el.getChildrenNamed("file").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        String src_attr = requireAttribute(f, "src");
        String path;

        if (new File(src_attr).isAbsolute())
          path = src_attr;
        else
          path = basedir + File.separator + src_attr;

        File file = new File(path);
        int override = getOverrideValue (f);
        List osList = OsConstraint.getOsList (f); // TODO: unverified
        String targetdir_attr = requireAttribute (f, "targetdir");
        
        try
        {
          addFile(file,
            targetdir_attr,
            osList,
            override,
            pack.packFiles);
        }
        catch (CompilerException x)
        {
          parseError(f, x.getMessage(), x);
        }
      }

      // We get the singlefiles list
      iter = el.getChildrenNamed("singlefile").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        String src_attr = requireAttribute (f, "src");
        String path;

        if (new File (src_attr).isAbsolute ())
          path = src_attr;
        else
          path = basedir + File.separator + src_attr;

        File file = new File(path);
        int override = getOverrideValue (f);
        List osList = OsConstraint.getOsList (f); // TODO: unverified
        String target_attr = requireAttribute (f, "target");

        try
        {
          addSingleFile(file,
            target_attr,
            osList,
            override,
            pack.packFiles);
        }
        catch (CompilerException x)
        {
          parseError(f, x.getMessage(), x);
        }
      }

      // We get the fileset list
      iter = el.getChildrenNamed("fileset").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        String dir_attr = requireAttribute (f, "dir");
        String path;

        if (new File(dir_attr).isAbsolute())
          path = dir_attr;
        else
          path = basedir + File.separator + dir_attr;

        boolean casesensitive = validateYesNoAttribute(f, "casesensitive", YES);
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
            includes[j] = requireAttribute(xclude, "name");
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
            excludes[j] = requireAttribute(xclude, "name");
          }
        }

        int override = getOverrideValue (f);
        String targetdir_attr = requireAttribute(f, "targetdir");
        List osList = OsConstraint.getOsList (f); // TODO: unverified

        try {
          addFileSet(path, includes, excludes,
            targetdir_attr,
            osList,
            pack.packFiles,
            casesensitive,
            override);
        }
        catch (CompilerException x)
        {
          parseError(f, x.getMessage(), x);
        }
      }

      // get the updatechecks list
      // We get the fileset list
      iter = el.getChildrenNamed("updatecheck").iterator();
      while (iter.hasNext())
      {
        XMLElement f = (XMLElement) iter.next();
        
        String casesensitive = f.getAttribute("casesensitive");
        //  get includes and excludes
        
        ArrayList includesList = new ArrayList ();
                
        Vector includesElementList = f.getChildrenNamed("include");
        
        if (includesElementList != null)
        {
          for (Iterator include_it = includesElementList.iterator(); include_it.hasNext();)
          {
            XMLElement inc_el = (XMLElement)include_it.next();
            
            String name = inc_el.getAttribute("name");
            
            if (name != null)
            {  
              includesList.add (name);
            }
            else
            {
              parseError(inc_el, "missing \"name\" attribute for <include> in <updatecheck>");              
            }
          }
        }
        
        ArrayList excludesList = new ArrayList ();
        
        Vector excludesElementList = f.getChildrenNamed("exclude");
        
        if (excludesElementList != null)
        {
          for (Iterator exclude_it = excludesElementList.iterator(); exclude_it.hasNext();)
          {
            XMLElement excl_el = (XMLElement)exclude_it.next();
            
            String name = excl_el.getAttribute("name");
            
            if (name != null)
            {  
              excludesList.add (name);
            }
            else
            {
              parseError(excl_el, "missing \"name\" attribute for <exclude> in <updatecheck>");              
            }
          }
        }
        
        pack.updatechecks.add (new UpdateCheck (includesList, excludesList, casesensitive));
      }
        
      // We add the pack
      packs.add(pack);
    }
    if (packs.isEmpty())
      parseError (root, "<packs> requires a <pack>");

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
   * @param  osList         The target os constraints.
   * @param  list           The files list.
   * @param  casesensitive  Case-sensitive stuff.
   * @param  override       Behaviour if a file already exists during install
   * @exception  Exception  Description of the Exception
   */
  protected void addFileSet(String path, String[] includes, String[] excludes,
                            String relPath, List osList, ArrayList list, 
                            boolean casesensitive, int override)
     throws CompilerException
  {
    File test = new File(path);
    if (test.isDirectory())
    {
      DirectoryScanner ds = new DirectoryScanner();
      ds.setIncludes(includes);
      ds.setExcludes(excludes);
      ds.setBasedir(new File(path));
      ds.setCaseSensitive(casesensitive);
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
        addFile(file, instPath, osList, override, list);
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
          addFile(dir, instPath, osList, override, list);
        }
      }
    }
    else
      throw new CompilerException("\"dir\" attribute of fileset is not valid: " + path);
  }


  /**
   *  Recursive method to add files in a pack.
   *
   * @param  file           The file to add.
   * @param  relPath        The relative path.
   * @param  osList         The target OS constraints.
   * @param  override       Overriding behaviour.
   * @param  list           The files list.
   * @exception  Exception  Description of the Exception
   */
  protected void addFile(File file, String relPath, List osList,
                         int override, ArrayList list) throws CompilerException
  {
    // We check if 'file' is correct
    if (!file.exists())
      throw new CompilerException(file.toString() + " does not exist");

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
        addFile(files[i], np, osList, override, list);
    }
    else
    {
      PackSource nf = new PackSource();
      nf.src = file.getAbsolutePath();
	  nf.setTargetDir (relPath);
      nf.osConstraints = osList;
      nf.override = override;
      debug("Adding file: "+ nf.toString());
      list.add(nf);
    }
  }

  private void debug(String s)
  {
	if (Debug.tracing())//if you are wondering what files gets added to the installer
	{
	  packagerListener.packagerMsg(s);
	}
  }

  /**
   *  Method to add a single file in a pack.
   *
   * @param  file           The file to add.
   * @param  targetFile     The target to add the file as.
   * @param  osList         The target OS constraints.
   * @param  override       Overriding behaviour.
   * @param  list           The files list.
   * @exception  Exception  Description of the Exception
   */
  protected void addSingleFile(File file, String targetFile, List osList,
                         int override, ArrayList list) throws CompilerException
  {
    //System.out.println ("adding single file " + file.getName() + " as " + targetFile);
    PackSource nf = new PackSource();
    nf.src = file.getAbsolutePath();
    nf.setTargetFile (targetFile);
    nf.osConstraints = osList;
    nf.override = override;
    list.add(nf);
  }

  /**
   *  Returns a list of the panels names to add.
   *
   * @param  data           The XML data.
   * @return                The panels list.
   * @exception  Exception  Description of the Exception
   */
  protected ArrayList getPanels(XMLElement data) throws CompilerException
  {
    // Initialisation
    ArrayList panels = new ArrayList();
    XMLElement root = requireChildNamed(data, "panels");

    // We process each langpack markup
    Iterator iter = root.getChildrenNamed("panel").iterator();
    while (iter.hasNext())
    {
      XMLElement xmlPanel = (XMLElement) iter.next();
	  List osList = OsConstraint.getOsList (xmlPanel);
	  Panel panel = new Panel();
	  panel.osConstraints = osList;
	  panel.className = xmlPanel.getAttribute("classname");
      panels.add(panel);
    }
    if (panels.isEmpty())
      parseError (root, "<panels> requires a <panel>");

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
  protected ArrayList getResources(XMLElement data) throws CompilerException
  {
    // Initialisation
    ArrayList resources = new ArrayList();
    XMLElement root = data.getFirstChildNamed("resources");
    
    if (root == null) return resources;

    // We process each res markup
    Iterator iter = root.getChildrenNamed("res").iterator();
    while (iter.hasNext())
    {
      XMLElement res = (XMLElement) iter.next();

      // Do not prepend basedir if src is already an absolute path
      File src = new File(requireAttribute(res, "src"));
      if (! src.isAbsolute())
        src = new File(basedir, src.getPath());

      resources.add(new Resource(requireAttribute(res, "id"),
        src.getPath(),
        validateYesNoAttribute(res, "parse", NO),
        res.getAttribute("type"),
        res.getAttribute("encoding")));
    }
    if (resources.isEmpty())
      parseError (root, "<resources> requires a <res>");

    // Uninstaller must be added as a resource for standalone compiler
    InputStream uninst_is = getClass().getResourceAsStream("/lib/uninstaller.jar");

    if (uninst_is == null)
    {
      String uninst = Compiler.IZPACK_HOME + "lib" + File.separator + "uninstaller.jar";
      try
      {
        uninst_is = new FileInputStream (uninst);
      }
      catch (IOException x)
      {
        // it's a runtime exception if this can't be found
        throw new RuntimeException ("The uninstaller ("+uninst+") seems to be missing: "+x.toString());
      }
    }
    resources.add(new Resource("IzPack.uninstaller", uninst_is));
    
    // We return the ArrayList
    return resources;
  }


  /**
   *  Returns a list of the ISO3 codes of the langpacks to include.
   *
   * @param  data           The XML data.
   * @return                The ISO 3 codes list.
   */
  protected ArrayList getLangpacksCodes(XMLElement data) throws CompilerException
  {
    // Initialisation
    ArrayList langpacks = new ArrayList();
    XMLElement locals = requireChildNamed(data, "locale");

    // We process each langpack markup
    Iterator iter = locals.getChildrenNamed("langpack").iterator();
    while (iter.hasNext())
    {
      XMLElement pack = (XMLElement) iter.next();
      langpacks.add(requireAttribute(pack, "iso3"));
    }
    if (langpacks.isEmpty())
      parseError (locals, "<locale> requires a <langpack>");
  
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
    XMLElement root = requireChildNamed(data, "info");

    info.setAppName(requireContent(requireChildNamed(root, "appname")));
    info.setAppVersion(requireContent(requireChildNamed(root, "appversion")));
    info.setAppURL(requireContent(requireChildNamed(root, "url")));

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
      info.setJavaVersion(javaVersion.getContent());

    XMLElement uninstallInfo = root.getFirstChildNamed ("uninstaller");
    if (uninstallInfo != null)
      info.setWriteUninstaller(validateYesNoAttribute(uninstallInfo, "write", YES));
    
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
    this.varMap = new VariableValueMapImpl ();
    
    Properties retVal = null;

    // We get the varible list
    XMLElement root = data.getFirstChildNamed("variables");
    if (root == null)
      return retVal;

    retVal = new Properties();
    Iterator iter = root.getChildrenNamed("variable").iterator();
    while (iter.hasNext())
    {
      XMLElement var = (XMLElement) iter.next();
      retVal.setProperty(requireAttribute(var, "name"), requireAttribute(var, "value"));
      varMap.setVariable(requireAttribute(var, "name"), requireAttribute(var, "value"));
    }

    return retVal;
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
    }
    catch (Exception x)
    {
      throw new CompilerException("Error parsing installation file", x);
    }

    // We check it
    if (!"installation".equalsIgnoreCase(data.getName()))
      parseError(data, "this is not an IzPack XML installation file");
    if (!requireAttribute(data, "version").equalsIgnoreCase(VERSION))
      parseError(data, "the file version is different from the compiler version");

    // We finally return the tree
    return data;
  }

  protected int getOverrideValue (XMLElement f)
  {
    int override = PackSource.OVERRIDE_UPDATE;

    String override_val = f.getAttribute("override");
    if (override_val != null)
    {
      if (override_val.equalsIgnoreCase("true"))
      {
        override = PackSource.OVERRIDE_TRUE;
      }
      else if (override_val.equalsIgnoreCase("false"))
      {
        override = PackSource.OVERRIDE_FALSE;
      }
      else if (override_val.equalsIgnoreCase("asktrue"))
      {
        override = PackSource.OVERRIDE_ASK_TRUE;
      }
      else if (override_val.equalsIgnoreCase("askfalse"))
      {
        override = PackSource.OVERRIDE_ASK_FALSE;
      }
      else if (override_val.equalsIgnoreCase("update"))
      {
        override = PackSource.OVERRIDE_UPDATE;
      }
    }

    return override;
  }


  /**
   * Create parse error with consistent messages. Includes file name and line #
   * of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the error occured
   * @param message Brief message explaining error
   */
  protected void parseError (XMLElement parent, String message)
    throws CompilerException
  {
    this.compileFailed = true;
    throw new CompilerException(filename+":"+parent.getLineNr()+": "+message);
  }

  /**
   * Create a chained parse error with consistent messages. Includes file name
   * and line # of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the error occured
   * @param message Brief message explaining error
   */
  protected void parseError (XMLElement parent, String message, Throwable cause)
    throws CompilerException
  {
    this.compileFailed = true;
    throw new CompilerException(filename+":"+parent.getLineNr()+": "+message, cause);
  }
  
  /**
   * Create a parse warning with consistent messages. Includes file name
   * and line # of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the warning occured
   * @param message Warning message
   */
  protected void parseWarn (XMLElement parent, String message)
  {
    System.out.println (filename+":"+parent.getLineNr()+": "+message);
  }


  /**
   * Call getFirstChildNamed on the parent, producing a meaningful error
   * message on failure. It is an error for 'parent' to be null.
   *
   * @param parent  The element to search for a child
   * @param child   Name of the child element to get
   */
  protected XMLElement requireChildNamed (XMLElement parent, String name)
    throws CompilerException
  {
    XMLElement child = parent.getFirstChildNamed(name);
    if (child == null)
      parseError(parent, "<"+parent.getName()+"> requires child <"+name+">");
    return child;
  }


  /**
   * Call getContent on an element, producing a meaningful error message if not
   * present, or empty. It is an error for 'element' to be null.
   *
   * @param element   The element to get content of
   */
  protected String requireContent (XMLElement element)
    throws CompilerException
  {
    String content = element.getContent();
    if (content == null || content.length() == 0)
      parseError(element, "<"+element.getName()+"> requires content");
    return content;
  }


  /**
   * Call getAttribute on an element, producing a meaningful error message if
   * not present, or empty. It is an error for 'element' or 'attribute' to be null.
   *
   * @param element   The element to get the attribute value of
   * @param attribute The name of the attribute to get
   */
  protected String requireAttribute (XMLElement element, String attribute)
    throws CompilerException
  {
    String value = element.getAttribute(attribute);
    if (value == null)
      parseError(element, "<"+element.getName()+"> requires attribute '"+attribute+"'");
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
  protected int requireIntAttribute (XMLElement element, String attribute)
    throws CompilerException
  {
    String value = element.getAttribute(attribute);
    if (value == null || value.length() == 0)
      parseError(element, "<"+element.getName()+"> requires attribute '"+attribute+"'");
    try
    {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException x)
    {
      parseError(element, "'"+attribute+"' must be an integer");
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
    String value = requireAttribute(element,attribute);
    if (value.equalsIgnoreCase("yes"))
      return true;
    if (value.equalsIgnoreCase("no"))
      return false;
    
    parseError(element, "<"+element.getName()+"> invalid attribute '"+
               attribute+"': Expected (yes|no)");
    
    return false; // never happens
  }


  /**
   * Call getAttribute on an element, producing a meaningful warning if not
   * "yes" or "no". It is an error for 'element' or 'attribute' to be null.
   *
   * @param element        The element to get the attribute value of
   * @param attribute      The name of the attribute to get
   * @param defaultValue   Value returned if attribute not present or invalid
   */
  protected boolean validateYesNoAttribute(XMLElement element, String attribute,
                                           boolean defaultValue)
  {
    String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
    if (value.equalsIgnoreCase("yes"))
      return true;
    if (value.equalsIgnoreCase("no"))
      return false;

    // TODO: should this be an error if it's present but "none of the above"?
    parseWarn(element, "<"+element.getName()+"> invalid attribute '"+
              attribute+"': Expected (yes|no) if present");

    return defaultValue;
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

    /**  The input stream to read from. */
    public InputStream src_is;
    
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
     * @param  id   The Id.
     * @param  is   The source.
     */
    public Resource(String id, InputStream is)
    {
      this.src_is = is;
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
    public ArrayList packFiles = new ArrayList();

    /**  The parsable files list. */
    public ArrayList parsables = new ArrayList ();

    /**  The executable files list. */
    public ArrayList executables = new ArrayList ();

    /**  The list of update checks. */
    public ArrayList updatechecks = new ArrayList ();
    
    /**  The target operation system of this file */
    public List osConstraints = null;

    public boolean preselected;

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

    public final static int OVERRIDE_TRUE = com.izforge.izpack.PackFile.OVERRIDE_TRUE;
    public final static int OVERRIDE_FALSE = com.izforge.izpack.PackFile.OVERRIDE_FALSE;
    public final static int OVERRIDE_ASK_TRUE = com.izforge.izpack.PackFile.OVERRIDE_ASK_TRUE;
    public final static int OVERRIDE_ASK_FALSE = com.izforge.izpack.PackFile.OVERRIDE_ASK_FALSE;
    public final static int OVERRIDE_UPDATE = com.izforge.izpack.PackFile.OVERRIDE_UPDATE;

    /**  Shall we override the file ? */
    public int override = OVERRIDE_TRUE;

    /**  The target operating systems of this file */
    public List osConstraints = null;

    /**  The target directory. */
    private String targetdir = null;

    /**  The target file. */
    private String targetfile = null;

    public String getTargetFilename ()
    {
      // targetfile overrides targetdir
      if (this.targetfile != null)
        return this.targetfile;

       File f = new File (this.src);

       return this.targetdir + f.getName ();
    }

    public void setTargetFile (String targetFile) throws CompilerException
    {
      this.targetdir = null;
      if (targetFile == null)
      {
        if (this.src != null)
          throw new CompilerException ("target for file " + src + " missing!");
        else
          throw new CompilerException ("target for file missing!");
      }
      this.targetfile = targetFile;
    }

    public String getTargetFile ()
    {
      return this.targetfile;
    }

    public void setTargetDir (String targetDir) throws CompilerException
    {
      if (targetDir == null)
      {
        if (this.src != null)
          throw new CompilerException ("target for file " + src + " missing!");
        else
          throw new CompilerException ("target for file missing!");
      }

      this.targetfile = null;
      this.targetdir = targetDir;

      if (! this.targetdir.endsWith (File.separator))
      {
        this.targetdir = this.targetdir + File.separatorChar;
      }

    }

    public String getTargetDir ()
    {
      return this.targetdir;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[PackSource file: '"+src+"' with path '"+targetdir+"']";
	}
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
        System.out.println("               default is the xml file name\n");
        
		System.out.println("   When using vm option -DSTACKTRACE=true there is all kind of debug info ");
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
          Thread.sleep(100);
        
        if (compiler.wasSuccessful())
          exitCode = 0;
        
        System.out.println("Build time: "+new Date());
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

