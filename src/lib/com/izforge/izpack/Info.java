/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               Info.java
 *  Description :        The information class for an installation.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
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
 *  Contains some informations for an installer, as defined in the <info>
 *  section of the XML files.
 *
 * @author     Julien Ponge
 */
public class Info implements Serializable
{
  /**  The application name and version */
  private String appName = "", appVersion = "";

  /**  The application authors */
  private ArrayList authors = new ArrayList();

  /**  The application URL */
  private String appURL = "";

  /**  The required Java version (min) */
  private String javaVersion = "1.2";

  /**  The constructor, deliberatly void.  */
  public Info()
  {
  }

  /**
   *  Sets the application name.
   *
   * @param  appName  The new application name.
   */
  public void setAppName(String appName)
  {
    this.appName = appName;
  }

  /**
   *  Gets the application name.
   *
   * @return    The application name.
   */
  public String getAppName()
  {
    return appName;
  }

  /**
   *  Sets the version.
   *
   * @param  appVersion  The application version.
   */
  public void setAppVersion(String appVersion)
  {
    this.appVersion = appVersion;
  }

  /**
   *  Gets the version.
   *
   * @return    The application version.
   */
  public String getAppVersion()
  {
    return appVersion;
  }

  /**
   *  Adds an author to the authors list.
   *
   * @param  author  The author to add.
   */
  public void addAuthor(Author author)
  {
    authors.add(author);
  }

  /**
   *  Gets the authors list.
   *
   * @return    The authors list.
   */
  public ArrayList getAuthors()
  {
    return authors;
  }

  /**
   *  Sets the application URL.
   *
   * @param  appURL  The application URL.
   */
  public void setAppURL(String appURL)
  {
    this.appURL = appURL;
  }

  /**
   *  Gets the application URL.
   *
   * @return    The application URL.
   */
  public String getAppURL()
  {
    return appURL;
  }

  /**
   *  Sets the minimum Java version required.
   *
   * @param  javaVersion  The Java version.
   */
  public void setJavaVersion(String javaVersion)
  {
    this.javaVersion = javaVersion;
  }

  /**
   *  Gets the Java version required.
   *
   * @return    The Java version.
   */
  public String getJavaVersion()
  {
    return javaVersion;
  }

  /**
   *  This class represents an author.
   *
   * @author     Julien Ponge
   */
  public static class Author implements Serializable
  {
    /**  The author name */
    private String name;

    /**  The author email */
    private String email;

    /**
     *  Gets the author name.
     *
     * @return    The author name.
     */
    public String getName()
    {
      return name;
    }

    /**
     *  Gets the author email.
     *
     * @return    The author email.
     */
    public String getEmail()
    {
      return email;
    }

    /**
     *  The constructor.
     *
     * @param  name   The author name.
     * @param  email  The author email.
     */
    public Author(String name, String email)
    {
      this.name = name;
      this.email = email;
    }

    /**
     *  Gets a String representation of the author.
     *
     * @return    The String representation of the author, in the form : name
     *      <email> .
     */
    public String toString()
    {
      return name + " <" + email + ">";
    }

  }
}
