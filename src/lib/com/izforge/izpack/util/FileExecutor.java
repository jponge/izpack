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
import java.util.HashMap;
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
   *  Executed a system command and waits for completion.
   *
   * @param  params  system command as string array
   * @param  output  Description of the Parameter
   * @return         Description of the Return Value
   */
  public int executeCommand(String[] params, String[] output)
  {
    Process process = null;
    MonitorInputStream outMonitor = null;
    MonitorInputStream errMonitor = null;
    Thread t1 = null;
    Thread t2 = null;
    int exitStatus = 0;

    try
    {
      // execute command
      process = Runtime.getRuntime().exec(params);

      InputStreamReader or = new InputStreamReader(process.getInputStream());
      InputStreamReader er = new InputStreamReader(process.getErrorStream());
      StringWriter outWriter = new StringWriter();
      StringWriter errWriter = new StringWriter();
      outMonitor = new MonitorInputStream(or, outWriter);
      errMonitor = new MonitorInputStream(er, errWriter);
      t1 = new Thread(outMonitor);
      t2 = new Thread(errMonitor);
      t1.setDaemon(true);
      t2.setDaemon(true);
      t1.start();
      t2.start();

      // wait for command to comlete
      exitStatus = process.waitFor();
      if (t1 != null)
        t1.join();

      if (t2 != null)
        t2.join();

      // save command output
      output[0] = outWriter.toString();
      output[1] = errWriter.toString();
      exitStatus = process.exitValue();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace(System.err);
      stopThread(t1, outMonitor);
      stopThread(t2, errMonitor);
      process.destroy();
    }
    catch (IOException e)
    {
      e.printStackTrace(System.err);
    }
    return exitStatus;
  }


  /**
   *  Executes files specified at construction time.
   *
   * @return    Description of the Return Value
   */
  public int executeFiles()
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
      File file = new File(efile.path);

      // fix executable permission for unix systems
      if (pathSep.equals(":") && (!osName.startsWith("mac") ||
        osName.endsWith("x")))
      {
        String[] params = {"/bin/chmod", permissions, file.toString()};
        exitStatus = executeCommand(params, output);
      }
      // loop through all operating systems
      Iterator osIterator = efile.osList.iterator();
      while (osIterator.hasNext())
      {
        Os os = (Os) osIterator.next();

        if (os.matchCurrentSystem())
        {
          // execute command in POSTINSTALL stage
          if ((exitStatus == 0) && (efile.executionStage == ExecutableFile.POSTINSTALL))
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
              String message = output[0] + "\n" + output[1];
              if (message.length() == 1)
                message = new String("Failed to execute " + file.toString() + ".");

              if (efile.onFailure == ExecutableFile.ABORT)

                javax.swing.JOptionPane.showMessageDialog(null, message,
                  "Installation error",
                  javax.swing.JOptionPane.ERROR_MESSAGE);
              else if (efile.onFailure == ExecutableFile.WARN)
              {

                javax.swing.JOptionPane.showMessageDialog(null, message,
                  "Installation warning",
                  javax.swing.JOptionPane.WARNING_MESSAGE);
                exitStatus = 0;
              }
              else
                if (
                  javax.swing.JOptionPane.showConfirmDialog(null,
                  message + "Would you like to proceed?",
                  "Installation Warning",
                  javax.swing.JOptionPane.YES_NO_OPTION) ==
                  javax.swing.JOptionPane.YES_OPTION)
                  exitStatus = 0;

            }
          }
        }
        else
        {
          //@todo
        }
      }
      // POSTINSTALL executables will be deleted
      if (efile.executionStage == ExecutableFile.POSTINSTALL)
        file.delete();

    }
    return exitStatus;
  }

  /** The files to execute. */
  private Collection files;
}

