/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2002 Olexij Tkatchenko
 *
 *  File :               OsConstraint.java
 *  Description :        A constraint on the OS to perform some action on.
 *  Author's email :     ot@parcs.de
 *  Website :            http://www.izforge.com
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

import java.util.*;

/**
 * Encapsulates OS constraints specified on creation time and allows
 * to check them against the current OS.
 *
 * For example, this is used for &lt;executable&gt;s to check whether
 * the executable is suitable for the current OS.
 *
 * @author     Olexij Tkatchenko <ot@parcs.de>
 * @created    November 1, 2002
 */
public class OsConstraint implements java.io.Serializable
{
  /**  The OS family */
  private String family;
  /**  OS name from java system properties */
  private String name;
  /**  OS version from java system properties */
  private String version;
  /**  OS architecture from java system properties */
  private String arch;


  /**
   *  Constructs a new instance. Please remember, MacOSX belongs to Unix family.
   *
   * @param  family   Description of the Parameter
   * @param  name     Description of the Parameter
   * @param  version  Description of the Parameter
   * @param  arch     Description of the Parameter
   */
  public OsConstraint(String family, String name, String version, String arch)
  {
    this.family = (family != null) ? family.toLowerCase() : null;
    this.name = (name != null) ? name.toLowerCase() : null;
    this.version = (version != null) ? version.toLowerCase() : null;
    this.arch = (arch != null) ? arch.toLowerCase() : null;
  }


  /**
   *  Matches OS specification in this class against current system properties.
   *
   * @return    Description of the Return Value
   */
  public boolean matchCurrentSystem()
  {
    boolean match = true;
    String osName = System.getProperty("os.name").toLowerCase();

    if ((arch != null) && (arch.length() != 0))
    {
      match = System.getProperty("os.arch").toLowerCase().equals(arch);
    }
    if (match && (version != null) && (version.length() != 0))
    {
      match = System.getProperty("os.version").toLowerCase().equals(version);
    }
    if (match && (name != null) && (name.length() != 0))
    {
      match = osName.equals(name);
    }
    if (match && (family != null))
    {
      if (family.equals("windows"))
      {
        match = (osName.indexOf("windows") > -1);
      }
      else if (family.equals("mac"))
      {
        match = ((osName.indexOf("mac") > -1) && !(osName.endsWith("x")));
      }
      else if (family.equals("unix"))
      {
        String pathSep = System.getProperty("path.separator");
        match = (pathSep.equals(":") && (!osName.startsWith("mac") || osName.endsWith("x")));
      }
    }

    return match && ((family != null) ||
      (name != null) ||
      (version != null) ||
      (arch != null));
  }


  /**
   * Helper function: Scan a list of OsConstraints for a match.
   *
   * @param constraint_list List of OsConstraint to check
   *
   * @return true if one of the OsConstraints matched the current system or 
   *       constraint_list is null (no constraints), false if none of the OsConstraints matched
   */
  public static boolean oneMatchesCurrentSystem (List constraint_list)
  {
    if (constraint_list == null)
      return true;

    Iterator constraint_it = constraint_list.iterator ();

    // no constraints at all - matches!
    if (! constraint_it.hasNext ())
      return true;
      
    while (constraint_it.hasNext ())
    {
      OsConstraint osc = (OsConstraint)constraint_it.next();

      Debug.trace ("checking if os constraints "+osc+" match current OS");

      // check for match
      if (osc.matchCurrentSystem ())
      {
        Debug.trace ("matched current OS.");
        return true; // bail out on first match
      }

    }

    Debug.trace ("no match with current OS!");
    // no match found
    return false;
  }


  public void setFamily(String f)
  {
    family = f.toLowerCase();
  }


  public String getFamily()
  {
    return family;
  }


  public void setName(String n)
  {
    name = n.toLowerCase();
  }


  public String getName()
  {
    return name;
  }


  public void setVersion(String v)
  {
    version = v.toLowerCase();
  }


  public String getVersion()
  {
    return version;
  }


  public void setArch(String a)
  {
    arch = a.toLowerCase();
  }


  public String getArch()
  {
    return arch;
  }
  
  public String toString()
  {
	  StringBuffer retval = new StringBuffer();
	  retval.append("[Os ");
	  retval.append(" family "+family);
	  retval.append(" name "+name);
	  retval.append(" version "+version);
	  retval.append(" arch "+arch);
	  retval.append(" ]");
	  return retval.toString();
  }
}

