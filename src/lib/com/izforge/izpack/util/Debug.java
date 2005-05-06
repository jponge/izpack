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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Debug
{

    private static final boolean TRACE;

    private static final boolean STACKTRACE;

    public static boolean LOG;

    static
    {
        boolean st = false;
        try
        {
            st = Boolean.getBoolean("STACKTRACE");
        }
        catch (Exception ex)
        {
            // ignore
        }
        STACKTRACE = st;

        boolean t = false;
        try
        {
            if (STACKTRACE)
            {
                t = true;
            }
            else
            {
                t = Boolean.getBoolean("TRACE");
            }
        }
        catch (Exception ex)
        {
            // ignore
        }
        TRACE = t;
    }

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

    public static void error(Object s)
    {
        trace(s);
        System.err.println(s);
        System.err.flush();

        if (LOG && logFile == null) createLogFile();
        if (LOG && logFile != null && s != null)
        {
            logFile.print(s);
            if (s instanceof Throwable)
            {
                ((Throwable) s).printStackTrace(logFile);
            }
            logFile.flush();
        }
    }

    private static PrintWriter logFile;

    private static void createLogFile()
    {
        try
        {
            File out = new File(System.getProperty("user.dir"), ".log.txt");
            if (out.canWrite())
            {
                if (out.exists()) out.delete();
                FileWriter fw = new FileWriter(out);
                logFile = new PrintWriter(fw);
            }
        }
        catch (Throwable e)
        {
            System.err.println(e);
            System.err.flush();
        }
    }

    public static boolean tracing()
    {
        return TRACE;
    }

    public static boolean stackTracing()
    {
        return STACKTRACE;
    }
}
