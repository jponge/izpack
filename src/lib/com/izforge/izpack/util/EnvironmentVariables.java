/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Tino Schwarze
 *
 *  File :               EnvironmentVariables.java
 *  Description :        Wrapper to get environment variables from OS
 *  Author's email :     tino.schwarze@informatik.tu-chemnitz.de
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

import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;

import com.izforge.izpack.installer.VariableSubstitutor;

/**
 * This is a helper class used to get environment variables from the
 * operating system.
 * 
 * This class is neccessary because there is some valuable information there
 * (like JAVA_SDK_HOME, CATALINA_HOME and probably others) and Java doesn't have
 * a standard method accross all versions. (1.4 has System.getenv(String) but it's 
 * deprecated and throws an Error at least on Linux, 1.5 has System.getenv() which would
 * be useful but we want IzPack to run on a older JREs!)
 * 
 * The singleton pattern is used for this class. All the hard work is performed when the
 * instance is created.
 * 
 * Thanks to Réal Gagnon - this class was inspired by 
 * <a href="http://www.rgagnon.com/javadetails/java-0150.html">his site</a>.
 * 
 */
public class EnvironmentVariables extends Properties
{
  private static EnvironmentVariables singletonInstance = null;
    
  private EnvironmentVariables ()
  {
    super();
    
    String osname = System.getProperty("os.name").toLowerCase();
    String command = null;
    
    if (osname.indexOf("windows 9") > -1) 
    {
      command = "command.com /c set";
    }
    else
      if (osname.indexOf ("windows") > -1)
      {
        command = "cmd.exe /c set";
      }
      else
      {
        OsConstraint unixOs = new OsConstraint ("unix", null, null, null);
        
        if (unixOs.matchCurrentSystem())
        {
          command = "env";
        }
      }
    
    // don't know how to get environment
    if (command == null)
      return;
    
    try
    {
      Process p = Runtime.getRuntime().exec(command);
      // output is usually valid for properties
      this.load(p.getInputStream());
    }
    catch (IOException e)
    {
      // simply ignore exceptions - environment is emtpy in this case
    }
  }
  
  /**
   * Get the environment variables.
   * 
   * @return The environment variables.
   */
  public static synchronized EnvironmentVariables getInstance()
  {
    if (singletonInstance == null)
      singletonInstance = new EnvironmentVariables();
    
    return singletonInstance;
  }

  public static void main (String[] args)
  {
    EnvironmentVariables ev = EnvironmentVariables.getInstance();

    TreeMap map = new TreeMap();
    map.put("test", "/opt");
    VariableSubstitutor vs = new VariableSubstitutor (map);

    String toparse = "test bla ${ENV[JAVA_HOME]} xx$test/bla";
    
    String result = vs.substitute(toparse, "plain");
    
    System.out.println (result);
  }
}
