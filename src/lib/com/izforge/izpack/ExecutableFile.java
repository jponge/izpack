/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Olexij Tkatchenko
 *
 *  File :               Pack.java
 *  Description :        Contains informations about a pack.
 *  Author's email :     ot@parcs.de
 *  Author's Website :   N/A
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
package com.izforge.izpack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  Encloses information about a executable file. This class abstracts the way
 *  to do a system dependent postprocessing of installation.
 *
 * @author     Olexij Tkatchenko <ot@parcs.de>
 */

public class ExecutableFile implements Serializable
{
  /**  when to execute this file */
  public final static int POSTINSTALL = 0;
  public final static int NEVER = 1;
  public final static int UNINSTALL = 2;

  /**  type of a file */
  public final static int BIN = 0;
  public final static int JAR = 1;

  /**  what to do if execution fails */
  public final static int ABORT = 0;
  public final static int WARN = 1;
  public final static int ASK = 2;

  /**  The file path */
  public String path;

  /**  Execution stage (NEVER, POSTINSTALL, UNINSTALL) */
  public int executionStage;

  /**  Main class of jar file */
  public String mainClass;

  /**  type (BIN|JAR) */
  public int type;

  /**  Failure handling (ABORT, WARN, ASK) */
  public int onFailure;

  /**  List of arguments */
  public List argList = null;

  /**  List of operating systems to run on */
  public List osList = null;

  /**  Delete file after executing? 
       Default is false for backward compatibility. */
  public boolean keepFile;

  /**  Constructs a new uninitialized instance.  */
  public ExecutableFile()
  {
    this.path = null;
    executionStage = NEVER;
    mainClass = null;
    type = BIN;
    onFailure = ASK;
    osList = new ArrayList();
    argList = new ArrayList();
    keepFile = false;
  }


  /**
   *  Constructs and initializes a new instance.
   *
   * @param  path            the file path
   * @param  executionStage  when to execute
   * @param  onFailure       what to do if execution fails
   * @param  osList          list of operating systems to run on
   */
  public ExecutableFile(String path, int executionStage,
                        int onFailure, java.util.List osList,
                        boolean keepFile)
  {
    this.path = path;
    this.executionStage = executionStage;
    this.onFailure = onFailure;
    this.osList = osList;
    this.keepFile = keepFile;
  }


  public ExecutableFile(String path,
                        int type,
                        String mainClass,
                        int executionStage,
                        int onFailure,
                        java.util.List argList,
                        java.util.List osList,
                        boolean keepFile)
  {
    this.path = path;
    this.mainClass = mainClass;
    this.type = type;
    this.executionStage = executionStage;
    this.onFailure = onFailure;
    this.argList = argList;
    this.osList = osList;
    this.keepFile = keepFile;
  }

  public String toString()
  {
    StringBuffer retval = new StringBuffer();
    retval.append("path = "+path);
    retval.append("\n");
    retval.append("mainClass = "+mainClass);
    retval.append("\n");
    retval.append("type = "+type);
    retval.append("\n");
    retval.append("executionStage = "+executionStage);
    retval.append("\n");
    retval.append("onFailure = "+onFailure);
    retval.append("\n");
    retval.append("argList: "+argList);
    retval.append("\n");
    if (argList != null)
    {
      for (int i = 0; i < argList.size(); i++)
      {
        retval.append("\targ: "+argList.get(i));
        retval.append("\n");
      }
    }
    retval.append("\n");
    retval.append("osList = "+osList);
    retval.append("\n");
    if (osList != null)
    {
      for (int i = 0; i < osList.size(); i++)
      {
        retval.append("\tos: "+osList.get(i));
        retval.append("\n");
      }
    }
    retval.append("keepFile = "+keepFile);
    retval.append("\n");
    return retval.toString();
  }
}

