/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Tino Schwarze
 *
 *  Description :        Class to encapsulate the result of compilation
 *  Author's email :     tino.schwarze@community4you.de
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

/**
 * This class describes the result of the compilation.
 *
 * This class is here because error handling is not straight-forward with
 * regard to compilation.
 *
 * The error condition consists of an error message, the full command line
 * which failed to execute plus it's stdout and stderr. The reason for this
 * class to exist is that there are three possible reactions to the error
 * (chosen by the user).
 * <ol>
 * <li>abort</li>
 * <li>ignore (continue anyway)</li>
 * <li>reconfigure</li>
 * </ol>
 *
 * @author     Tino Schwarze
 * @created    2003-27-05
 */
public class CompileResult
{
  // -------- public constants ---------------
  // arbitrary values
  public final static int SUCCESS = 42;
  public final static int FAILED = 23;

  public final static int ACTION_ABORT = 27;
  public final static int ACTION_CONTINUE = 39;
  public final static int ACTION_RECONFIGURE = 31;

  // -------- private variables ---------------
  // we're optimistic...
  private int status = SUCCESS;
  // here we're pessimistic
  private int action = ACTION_ABORT;
  /** the error message */
  private String message = null;
  /** the command line */
  private String[] cmdline = null;
  /** the stdout of the command */
  private String stdout = null;
  /** the stderr of the command */
  private String stderr = null;

  /**  constructor, create a new successful result */
  public CompileResult()
  {
    this.status = SUCCESS;
    this.action = ACTION_CONTINUE;
  }

  /**
   * creates a new CompileResult with status FAILED
   *
   * @param  message  description of the exception
   * @param  cmdline  full command line of failed command
   * @param  stdout   standard output of failed command
   * @param  stderr   standard error of failed command
   */
  public CompileResult (
    String message, String[] cmdline, String stdout, String stderr)
  {
    this.message = message;
    this.status = FAILED;
    this.cmdline = cmdline;
    this.stdout = stdout;
    this.stderr = stderr;
  }

  /* is this neccessary?
  public void setStatus (int status)
  {
    if (   (status == SUCCESS)
        || (status == FAILED) )
    {
      this.status = status;
    }
  }
  */

  public int getStatus ()
  {
    return this.status;
  }

  public void setAction (int action)
  {
    if (   (action == ACTION_ABORT)
        || (action == ACTION_CONTINUE)
        || (action == ACTION_RECONFIGURE) )
    {
      this.action = action;
    }

  }

  public int getAction ()
  {
    return this.action;
  }

  /** check for success  (convenience function) */
  public boolean isSuccess ()
  {
    return (this.status == SUCCESS);
  }

  /** check whether to abort (convenience function) */
  public boolean isAbort ()
  {
    return ((this.status == FAILED) && (this.action == ACTION_ABORT));
  }

  /** check whether to continue (convenience function) 
   *
   * @return true if status is SUCCESS or action is CONTINUE
   */
  public boolean isContinue ()
  {
    return ((this.status == SUCCESS) || (this.action == ACTION_CONTINUE));
  }

  /** check whether to reconfigure (convenience function) */
  public boolean isReconfigure ()
  {
    return ((this.status == FAILED) && (this.action == ACTION_RECONFIGURE));
  }

  /** return error message
   *
   * @return the error message describing the action that failed
   *         (might be null)
   */
  public String getMessage ()
  {
    return this.message;
  }

  /** get command line of failed command as a string
   *
   * @return command line of failed command
   */
  public String getCmdline ()
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < this.cmdline.length; ++i)
    {
      if (sb.length() > 0)
        sb.append (' ');
      sb.append (this.cmdline[i]);
    }
    return sb.toString ();
  }

  /** get command line of failed command as an array of strings
   *
   * @return command line of failed command
   */
  public String[] getCmdlineArray ()
  {
    return this.cmdline;
  }

  public String getStdout ()
  {
    return this.stdout;
  }

  public String getStderr ()
  {
    return this.stderr;
  }

}
