/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge, Tino Schwarze
 *
 *  File :               CompilePanel.java
 *  Description :        A panel to compile files after installation
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.OsConstraint;

/**
 * This class does alle the work for the process panel.
 *
 * It responsible for
 * <ul>
 * <li>parsing the process spec XML file
 * <li>performing the actions described therein
 * </ul>
 *
 * @author    Tino Schwarze
 * @created    August 2003
 */
public class ProcessPanelWorker implements Runnable
{
  /**  Name of resource for specifying processing parameters. */
  private static final String SPEC_RESOURCE_NAME  = "ProcessPanel.Spec.xml";

  private VariableSubstitutor vs;

  private XMLElement spec;

  private AutomatedInstallData idata;

  protected AbstractUIProcessHandler handler;

  private ArrayList jobs = new ArrayList ();

  private Thread processingThread = null;
    
  /**
   *  The constructor.
   *
   * @param  idata    The installation data.
   * @param  handler The handler to notify of progress.
   */
  public ProcessPanelWorker(AutomatedInstallData idata, AbstractUIProcessHandler handler) 
    throws IOException
  {
    this.idata = idata;
    this.handler = handler;
    this.vs = new VariableSubstitutor(idata.getVariableValueMap());

    if (! readSpec ())
      throw new IOException ("Error reading processing specification");
  }

  private boolean readSpec ()
  {
		InputStream input;
		try
		{
			input = ResourceManager.getInstance().getInputStream (SPEC_RESOURCE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
      return false;
		}

    StdXMLParser parser = new StdXMLParser ();
    parser.setBuilder (new StdXMLBuilder ());
    parser.setValidator (new NonValidator ());
    
		try
		{
			parser.setReader (new StdXMLReader (input));
			
			this.spec = (XMLElement) parser.parse();
		}
		catch (Exception e)
		{
      System.err.println ("Error parsing XML specification for processing.");
      System.err.println (e.toString ());
      return false;
		}

    if (! this.spec.hasChildren ())
      return false;

    for (Iterator job_it = this.spec.getChildrenNamed("job").iterator(); job_it.hasNext(); )
    {
      XMLElement job_el = (XMLElement)job_it.next ();
      
      // first check OS constraints - skip jobs not suited for this OS
      List constraints = OsConstraint.getOsList (job_el);

      if (OsConstraint.oneMatchesCurrentSystem(constraints))
      {
        List ef_list = new ArrayList ();
              
        String job_name = job_el.getAttribute("name", "");
        
        for (Iterator ef_it = job_el.getChildrenNamed("executefile").iterator (); ef_it.hasNext(); )
        {
          XMLElement ef = (XMLElement)ef_it.next();
          
          String ef_name = ef.getAttribute("name");
          
          if ((ef_name == null) || (ef_name.length() == 0))
          {
            System.err.println ("missing \"name\" attribute for <executefile>");
            return false;
          }
          
          List args = new ArrayList ();
          
          for (Iterator arg_it = ef.getChildrenNamed("arg").iterator(); arg_it.hasNext();)
          {
            XMLElement arg_el = (XMLElement)arg_it.next();
            
            String arg_val = arg_el.getContent();
            
            args.add (arg_val);
          }
          
          ef_list.add (new ExecutableFile (ef_name, args));
        }
        
        this.jobs.add (new ProcessingJob (job_name, ef_list));
      }
      
    }
    
    return true;
  }

  /** This is called when the processing thread is activated. 
   *
   * Can also be called directly if asynchronous processing is not
   * desired.
   */
  public void run ()
  {
    this.handler.startProcessing (this.jobs.size());
    
    for (Iterator job_it = this.jobs.iterator(); job_it.hasNext(); )
    {
      ProcessingJob pj = (ProcessingJob)job_it.next();
      
      this.handler.startProcess (pj.name);
      
      boolean result = pj.run (this.handler, this.vs);
      
      this.handler.finishProcess ();
      
      if (! result)
        break;
    }
    
    this.handler.finishProcessing();
  }

  /** Start the compilation in a separate thread. */
  public void startThread ()
  {
    this.processingThread = new Thread (this, "processing thread");
    //will call this.run()
    this.processingThread.start();
  }

  interface Processable
  {
    /**
     * @param handler The UI handler for user interaction and to send output to.
     * @return true on success, false if processing should stop
     */
    public boolean run (AbstractUIProcessHandler handler, VariableSubstitutor vs);
  }
  
  class ProcessingJob implements Processable
  {
    public String name;
    private List processables;
    
    public ProcessingJob (String name, List processables)
    {
      this.name = name;
      this.processables = processables;
    }
    
    public boolean run (AbstractUIProcessHandler handler, VariableSubstitutor vs)
    {
      for (Iterator pr_it = this.processables.iterator(); pr_it.hasNext(); )
      {
        Processable pr = (Processable)pr_it.next();
        
        if (! pr.run (handler, vs)) return false;
      }
      
      return true;
    }
    
  }
 
  class ExecutableFile implements Processable
  {
    private String filename;
    private List arguments;
    protected AbstractUIProcessHandler handler;
    
    public ExecutableFile (String fn, List args)
    {
      this.filename = fn;
      this.arguments = args;        
    }
    
    public boolean run (AbstractUIProcessHandler handler, VariableSubstitutor vs)
    {
      this.handler = handler;
    
      String params[] = new String[this.arguments.size()+1];
        
      params[0] = vs.substitute(this.filename, "plain");
          
      int i = 1;
      for (Iterator arg_it = this.arguments.iterator(); arg_it.hasNext(); )
      {
        params[i++] = vs.substitute ((String)arg_it.next(), "plain");
      }
      
      try
      {
        Process p = Runtime.getRuntime().exec (params);
        
        OutputMonitor stdoutMon = new OutputMonitor (this.handler, p.getInputStream(), false);
        OutputMonitor stderrMon = new OutputMonitor (this.handler, p.getErrorStream(), true);
        Thread stdoutThread = new Thread (stdoutMon);
        Thread stderrThread = new Thread (stderrMon);
        stdoutThread.setDaemon(true);
        stderrThread.setDaemon(true);
        stdoutThread.start();
        stderrThread.start();
        
        try
        {
          int exitStatus = p.waitFor ();
          
          stopMonitor(stdoutMon, stdoutThread);
          stopMonitor(stderrMon, stderrThread);
          
          if (exitStatus != 0)
          {
            if (this.handler.askQuestion("process execution failed", "Continue anyway?", 
                  AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES) 
                  == AbstractUIHandler.ANSWER_NO)
            {
              return false;
            }
          }
        }
        catch (InterruptedException ie)
        {
          p.destroy ();
          this.handler.emitError ("process interrupted", ie.toString ());
          return false;
        }
      }
      catch (IOException ioe)
      {
        this.handler.emitError("I/O error", ioe.toString());
        return false;
      }
      
      return true;
    }

    private void stopMonitor (OutputMonitor m, Thread t)
    {
      // taken from com.izforge.izpack.util.FileExecutor
      m.doStop();
      long softTimeout = 500;
      try
      {
        t.join(softTimeout);
      }
      catch (InterruptedException e)
      {}

      if (t.isAlive() == false)
        return;

      t.interrupt();
      long hardTimeout = 500;
      try
      {
        t.join(hardTimeout);
      }
      catch (InterruptedException e)
      {}
    }
    
    public class OutputMonitor implements Runnable
    {
      private boolean stderr = false;
      private AbstractUIProcessHandler handler;
      private BufferedReader reader;
      private Boolean stop = new Boolean (false);
      
      public OutputMonitor (AbstractUIProcessHandler handler, InputStream is, boolean stderr)
      {
        this.stderr = stderr;
        this.reader = new BufferedReader (new InputStreamReader (is));
        this.handler = handler;
      }
      
      public void run ()
      {
        try
        {
          String line;
          while ((line = reader.readLine()) != null)
          {
            this.handler.logOutput(line, stderr);
            
            synchronized (this.stop)
            {
              if (stop.booleanValue())
                return;
            }
          }
        }
        catch (IOException ioe)
        {
          this.handler.logOutput (ioe.toString (), true);
        }
        
      }
      
      public void doStop ()
      {
        synchronized (this.stop)
        {
          this.stop = new Boolean (true);
        }
      }
     
    }
    
  }
  
}

