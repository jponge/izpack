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

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  Encloses information about an update check.
 *
 * @author Tino Schwarze <tino.schwarze@community4you.de>
 */
public class UpdateCheck implements Serializable
{
  /** ant-fileset-like list of include patterns, based on INSTALL_PATH if relative */
  public ArrayList includesList = null;
  
  /** ant-fileset-like list of exclude patterns, based on INSTALL_PATH if relative */
  public ArrayList excludesList = null;

  /** Whether pattern matching is performed case-sensitive */
  boolean caseSensitive = true;
  
  /**  Constructs a new uninitialized instance. */
  public UpdateCheck() { }

  /**
   * Constructs and initializes a new instance.
   *
   * @param  includes The patterns to include in the check.
   * @param  excludes The patterns to exclude from the check.
   */
  public UpdateCheck(ArrayList includes, ArrayList excludes)
  {
    this.includesList = includes;
    this.excludesList = excludes;
  }

  /**
   * Constructs and initializes a new instance.
   *
   * @param  includes      The patterns to include in the check.
   * @param  excludes      The patterns to exclude from the check.
   * @param  casesensitive If "yes", matches are performed case sensitive.
   */
  public UpdateCheck(ArrayList includes, ArrayList excludes, String casesensitive)
  {
    this.includesList = includes;
    this.excludesList = excludes;
    this.caseSensitive = ((casesensitive != null) && casesensitive.equalsIgnoreCase("yes"));
  }

}

