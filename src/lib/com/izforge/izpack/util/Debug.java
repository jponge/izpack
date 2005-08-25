/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
//import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import com.izforge.izpack.installer.Installer;


/**
 * This class is for debug purposes.
 * It is highly recommended to use it on critical or experimental
 * code places.
 * To enable the debug mode of IzPack, just start the installer 
 * with the java parameter -DTRACE=true or -DSTACKTRACE=true to enable extendend
 * output of the internal status of critical objects.
 * <br>
 * How to use it as IzPack Setup Developer:
 * <br>
 * Just import this class and use one of the methods:
 * <dl>
 *   <dt>Debug.trace( aCriticalObject )</dt>
 *       <dd> - to print the status on console</dd>
 *   <dt>Debug.error( aCriticalObject )</dt>
 *       <dd> - to print the status on console and<br> 
 * print the stacktrace of a supressed Exception.</dd>
 * 
 * <dt>Additionally:</dt>
 *     <dd> if -DLOG is given the output will be written in the File @see #LOGFILENAME in the users Home directory.</dd> 
 *  
 * </dl>

 * @author Julien Ponge, Klaus Bartz, Marc Eppelmann
 * @version $Revision$ ($Id$)
 */
public class Debug
{
    /** Parameter for public javacall "java -jar izpack.jar -DLOG" (Class.internal.variable: (DLOG = "LOG")) */
    public static final String DLOG  = "LOG";

    /** Parameter for public javacall "java -jar izpack.jar -DSTACKTRACE" (Class.internal.variable: (DSTACKTRACE = "STACKTRACE")) */
    public static final String DSTACKTRACE = "STACKTRACE";
    
    /** Parameter for public javacall "java -jar izpack.jar -DTRACE" (Class.internal.variable: (DTRACE = "TRACE")) */
    public static final String DTRACE = "TRACE";
    
    /** System.Property Key: IZPACK_LOGFILE = "izpack.logfile" */
    public static final String IZPACK_LOGFILE = "izpack.logfile";

    /** internally initial unintialized TRACE-flag */
    private static boolean TRACE;

    /** internal initial unintialized STACKTRACE-flag */
    private static boolean STACKTRACE;

    /** internal initial unintialized LOG-flag */
    private static boolean LOG;

    /** LOGFILE_PREFIX = "IzPack_Logfile_at_" */
    public static String LOGFILE_PREFIX = "IzPack_Logfile_at_";
    
    /** LOGFILE_EXTENSION = ".txt" */    
    public static String LOGFILE_EXTENSION = ".txt";
    
    /** LOGFILENAME = LOGFILE_PREFIX + System.currentTimeMillis() + LOGFILE_EXTENSION */
    public static String LOGFILENAME = LOGFILE_PREFIX + System.currentTimeMillis() + LOGFILE_EXTENSION;

    /** internal used File writer */
    private static BufferedWriter fw;
    
    /** internal used Printfile writer */
    private static PrintWriter logfile;
    
    
    
    static
    {
        boolean st = false;
        try
        {
          st = Boolean.getBoolean(DSTACKTRACE);
        }
        catch (Exception ex)
        {
          // ignore
        }
        STACKTRACE = st;
        
        boolean log = false;
        try
        {
          log = Boolean.getBoolean( DLOG );
        }
        catch (Exception ex)
        {
          // ignore
        }
        LOG = log;
        

        boolean t = false;
        try
        {
          if (STACKTRACE)
          {
             t = true;
          }
          else
          {
            t = Boolean.getBoolean( DTRACE );
          }
        }
        catch (Exception ex)
        {
          // ignore
        }
        TRACE = t;
        if(LOG)
            System.out.println(DLOG+ " enabled.");
        if(TRACE)
            System.out.println(DTRACE+ " enabled.");
        if(STACKTRACE)
            System.out.println(DSTACKTRACE+ " enabled.");
        
        
        logfile = createLogFile();
        
        Debug.log( Installer.class.getName() +  " LogFile created at " + new Date(System.currentTimeMillis() ) );
        
        Properties sysProps = System.getProperties();
        
        Enumeration spe = sysProps.keys();
        while( spe.hasMoreElements() )
        {
          String aKey = (String) spe.nextElement();
          
          Debug.log( aKey + "  =  " + sysProps.getProperty( aKey ));
        }
        
    }

    /**
     * Traces the internal status of the given Object
     * @param s
     */
    public static void trace(Object s)
    {
        if (TRACE)
        {
            // console.println(s.toString());
            System.out.println(s);
            if (STACKTRACE && (s instanceof Throwable))
            {
                // StringWriter sw = new StringWriter();
                // PrintWriter pw = new PrintWriter(sw);
                // ((Throwable)s).printStackTrace(pw);
                // console.println(sw.toString());
                ((Throwable) s).printStackTrace();
            }
            System.out.flush();
        }
    }

    /**
     * Traces the given object and additional write their status in the LOGFILE.
     * @param s
     */
    public static void error(Object s)
    {
        trace(s);
        System.err.println(s);
        System.err.flush();
        log(s);        
    }
    
    public static void log( Object o )
    {
      // PrintWriter logfile = getLogFile();
       if( (logfile = getLogFile()) == null ) 
           logfile = createLogFile();
        //if (LOG) 
        if (LOG && logfile!=null )
        {
            if(o==null)
                o = "null";
            logfile.println(o);
            if (o instanceof Throwable)
            {
                ((Throwable) o).printStackTrace(logfile);
            }
            logfile.flush();
            //logfile.close();
            //logFile = null;
        }
        else
        {
            System.err.println("Cannot write into logfile: ("+ logfile +") <- '" + o + "'");
        }
    }

    private static PrintWriter createLogFile()
    {
        //PrintWriter result = null;
       
            String tempDir = System.getProperty("java.io.tmpdir");
            
            File tempDirFile = new File(tempDir);
            try
            {
                tempDirFile.mkdirs();
            }
            catch (RuntimeException e1)
            {
                e1.printStackTrace();
            }
            String logfilename = LOGFILENAME;
            System.out.println("creating Logfile: '" + logfilename + "' in: '" + tempDir + "'" ) ;
            
            File out = new File( tempDir, logfilename );
        
            if (tempDirFile.canWrite())
            {                
                try
                {
                    fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( out ), "UTF-8" ));
                    //fw = new FileWriter(out, true );
                    logfile = setLogFile( new PrintWriter(fw) );
                }
                catch (Exception e)
                {                    
                    logfile = null;
                    e.printStackTrace();
                    
                }
                
//                }
//                catch (Throwable e)
//                {
//                    System.err.println(e);
//                    System.err.flush();
//                }
                
            }
            else
            {
                logfile = null;
                System.err.println("Fatal: cannot write File: '" + logfilename + "' into: " + tempDirFile);           
            }
        return logfile;
    }

    /**
     * Indicates if debug is tracing
     * @return true if tracing otherwise false
     */
    public static boolean tracing()
    {
        return TRACE;
    }

    /**
     * Indicates if debug is stacktracing
     * @return true if stacktracing otherwise false
     */
    public static boolean stackTracing()
    {
        return STACKTRACE;
    }

    
    /**
     * Returns the LOG flag.
     * @return Returns the LOG flag.
     */
    public static boolean isLOG()
    {
        return LOG;
    }

    
    /**
     * Sets The LOG like the given value
     * @param aFlag The LOG status to set to or not.
     */
    public static void setLOG(boolean aFlag)
    {
        System.out.println( DLOG + " = " + aFlag );
        LOG = aFlag;
    }

    
    /**
     * Returns the current STACKTRACE flag
     * @return Returns the STACKTRACE.
     */
    public static boolean isSTACKTRACE()
    {
        return STACKTRACE;
    }

    
    /**
     * Sets the STACKTRACE like the given value 
     * @param aFlag The STACKTRACE to set / unset.
     */
    public static void setSTACKTRACE(boolean aFlag)
    {
        System.out.println( DSTACKTRACE + " = " + aFlag );
        STACKTRACE = aFlag;
    }

    
    /**
     * Gets the current TRACE flag
     * @return Returns the TRACE.
     */
    public static boolean isTRACE()
    {
        return TRACE;
    }

    
    /**
     * Sets the TRACE flag like the given value
     * @param aFlag The TRACE to set / unset.
     */
    public static void setTRACE(boolean aFlag)
    {
        System.out.println( DTRACE + " = " + aFlag );
        TRACE = aFlag;
    }

    
    /**
     * @return Returns the logFile.
     */
    public static PrintWriter getLogFile()
    {
        logfile = (PrintWriter) System.getProperties().get(IZPACK_LOGFILE);
        //System.out.println("Get::logfile == " + logfile);
        return logfile;
    }

    
    /**
     * @param logFile The logFile to set.
     */
    public static synchronized PrintWriter setLogFile( PrintWriter aLogFile )
    {
        System.getProperties().put(IZPACK_LOGFILE, aLogFile);
         
        logfile = (PrintWriter) System.getProperties().get(IZPACK_LOGFILE);
        
        if( logfile ==null)
          System.err.println( "Set::logfile == null");
        return logfile;
    }
}
