/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2002 Olexij Tkatchenko
 *
 *  File :               FileExecutor.java
 *  Description :        File execution class.
 *  Author's email :     ot@parcs.de
 *  Website :            http://www.izforge.com
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
package com.izforge.izpack.util;

import com.izforge.izpack.ExecutableFile;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *  Executes a bunch of files. This class is intended to do a system dependent
 *  installation postprocessing. Executable file can be any file installed with
 *  current package. After execution the file can be optionally removed. Before
 *  execution on Unix systems execution flag will be set on processed file.
 *
 * @author     Olexij Tkatchenko <ot@parcs.de>
 * @created    November 1, 2002
 */
public class FileExecutor
{
  /**
   *  This is a grabber for stdout and stderr. It will be launched once at
   *  command execution end terminates if the apropriate stream runs out of
   *  data.
   *
   * @author     julien
   * @created    November 1, 2002
   */
  private class MonitorInputStream implements Runnable
  {

    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean shouldStop = false;


    public MonitorInputStream(Reader in, Writer out)
    {
      reader = new BufferedReader(in);
      writer = new BufferedWriter(out);
    }


    public void doStop()
    {
      shouldStop = true;
    }


    public void run()
    {
      try
      {
        String line;
        while ((line = reader.readLine()) != null)
        {
          writer.write(line);
          writer.newLine();
          writer.flush();
          if (shouldStop)
            return;
        }
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace(System.out);
      }
    }
  }


  private boolean stopThread(Thread t, MonitorInputStream m)
  {
    m.doStop();
    long softTimeout = 1000;
    try
    {
      t.join(softTimeout);
    }
    catch (InterruptedException e)
    {}

    if (t.isAlive() == false)
      return true;

    t.interrupt();
    long hardTimeout = 1000;
    try
    {
      t.join(hardTimeout);
    }
    catch (InterruptedException e)
    {}
    return !t.isAlive();
  }


  /**
   *  Constructs a new executor. The executable files specified must have
   *  pretranslated paths (variables expanded and file separator characters
   *  converted if necessary).
   *
   * @param  files  the executable files to process
   */
  public FileExecutor(Collection files)
  {
    this.files = files;
  }


  /**
   *  Constructs a new executor.
   */
  public FileExecutor ()
  {
    this.files = null;
  }

  /**
   *  Executed a system command and waits for completion.
   *
   * @param  params  system command as string array
   * @param  output  contains output of the command 
   *                 index 0 = standard output
   *                 index 1 = standard error
   * @return         exit status of process
   */
  public int executeCommand(String[] params, String[] output)
  {
    StringBuffer retval = new StringBuffer();
    retval.append("executeCommand\n");
    if (params != null)
    {
      for (int i = 0; i < params.length; i++)
      {
        retval.append("\tparams: "+params[i]);
        retval.append("\n");
      }
    }
    Process process = null;
    MonitorInputStream outMonitor = null;
    MonitorInputStream errMonitor = null;
    Thread t1 = null;
    Thread t2 = null;
    int exitStatus = -1;

    Debug.trace(retval);

    try
    {
      // execute command
      process = Runtime.getRuntime().exec(params);

      boolean console = false;//TODO: impl from xml <execute in_console=true ...>, but works already if this flag is true
      if (console)
      {
        Console c = new Console(process);
        // save command output
        output[0] = c.getOutputData();
        output[1] = c.getErrorData();
        exitStatus = process.exitValue();
      }
      else
      {
        StringWriter outWriter = new StringWriter();
        StringWriter errWriter = new StringWriter();

        InputStreamReader or =
          new InputStreamReader(process.getInputStream());
        InputStreamReader er =
          new InputStreamReader(process.getErrorStream());
        outMonitor = new MonitorInputStream(or, outWriter);
        errMonitor = new MonitorInputStream(er, errWriter);
        t1 = new Thread(outMonitor);
        t2 = new Thread(errMonitor);
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        // wait for command to complete
        exitStatus = process.waitFor();
        if (t1 != null)
        {
          t1.join();
        }
        if (t2 != null)
        {
          t2.join();
        }

        // save command output
        output[0] = outWriter.toString();
        Debug.trace ("stdout:");
        Debug.trace (output[0]);
        output[1] = errWriter.toString();
        Debug.trace ("stderr:");
        Debug.trace (output[1]);
      }
      Debug.trace ("exit status: " + Integer.toString (exitStatus));
    }
    catch (InterruptedException e)
    {
      if (Debug.tracing()) e.printStackTrace(System.err);
      stopThread(t1, outMonitor);
      stopThread(t2, errMonitor);
      output[0] = "";
      output[1] = e.getMessage() + "\n";
      process.destroy();
    }
    catch (IOException e)
    {
      if (Debug.tracing()) e.printStackTrace(System.err);
      output[0] = "";
      output[1] = e.getMessage() + "\n";
    }
    return exitStatus;
  }

  /**
   *  Executes files specified at construction time.
   *
   * @param   currentStage the stage of the installation
   * @param   handler The AbstractUIHandler to notify on errors.
   * 
   * @return  0 on success, else the exit status of the last failed command
   */
  public int executeFiles(int currentStage, AbstractUIHandler handler)
  {
    int exitStatus = 0;
    String[] output = new String[2];
    String pathSep = System.getProperty("path.separator");
    String osName = System.getProperty("os.name").toLowerCase();
    String permissions = (System.getProperty("user.name").equals("root")) ? "a+x" : "u+x";

    // loop through all executables
    Iterator efileIterator = files.iterator();
    while ((exitStatus == 0) && efileIterator.hasNext())
    {
      ExecutableFile efile = (ExecutableFile) efileIterator.next();
      boolean deleteAfterwards = ! efile.keepFile;
      File file = new File(efile.path);
      Debug.trace("handeling executable file "+efile);

      // skip file if not for current OS (it might not have been installed at all)
      if (! OsConstraint.oneMatchesCurrentSystem (efile.osList))
        continue;
      
      if(currentStage!=ExecutableFile.UNINSTALL)
      {
        // fix executable permission for unix systems
        if (pathSep.equals(":") && (!osName.startsWith("mac") ||
              osName.endsWith("x")))
        {
          Debug.trace("making file executable (setting executable flag)");
          String[] params = {"/bin/chmod", permissions, file.toString()};
          exitStatus = executeCommand(params, output);
        }
      }

      // execute command in POSTINSTALL stage
      if ((exitStatus == 0) &&
          ((currentStage == ExecutableFile.POSTINSTALL && efile.executionStage == ExecutableFile.POSTINSTALL)
           || (currentStage==ExecutableFile.UNINSTALL && efile.executionStage == ExecutableFile.UNINSTALL)))
      {
        List paramList = new ArrayList();
        if (ExecutableFile.BIN == efile.type)
          paramList.add(file.toString());

        else if (ExecutableFile.JAR == efile.type && null == efile.mainClass)
        {
          paramList.add(System.getProperty("java.home") + "/bin/java");
          paramList.add("-jar");
          paramList.add(file.toString());
        }
        else if (ExecutableFile.JAR == efile.type && null != efile.mainClass)
        {
          paramList.add(System.getProperty("java.home") + "/bin/java");
          paramList.add("-cp=" + file.toString());
          paramList.add(efile.mainClass);
        }

        if (null != efile.argList && !efile.argList.isEmpty())
          paramList.addAll(efile.argList);

        String[] params = new String[paramList.size()];
        for (int i = 0; i < paramList.size(); i++)
          params[i] = (String) paramList.get(i);

        exitStatus = executeCommand(params, output);
          
        // bring a dialog depending on return code and failure handling
        if (exitStatus != 0)
        {
          deleteAfterwards = false;
          String message = output[0] + "\n" + output[1];
          if (message.length() == 1)
            message = new String("Failed to execute " + file.toString() + ".");

          if (efile.onFailure == ExecutableFile.ABORT)
          {
            // CHECKME: let the user decide or abort anyway?
            handler.emitError("file execution error", message);
          }
          else if (efile.onFailure == ExecutableFile.WARN)
          {
            // CHECKME: let the user decide or abort anyway?
            handler.emitWarning ("file execution error", message);
            exitStatus = 0;
          }
          else
          {
            if (handler.askQuestion (null, "Continue?", AbstractUIHandler.CHOICES_YES_NO) 
                == AbstractUIHandler.ANSWER_YES)
              exitStatus = 0;
          }

        }

      }

      

      // POSTINSTALL executables will be deleted
      if (efile.executionStage == ExecutableFile.POSTINSTALL && deleteAfterwards)
      {
        if (file.canWrite()) file.delete();
      }

    }
    return exitStatus;
  }

  /** The files to execute. */
  private Collection files;
}

