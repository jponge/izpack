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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.n3.nanoxml.XMLElement;

/**
 * Encapsulates OS constraints specified on creation time and allows
 * to check them against the current OS.
 *
 * For example, this is used for &lt;executable&gt;s to check whether
 * the executable is suitable for the current OS.
 *
 * @author     Olexij Tkatchenko <ot@parcs.de>
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
   * Constructs a new instance. Please remember, MacOSX belongs to Unix family.
   *
   * @param  family   The OS family (unix, windows or mac).
   * @param  name     The exact OS name.
   * @param  version  The exact OS version (check property <code>os.version</code> for values).
   * @param  arch     The machine architecture (check property <code>os.arch</code> for values).
   */
  public OsConstraint(String family, String name, String version, String arch)
  {
    this.family = family != null ? family.toLowerCase() : null;
    this.name = name != null ? name.toLowerCase() : null;
    this.version = version != null ? version.toLowerCase() : null;
    this.arch = arch != null ? arch.toLowerCase() : null;
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

    if (arch != null && arch.length() != 0)
    {
      match = System.getProperty("os.arch").toLowerCase().equals(arch);
    }
    if (match && version != null && version.length() != 0)
    {
      match = System.getProperty("os.version").toLowerCase().equals(version);
    }
    if (match && name != null && name.length() != 0)
    {
      match = osName.equals(name);
    }
    if (match && family != null)
    {
      if (family.equals("windows"))
      {
        match = OsVersion.IS_WINDOWS;
      }
      else if (family.equals("mac") || family.equals("osx"))
      {
        match = OsVersion.IS_OSX;
      }
      else if (family.equals("unix"))
      {
        match = OsVersion.IS_UNIX;
      }
    }

    return match && (family != null || name != null || version != null || arch != null);
  }

  /**
   * Extract a list of OS constraints from given element.
   * 
   * @param element parent XMLElement
   * @return List of OsConstraint (or empty List if no constraints found)
   */
  static public List getOsList(XMLElement element)
  {
    // get os info on this executable
    ArrayList osList = new ArrayList();
    Iterator osIterator = element.getChildrenNamed("os").iterator();
    while (osIterator.hasNext())
    {
      XMLElement os = (XMLElement) osIterator.next();
      osList.add (new OsConstraint (
          os.getAttribute("family", null),
          os.getAttribute("name", null),
          os.getAttribute("version", null),
          os.getAttribute("arch", null)
          )
        );
    }
    
    // backward compatibility: still support os attribute
    String osattr = element.getAttribute ("os");
    if (osattr != null && osattr.length() > 0)
    {
      // add the "os" attribute as a family constraint
      osList.add (new OsConstraint (osattr, null, null, null));
    }
    
    return osList;
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

  /**
   * Helper function: Check whether the given XMLElement is "suitable" for the
   * current OS.
   *
   * @param el The XMLElement to check for OS constraints.
   *
   * @return true if there were no OS constraints or the constraints matched the current OS.  
   *       
   */
  public static boolean oneMatchesCurrentSystem (XMLElement el)
  {
    return oneMatchesCurrentSystem(getOsList(el));
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
  /** 
   * This simply checks if the current OS is menber of the MS-Windows-family.
   *
   * @return true if so.
   */
  public static boolean isWindows(  )
  {
    return ( System.getProperty( "os.name" ).toLowerCase(  ).indexOf( "windows" ) > -1 );
  }

}

