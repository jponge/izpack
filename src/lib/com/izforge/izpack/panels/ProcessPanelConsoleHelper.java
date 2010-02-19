/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import java.io.PrintWriter;
import java.util.Properties;
import java.io.IOException;

import com.izforge.izpack.installer.*;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.AbstractUIProcessHandler;


/**
 * Process Panel console helper
 *
 * @author Dustin Hawkins
 * @author Mounir el hajj
 */
public class ProcessPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole, 
	AbstractUIProcessHandler 
{
	
    private int noOfJobs = 0;

    private int currentJob = 0;
    
	 public boolean runConsoleFromProperties(AutomatedInstallData installData, Properties p){
		boolean retVal = false;

		retVal = this.runConsole(installData);

		return (retVal);
	 }


	public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter) {
		return true;
	}
	
	public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p){
		boolean retVal = false;

		retVal = this.runConsole(installData);

		return (retVal);
		
	}

	public boolean runConsole(AutomatedInstallData idata) {
        try
        {
            
            ProcessPanelWorker worker = new ProcessPanelWorker(idata, this);

            worker.run();

            if (!worker.getResult())
            {
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

		  return (true);
	}
	  public void logOutput(String message, boolean stderr)
	    {
	        if (stderr)
	        {
	            System.err.println(message);
	        }
	        else
	        {
	            System.out.println(message);
	        }
	    }

	    /**
	     * Reports progress on System.out
	     *
	     * @see com.izforge.izpack.util.AbstractUIProcessHandler#startProcessing(int)
	     */
	    public void startProcessing(int noOfJobs)
	    {
	        System.out.println("[ Starting processing ]");
	        this.noOfJobs = noOfJobs;
	    }

	    /**
	     * @see com.izforge.izpack.util.AbstractUIProcessHandler#finishProcessing
	     */
	    public void finishProcessing(boolean unlockPrev, boolean unlockNext)
	    {
	        /* FIXME: maybe we should abort if unlockNext is false...? */
	        System.out.println("[ Processing finished ]");
	    }

	    /**
	     *
	     */
	    public void startProcess(String name)
	    {
	        this.currentJob++;
	        System.out.println("Starting process " + name + " (" + Integer.toString(this.currentJob)
	                + "/" + Integer.toString(this.noOfJobs) + ")");
	    }

	    public void finishProcess()
	    {
	    }

    public void emitNotification(String message)
    {
        System.out.println(message);
    }

    public boolean emitWarning(String title, String message)
    {
        System.err.println("[ WARNING: " + message + " ]");

        return true;
    }

    public void emitError(String title, String message)
    {
        System.err.println("[ ERROR: " + message + " ]");
    }
    
    public void emitErrorAndBlockNext(String title, String message)
    {
        System.err.println("[ ERROR: " + message + " ]");
    }

    public int askQuestion(String title, String question, int choices)
    {
        // don't know what to answer
        return AbstractUIHandler.ANSWER_CANCEL;
    }

    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        return default_choice;
    }   

    public void startAction(String name, int no_of_steps)
    {
        System.out.println("[ Starting to process ]");
        this.noOfJobs = no_of_steps;
    }

    public void stopAction()
    {
        System.out.println("[ Processing finished ]");
        boolean done = true;
    }

    public void progress(int val, String msg)
    {

    }

    public void nextStep(String packName, int stepno, int stepsize)
    {
        System.out.print("[ Processing job: " + packName + " (");
        System.out.print(stepno);
        System.out.print('/');
        System.out.print(this.noOfJobs);
        System.out.println(") ]");
    }

    public void setSubStepNo(int no_of_substeps)
    {

    }
}
