/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001 Johannes Lehtinen
 *
 *  File :               Pack.java
 *  Description :        Contains informations about a pack file.
 *  Author's email :     johannes.lehtinen@iki.fi
 *  Author's Website :   http://www.iki.fi/jle/
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;

/**
 *  Encloses information about a packed file. This class abstracts the way file
 *  data is stored to package.
 *
 * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class PackFile implements Serializable
{
  public static final int OVERRIDE_FALSE = 0;
  public static final int OVERRIDE_TRUE = 1;
  public static final int OVERRIDE_ASK_FALSE = 2;
  public static final int OVERRIDE_ASK_TRUE = 3;
  public static final int OVERRIDE_UPDATE = 4;

  /**  The full path name of the target file */
  private String targetPath = null;

  /**  The target operating system constraints of this file */
  private List osConstraints = null;

  /**  The length of the file in bytes */
  private long length = 0;

  /**  The last-modification time of the file. */
  private long mtime = -1;

  /** True if file is a directory (length should be 0 or ignored) */
  private boolean isDirectory = false;

  /**  Whether or not this file is going to override any existing ones */
  private int override = OVERRIDE_FALSE;

  public int previousPackNumber = -1;
  public long offsetInPreviousPack = -1;
  
  /**
   * Constructs and initializes from a source file.
   *
   * @param  src      file which this PackFile describes
   * @param  target   the path to install the file to
   * @param  osList   OS constraints
   * @param  override what to do when the file already exists
   * @throws FileNotFoundException if the specified file does not exist.
   */
  public PackFile(File src, String target, List osList, int override)
    throws FileNotFoundException
  {
    if (! src.exists()) // allows cleaner client co
      throw new FileNotFoundException("No such file: "+src);
    
    if ('/' != File.separatorChar)
      target = target.replace(File.separatorChar, '/');
    if (target.endsWith("/"))
      target = target.substring(0, target.length()-1);

    this.targetPath = target;
    this.osConstraints = osList;
    this.override = override;

    this.length = src.length();
    this.mtime = src.lastModified();
    this.isDirectory = src.isDirectory();
  }

  public void setPreviousPackFileRef(int previousPackNumber,
                                     long offsetInPreviousPack)
  {
    this.previousPackNumber = previousPackNumber;
    this.offsetInPreviousPack = offsetInPreviousPack;
  }

  /**  The target operating system constraints of this file */
  public final List osConstraints()
  {
    return osConstraints;
  }

  /**  The length of the file in bytes */
  public final long length()
  {
    return length;
  }

  /**  The last-modification time of the file. */
  public final long lastModified()
  {
    return mtime;
  }

  /**  Whether or not this file is going to override any existing ones */
  public final int override()
  {
    return override;
  }

  public final boolean isDirectory()
  {
    return isDirectory;
  }

  public final boolean isBackReference()
  {
    return (previousPackNumber >= 0);
  }

  /**  The full path name of the target file, using '/' as fileseparator. */
  public final String getTargetPath()
  {
    return targetPath;
  }
}
