/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

import com.izforge.izpack.*;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraint;

/**
 *  Unpacker class.
 *
 * @author     Julien Ponge
 * @author     Johannes Lehtinen
 */
public class Unpacker extends Thread
{
  /**  The installdata. */
  private AutomatedInstallData idata;

  /**  The installer listener. */
  private AbstractUIProgressHandler handler;

  /**  The uninstallation data. */
  private UninstallData udata;

  /**  The variables substitutor. */
  private VariableSubstitutor vs;

  /**  The instances of the unpacker objects. */
  private static ArrayList instances = new ArrayList();

  /**  The absolute path of the installation. (NOT the canonical!) */
  private File absolute_installpath;

  /** The packs locale database. */
  private LocaleDatabase langpack = null;

  /** The name of the XML file that specifies the panel langpack */
  private static final String LANG_FILE_NAME = "packsLang.xml";

  /**
   *  The constructor.
   *
   * @param  idata     The installation data.
   * @param  handler   The installation progress handler.
   */
  public Unpacker(
    AutomatedInstallData idata,
    AbstractUIProgressHandler handler)
  {
    super("IzPack - Unpacker thread");
    try
    {
      String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
      this.langpack = new LocaleDatabase(ResourceManager.getInstance().getInputStream(resource));
    }
    catch (Throwable exception)
    {}

    this.idata = idata;
    this.handler = handler;

    // Initialize the variable substitutor
    vs = new VariableSubstitutor(idata.getVariables());
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
      //
      // Initialisations
      FileOutputStream out = null;
      ArrayList parsables = new ArrayList();
      ArrayList executables = new ArrayList();
      ArrayList updatechecks = new ArrayList();
      List packs = idata.selectedPacks;
      int npacks = packs.size();
      handler.startAction("Unpacking", npacks);
      udata = UninstallData.getInstance();
      // Custom action listener stuff --- load listeners ----
      List [] customActions = getCustomActions();
      // Custom action listener stuff --- beforePacks ----
      informListeners(customActions, InstallerListener.BEFORE_PACKS,idata, 
        new Integer(npacks), handler);

      // We unpack the selected packs
      for (int i = 0; i < npacks; i++)
      {
        // We get the pack stream
        int n = idata.allPacks.indexOf(packs.get(i));

        // Custom action listener stuff --- beforePack ----
        informListeners(customActions, InstallerListener.BEFORE_PACK,packs.get(i),
          new Integer(npacks), handler);
        ObjectInputStream objIn = new ObjectInputStream(getPackAsStream(n));

        // We unpack the files
        int nfiles = objIn.readInt();

          //We get the internationalized name of the pack
          final Pack pack = ((Pack) packs.get(i));
          String stepname = pack.name;//the message to be passed to the installpanel
          if(langpack !=null && !(pack.id == null || pack.id.equals("")) )
          {

            final String name = langpack.getString(pack.id);
            if(name !=null && !name.equals(""))
            {
              stepname = name;
            }
          }
          handler.nextStep(stepname, i + 1, nfiles);
        for (int j = 0; j < nfiles; j++)
        {
          // We read the header
          PackFile pf = (PackFile) objIn.readObject();

          if (OsConstraint.oneMatchesCurrentSystem(pf.osConstraints()))
          {
            // We translate & build the path
            String path = translatePath(pf.getTargetPath());
            File pathFile = new File(path);
            File dest = pathFile;
            if (! pf.isDirectory())
              dest = pathFile.getParentFile();

            if (!dest.exists())
            {
              // If there are custom actions which would be called at
              // creating a directory, create it recursively.
              List fileListeners = customActions[customActions.length - 1];
              if( fileListeners != null && fileListeners.size() > 0 )
                mkDirsWithEnhancement(dest, pf, customActions );
              else  // Create it in on step.
              {
                if (!dest.mkdirs())
                {
                  handler.emitError(
                    "Error creating directories",
                    "Could not create directory\n" + dest.getPath());
                  handler.stopAction();
                  return;
                }
              }
            }

            if (pf.isDirectory())
              continue;

            // Custom action listener stuff --- beforeFile ----
            informListeners(customActions, InstallerListener.BEFORE_FILE,pathFile,
              pf, null);
            // We add the path to the log,
            udata.addFile(path);

            handler.progress(j, path);

            //if this file exists and should not be overwritten, check
            //what to do
            if ((pathFile.exists()) && (pf.override() != PackFile.OVERRIDE_TRUE))
            {
              boolean overwritefile = false;

              // don't overwrite file if the user said so
              if (pf.override() != PackFile.OVERRIDE_FALSE)
              {
                if (pf.override() == PackFile.OVERRIDE_TRUE)
                {
                  overwritefile = true;
                } else if (pf.override() == PackFile.OVERRIDE_UPDATE)
                {
                  // check mtime of involved files
                  // (this is not 100% perfect, because the already existing file might
                  // still be modified but the new installed is just a bit newer; we would
                  // need the creation time of the existing file or record with which mtime
                  // it was installed...)
                  overwritefile = (pathFile.lastModified() < pf.lastModified());
                } else
                {
                  int def_choice = -1;

                  if (pf.override() == PackFile.OVERRIDE_ASK_FALSE)
                    def_choice = AbstractUIHandler.ANSWER_NO;
                  if (pf.override() == PackFile.OVERRIDE_ASK_TRUE)
                    def_choice = AbstractUIHandler.ANSWER_YES;

                  int answer =
                    handler.askQuestion(
                      idata.langpack.getString("InstallPanel.overwrite.title")
                        + pathFile.getName(),
                      idata.langpack.getString(
                        "InstallPanel.overwrite.question")
                        + pathFile.getAbsolutePath(),
                      AbstractUIHandler.CHOICES_YES_NO,
                      def_choice);

                  overwritefile = (answer == AbstractUIHandler.ANSWER_YES);
                }

              }

              if (!overwritefile)
              {
                if (!pf.isBackReference() && !((Pack)packs.get(i)).loose)
                  objIn.skip(pf.length());
                continue;
              }

            }

            // We copy the file
            out = new FileOutputStream(pathFile);
            byte[] buffer = new byte[5120];
            long bytesCopied = 0;
            InputStream pis = objIn;
            if (pf.isBackReference())
            {
              InputStream is = getPackAsStream(pf.previousPackNumber);
              pis = new ObjectInputStream(is);
              //must wrap for blockdata use by objectstream (otherwise strange result)
              //skip on underlaying stream (for some reason not possible on ObjectStream)
              is.skip(pf.offsetInPreviousPack - 4);
              //but the stream header is now already read (== 4 bytes)
            }
            else if(((Pack)packs.get(i)).loose)
            {
              pis = new FileInputStream(pf.sourcePath);
            }
            while (bytesCopied < pf.length())
            {
              int maxBytes = (int)Math.min(pf.length() - bytesCopied, buffer.length);
              int bytesInBuffer = pis.read(buffer, 0, maxBytes);
              if (bytesInBuffer == -1)
                throw new IOException("Unexpected end of stream");

              out.write(buffer, 0, bytesInBuffer);

              bytesCopied += bytesInBuffer;
            }
            // Cleanings
            out.close();
            if (pis != objIn)
              pis.close();

            // Set file modification time if specified
            if (pf.lastModified() >= 0)
              pathFile.setLastModified(pf.lastModified());
            // Custom action listener stuff --- afterFile ----
            informListeners(customActions, InstallerListener.AFTER_FILE,pathFile,
              pf, null);


          } else
          {
            if (!pf.isBackReference())
              objIn.skip(pf.length());
          }
        }

        // Load information about parsable files
        int numParsables = objIn.readInt();
        for (int k = 0; k < numParsables; k++)
        {
          ParsableFile pf = (ParsableFile) objIn.readObject();
          pf.path = translatePath(pf.path);
          parsables.add(pf);
        }

        // Load information about executable files
        int numExecutables = objIn.readInt();
        for (int k = 0; k < numExecutables; k++)
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
          if (ef.executionStage == ExecutableFile.UNINSTALL)
          {
            udata.addExecutable(ef);
          }
        }
        // Custom action listener stuff --- uninstall data ----
        handleAdditionalUninstallData(udata, customActions);

        // Load information about updatechecks
        int numUpdateChecks = objIn.readInt();

        for (int k = 0; k < numUpdateChecks; k++)
        {
          UpdateCheck uc = (UpdateCheck) objIn.readObject();

          updatechecks.add(uc);
        }

        objIn.close();
        // Custom action listener stuff --- afterPack ----
        informListeners(customActions, InstallerListener.AFTER_PACK,
          packs.get(i), new Integer(i), handler);
      }

      // We use the scripts parser
      ScriptParser parser = new ScriptParser(parsables, vs);
      parser.parseFiles();

      // We use the file executor
      FileExecutor executor = new FileExecutor(executables);
      if (executor.executeFiles(ExecutableFile.POSTINSTALL, handler) != 0)
        handler.emitError(
          "File execution failed",
          "The installation was not completed");

      // We put the uninstaller (it's not yet complete...)
      putUninstaller();

      // update checks _after_ uninstaller was put, so we don't delete it
      performUpdateChecks(updatechecks);

      // Custom action listener stuff --- afterPacks ----
      informListeners(customActions, InstallerListener.AFTER_PACKS,idata, 
        handler, null);

      // The end :-)
      handler.stopAction();
    } catch (Exception err)
    {
      // TODO: finer grained error handling with useful error messages
      handler.stopAction();
      handler.emitError("An error occured", err.toString());
      err.printStackTrace();
    } finally
    {
      instances.remove(instances.indexOf(this));
    }
  }

  /**
   * @param updatechecks
   */
  private void performUpdateChecks(ArrayList updatechecks)
  {
    ArrayList include_patterns = new ArrayList();
    ArrayList exclude_patterns = new ArrayList();

    RECompiler recompiler = new RECompiler();

    this.absolute_installpath =
      new File(idata.getInstallPath()).getAbsoluteFile();

    // at first, collect all patterns
    for (Iterator iter = updatechecks.iterator(); iter.hasNext();)
    {
      UpdateCheck uc = (UpdateCheck) iter.next();

      if (uc.includesList != null)
        include_patterns.addAll(preparePatterns(uc.includesList, recompiler));

      if (uc.excludesList != null)
        exclude_patterns.addAll(preparePatterns(uc.excludesList, recompiler));
    }

    // do nothing if no update checks were specified
    if (include_patterns.size() == 0)
      return;

    // now collect all files in the installation directory and figure
    // out files to check for deletion

    // use a treeset for fast access
    TreeSet installed_files = new TreeSet();

    for (Iterator if_it = this.udata.getFilesList().iterator();
      if_it.hasNext();
      )
    {
      String fname = (String) if_it.next();

      File f = new File(fname);

      if (!f.isAbsolute())
      {
        f = new File(this.absolute_installpath, fname);
      }

      installed_files.add(f.getAbsolutePath());
    }

    // now scan installation directory (breadth first), contains Files of directories to scan
    // (note: we'll recurse infinitely if there are circular links or similar nasty things)
    Stack scanstack = new Stack();

    // contains File objects determined for deletion
    ArrayList files_to_delete = new ArrayList();

    try
    {
      scanstack.add(absolute_installpath);

      while (!scanstack.empty())
      {
        File f = (File) scanstack.pop();

        File[] files = f.listFiles();

        if (files == null)
        {
          throw new IOException(f.getPath() + "is not a directory!");
        }

        for (int i = 0; i < files.length; i++)
        {
          File newf = files[i];

          String newfname = newf.getPath();

          // skip files we just installed
          if (installed_files.contains(newfname))
            continue;

          if (fileMatchesOnePattern(newfname, include_patterns)
            && (!fileMatchesOnePattern(newfname, exclude_patterns)))
          {
            files_to_delete.add(newf);
          }

          if (newf.isDirectory())
          {
            scanstack.push(newf);
          }

        }
      }
    } catch (IOException e)
    {
      this.handler.emitError(
        "error while performing update checks",
        e.toString());
    }

    for (Iterator f_it = files_to_delete.iterator(); f_it.hasNext();)
    {
      File f = (File) f_it.next();

      if (!f.isDirectory())
        // skip directories - they cannot be removed safely yet
      {
        this.handler.emitNotification("deleting " + f.getPath());
        f.delete();
      }

    }

  }

  /**
   * @param filename
   * @param patterns
   * 
   * @return true if the file matched one pattern, false if it did not
   */
  private boolean fileMatchesOnePattern(String filename, ArrayList patterns)
  {
    // first check whether any include matches
    for (Iterator inc_it = patterns.iterator(); inc_it.hasNext();)
    {
      RE pattern = (RE) inc_it.next();

      if (pattern.match(filename))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * @param list A list of file name patterns (in ant fileset syntax)
   * @param recompiler The regular expression compiler (used to speed up RE compiling).
   * 
   * @return List of org.apache.regexp.RE
   */
  private List preparePatterns(ArrayList list, RECompiler recompiler)
  {
    ArrayList result = new ArrayList();

    for (Iterator iter = list.iterator(); iter.hasNext();)
    {
      String element = (String) iter.next();

      if ((element != null) && (element.length() > 0))
      {
        // substitute variables in the pattern
        element = this.vs.substitute(element, "plain");

        // check whether the pattern is absolute or relative
        File f = new File(element);

        // if it is relative, make it absolute and prepend the installation path
        // (this is a bit dangerous...)
        if (!f.isAbsolute())
        {
          element = new File(this.absolute_installpath, element).toString();
        }

        // now parse the element and construct a regular expression from it
        // (we have to parse it one character after the next because every
        // character should only be processed once - it's not possible to get this
        // correct using regular expression replacing)
        StringBuffer element_re = new StringBuffer();

        int lookahead = -1;

        int pos = 0;

        while (pos < element.length())
        {
          char c;

          if (lookahead != -1)
          {
            c = (char) lookahead;
            lookahead = -1;
          } else
            c = element.charAt(pos++);

          switch (c)
          {
            case '/' :
              {
                element_re.append(File.separator);
                break;
              }
              // escape backslash and dot
            case '\\' :
            case '.' :
              {
                element_re.append("\\");
                element_re.append(c);
                break;
              }
            case '*' :
              {
                if (pos == element.length())
                {
                  element_re.append("[^" + File.separator + "]*");
                  break;
                }

                lookahead = element.charAt(pos++);

                // check for "**"
                if (lookahead == '*')
                {
                  element_re.append(".*");
                  // consume second star
                  lookahead = -1;
                } else
                {
                  element_re.append("[^" + File.separator + "]*");
                  // lookahead stays there
                }
                break;
              }
            default :
              {
                element_re.append(c);
                break;
              }
          } // switch

        }

        // make sure that the whole expression is matched
        element_re.append('$');

        // replace \ by \\ and create a RE from the result
        try
        {
          result.add(new RE(recompiler.compile(element_re.toString())));
        } catch (RESyntaxException e)
        {
          this.handler.emitNotification(
            "internal error: pattern \""
              + element
              + "\" produced invalid RE \""
              + f.getPath()
              + "\"");
        }

      }
    }

    return result;
  }

  /**
   *  Puts the uninstaller.
   *
   * @exception  Exception  Description of the Exception
   */
  private void putUninstaller() throws Exception
  {
    // get the uninstaller base, returning if not found so that
    // idata.uninstallOutJar remains null
    InputStream [] in = new InputStream[2];
    in[0] = Unpacker.class.getResourceAsStream("/res/IzPack.uninstaller");
    if (in[0] == null)
      return;
    // The uninstaller extension is facultative; it will be exist only
    // if a native library was marked for uninstallation.
    in[1] = Unpacker.class.getResourceAsStream("/res/IzPack.uninstaller-ext");

    // Me make the .uninstaller directory
    String dest =
      translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";
    String jar = dest + File.separator + "uninstaller.jar";
    File pathMaker = new File(dest);
    pathMaker.mkdirs();

    // We log the uninstaller deletion information
    udata.setUninstallerJarFilename(jar);
    udata.setUninstallerPath(dest);

    // We open our final jar file
    FileOutputStream out = new FileOutputStream(jar);
    ZipOutputStream outJar = new ZipOutputStream(out);
    idata.uninstallOutJar = outJar;
    outJar.setLevel(9);
    udata.addFile(jar);

    // We copy the uninstallers
    HashSet doubles = new HashSet();
        
    for( int i = 0; i < in.length; ++i )
    {
      if( in[i] == null )
        continue;
      ZipInputStream inRes = new ZipInputStream(in[i]);
      ZipEntry zentry = inRes.getNextEntry();
      while (zentry != null)
      {
        // Puts a new entry, but not twice like META-INF
        if( ! doubles.contains(zentry.getName()))
        {
          doubles.add(zentry.getName());
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
        }
        zentry = inRes.getNextEntry();
      }
      inRes.close();
    }

    // We put the langpack
    InputStream in2 =
    Unpacker.class.getResourceAsStream("/langpacks/" + idata.localeISO3 + ".xml");
    outJar.putNextEntry(new ZipEntry("langpack.xml"));
    int read = in2.read();
    while (read != -1)
    {
      outJar.write(read);
      read = in2.read();
    }
    outJar.closeEntry();
  }

  /**
   *  Returns a stream to a pack, location depending on if it's web based.
   *
   * @param  n              The pack number.
   * @return                The stream or null if it could not be found.
   * @exception  Exception  Description of the Exception
   */
  private InputStream getPackAsStream(int n) throws Exception
  {
    InputStream in = null;

    String webDirURL = idata.info.getWebDirURL();
    
    if (webDirURL == null) // local
    {
      in = Unpacker.class.getResourceAsStream("/packs/pack" + n);
    }
    else // web based
    {
      // TODO: Look first in same directory as primary jar
      //       This may include prompting for changing of media
      // TODO: download and cache them all before starting copy process
      
      // See compiler.Packager#getJarOutputStream for the counterpart
      String baseName = idata.info.getInstallerBase();
      String packURL = webDirURL + "/" + baseName + ".pack" + n + ".jar";
      URL url = new URL("jar:" + packURL + "!/packs/pack" + n);
      //JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
      // TODO: what happens when using an automated installer?
      in = new WebAccessor(null).openInputStream(url);
      // TODO: Fails miserably when pack jars are not found, so this is temporary
      if (in == null)
        throw new FileNotFoundException(url.toString());
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

  // CUSTOM ACTION STUFF -------------- start -----------------

  /**
   * Informs all listeners which would be informed at the given
   * action type.
   * @param customActions array of lists with the custom action objects
   * @param action identifier for which callback should be called
   * @param firstParam first parameter for the call
   * @param secondParam second parameter for the call
   * @param thirdParam third parameter for the call
   */
  private void informListeners(List[] customActions, int action, 
    Object firstParam, Object secondParam, Object thirdParam)
    throws Exception
  {
    List listener = null;
    // select the right action list.
    switch( action )
    {
      case InstallerListener.BEFORE_FILE:
      case InstallerListener.AFTER_FILE:
      case InstallerListener.BEFORE_DIR:
      case InstallerListener.AFTER_DIR:
        listener = customActions[customActions.length - 1];  
        break;
      default:
        listener = customActions[0];   
        break;
    }
    if( listener == null )
      return;
    // Iterate the action list.
    Iterator iter = listener.iterator();
    while( iter.hasNext())
    {
      InstallerListener il = (InstallerListener) iter.next();
      switch( action )
      {
        case InstallerListener.BEFORE_FILE:
          il.beforeFile(  (File) firstParam, (PackFile) secondParam );
          break;
        case InstallerListener.AFTER_FILE:
          il.afterFile(  (File) firstParam, (PackFile) secondParam );
          break;
        case InstallerListener.BEFORE_DIR:
          il.beforeDir( (File) firstParam, (PackFile) secondParam );
          break;
        case InstallerListener.AFTER_DIR:
          il.afterDir( (File) firstParam, (PackFile) secondParam );
          break;
        case InstallerListener.BEFORE_PACK:
          il.beforePack((Pack) firstParam, (Integer) secondParam, 
            (AbstractUIProgressHandler) thirdParam);
          break;
        case InstallerListener.AFTER_PACK:
          il.afterPack((Pack) firstParam, (Integer) secondParam, 
            (AbstractUIProgressHandler) thirdParam);
          break;
        case InstallerListener.BEFORE_PACKS:
          il.beforePacks((AutomatedInstallData) firstParam, 
            (Integer) secondParam, (AbstractUIProgressHandler) thirdParam);
          break;
        case InstallerListener.AFTER_PACKS:
          il.afterPacks((AutomatedInstallData) firstParam,
            (AbstractUIProgressHandler) secondParam );
          break;
       
      }
    }
  }

  /**
   * Returns the defined custom actions split into types including
   * a constructed type for the file related installer listeners. 
   * @return array of lists of custom action data like listeners
   */
  private List [] getCustomActions()
  {
    String [] listenerNames = AutomatedInstallData.CUSTOM_ACTION_TYPES;
    List [] retval = new List[listenerNames.length + 1];
    int i;
    for( i = 0; i < listenerNames.length; ++i)
    {
      retval[i] = (List) idata.customActionData.get(listenerNames[i]);
      if( retval[i] == null)
        //    Make a dummy list, then iterator is ever callable.
        retval[i] = new ArrayList(); 
    }
    if( retval[AutomatedInstallData.INSTALLER_LISTENER_INDEX].size() > 0 )
    { // Installer listeners exist
      // Create file related installer listener list in the last
      //element of custom action array.
      i = retval.length - 1; // Should be so, but safe is safe ...
      retval[i] = new ArrayList(); 
      Iterator iter = ((List)retval[
        AutomatedInstallData.INSTALLER_LISTENER_INDEX]).iterator();
      while( iter.hasNext())
      {
        // If we get a class cast exception many is wrong and
        // we must fix it.
        InstallerListener li = (InstallerListener) iter.next();
        if( li.isFileListener())
          retval[i].add(li);
      }
       
    }
    return(retval);
  }

  /**
   * Adds additional unistall data to the uninstall data object.
   * @param udata unistall data 
   * @param customActions array of lists of custom action data like uninstaller listeners
   */
  private void handleAdditionalUninstallData(UninstallData udata, List[] customActions)
  {
    // Handle uninstall libs
    udata.addAdditionalData("__uninstallLibs__",
      customActions[AutomatedInstallData.UNINSTALLER_LIBS_INDEX]);
    // Handle uninstaller listeners
    udata.addAdditionalData("uninstallerListeners",
      customActions[AutomatedInstallData.UNINSTALLER_LISTENER_INDEX]);
  }

  
  // This method is only used if a file related custom action exist.
  /**
   * Creates the given directory recursive and calls the
   * method "afterDir" of each listener with the current file
   * object and the pack file object.  On error an exception
   * is raised.
   * @param dest the directory which should be created
   * @param pf current pack file object
   * @param customActions all defined custom actions
   * @return false on error, true else
   * @throws Exception
   */

  private boolean mkDirsWithEnhancement(File dest, PackFile pf, 
    List [] customActions  ) throws Exception
  {
    String path = "unknown";
    if( dest != null )
      path = dest.getAbsolutePath();
    if( dest != null && !dest.exists() && dest.getParentFile() != null)
    {
      if( dest.getParentFile().exists() )
        informListeners(customActions, InstallerListener.BEFORE_DIR,
          dest, pf, null);
      if( ! dest.mkdir() )
      {
        mkDirsWithEnhancement( dest.getParentFile(), pf, customActions );
        if( ! dest.mkdir() )
          dest = null;
      }
      informListeners(customActions, InstallerListener.AFTER_DIR,
        dest, pf, null);
    }
    if (dest == null)
    {
      handler.emitError(
      "Error creating directories",
      "Could not create directory\n" + path);
      handler.stopAction();
      return(false);
    }
    return(true);
  }
  // CUSTOM ACTION STUFF -------------- end -----------------

}
