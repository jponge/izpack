/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               IoHelper.java
 *  Description :        Helper for IO related stuff.
 *  Author's email :     bartzkau@users.berlios.de
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

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <p>Class  with some IO related helper.</p>
 *
 */
public class IoHelper
{
  private static Properties envVars = null;
	/**
	 * Default constructor
	 */
  private IoHelper()
  {
  }

  /**
   * Returns the value of the environment variable given
   * by key. This method is a work around for VM versions which
   * do not support getenv in an other way.
   * At the first call all environment variables will be loaded via
   * an exec.  
   * @param key variable name for which the value should be resolved
   * @return the value of the environment variable given
   * by key
   */
  public static String getenv(String key)
  {
    if( envVars == null)
      loadEnv();
    return(String) ( envVars.get(key));
  }
  
  /**
   * Loads all environment variables via an exec.
   */
  private static void loadEnv()
  {
    envVars = new Properties();
    String[] output = new String[2];
    String[] params = null;
    String osname = System.getProperty("os.name").toLowerCase();
    if (osname.indexOf("windows 9") > -1) 
    {
      params = new String[] { "command.com", "/c", "set" };
    }
    else
      if (osname.indexOf ("windows") > -1)
      {
        params = new String[] { "cmd.exe", "/c", "set" };
      }
      else
      {
        OsConstraint unixOs = new OsConstraint ("unix", null, null, null);
        
        if (unixOs.matchCurrentSystem())
        {
          params = new String[] { "env" };
        }
      }
    
    if (params == null)
      return;
    
    FileExecutor fe = new FileExecutor();
    fe.executeCommand(params, output);
    if( output[0].length() <= 0 )
      return;
    String lineSep = System.getProperty("line.separator");
    StringTokenizer st = new StringTokenizer(output[0], lineSep);
    String var = null;
    int index = 0;
    while(st.hasMoreTokens())
    {
      String line = st.nextToken();
      if( line.indexOf('=') == -1)
      { // May be a env var with a new line in it.
        if( var == null )
        {
          var = lineSep + line;
        }
        else
        {
          var += lineSep + line;
        }
      }
      else
      { // New var, perform the previous one.
        if( var != null )
        {
          index = var.indexOf('=');
          envVars.setProperty(var.substring(0, index), var.substring(index + 1));
        }
        var = line;
      }
    }
    if( var != null )
    { // Add last env var.
      index = var.indexOf('=');
      envVars.setProperty(var.substring(0, index), var.substring(index + 1));
    }
  }
}
