/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2002 Olexij Tkatchenko
 *
 *  File :               Os.java
 *  Description :        Os class.
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

/**
 *  Performs matching of OS specified on construction time against execution
 *  platform.
 *
 * @author     Olexij Tkatchenko <ot@parcs.de>
 * @created    November 1, 2002
 */
public class Os implements java.io.Serializable
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
  public Os(String family, String name, String version, String arch)
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

