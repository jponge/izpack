/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               Property.java
 *  Description :        Property handling at compile time.
 *  Author's email :     mchenryc@acm.org
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

package com.izforge.izpack.compiler;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;

import net.n3.nanoxml.XMLElement;

import org.apache.tools.ant.taskdefs.Execute;

import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Sets a property by name, or set of properties (from file or resource) in the
 * project. This is modeled after ant properties<p>
 *
 * Properties are immutable: once a property is set it cannot be changed. They
 * are most definately not variable.<p>
 * 
 * There are five ways to set properties:
 * <ul>
 *   <li>By supplying both the <i>name</i> and <i>value</i> attributes.</li>
 *   <li>By setting the <i>file</i> attribute with the filename of the property
 *     file to load. This property file has the format as defined by the file used
 *     in the class java.util.Properties.</li>
 *   <li>By setting the <i>environment</i> attribute with a prefix to use.
 *     Properties will be defined for every environment variable by
 *     prefixing the supplied name and a period to the name of the variable.</li>
 * </ul>
 *
 * Combinations of the above are considered an error.<p>
 *
 * The value part of the properties being set, might contain references to
 * other properties. These references are resolved when the properties are
 * set.<p>
 *
 * This also holds for properties loaded from a property file.<p>
 *
 * Properties are case sensitive.<p>
 *
 * When specifying the environment attribute, it's value is used as a prefix to
 * use when retrieving environment variables. This functionality is currently
 * only implemented on select platforms.<p>
 *
 * Thus if you specify environment=&quot;myenv&quot; you will be able to access
 * OS-specific environment variables via property names &quot;myenv.PATH&quot;
 * or &quot;myenv.TERM&quot;.<p>
 *
 * Note also that properties are case sensitive, even if the
 * environment variables on your operating system are not, e.g. it
 * will be ${env.Path} not ${env.PATH} on Windows 2000.<p>
 *
 * Note that when specifying either the <code>prefix</code> or
 * <code>environment</code> attributes, if you supply a property name with a
 * final &quot;.&quot; it will not be doubled. ie
 * environment=&quot;myenv.&quot; will still allow access of environment
 * variables through &quot;myenv.PATH&quot; and &quot;myenv.TERM&quot;.<p>
 */
public class Property
{
  protected String name;
  protected String value;
  protected File file;
  // protected String resource;
  // protected Path classpath;
  protected String env;
  // protected Reference ref;
  protected String prefix;

  protected XMLElement xmlProp;
  protected Compiler compiler;

  public Property(XMLElement xmlProp, Compiler comp)
  {
    this.xmlProp = xmlProp;
    compiler = comp;
    name = xmlProp.getAttribute("name");
    value = xmlProp.getAttribute("value");
    env = xmlProp.getAttribute("environment");
    if (env != null && !env.endsWith("."))
      env += ".";

    prefix = xmlProp.getAttribute("prefix");
    if (prefix != null && !prefix.endsWith("."))
      prefix += ".";

    String filename = xmlProp.getAttribute("file");
    if (filename != null)
      file = new File(filename);
  }

  /**
   * get the value of this property
   * @return the current value or the empty string
   */
  public String getValue() {
    return toString();
  }

  /**
   * get the value of this property
   * @return the current value or the empty string
   */
  public String toString() {
    return value == null ? "" : value;
  }

  /**
   * set the property in the project to the value.
   * if the task was give a file, resource or env attribute
   * here is where it is loaded
   */
  public void execute() throws CompilerException
  {
    if (name != null)
    {
      if (value == null)
        compiler.parseError(xmlProp, "You must specify a value with the name attribute");
    }
    else
    {
      if (file == null && env == null)
        compiler.parseError(xmlProp, "You must specify file, or environment when not using the name attribute");
    }

    if (file == null && prefix != null)
      compiler.parseError(xmlProp, "Prefix is only valid when loading from a file ");

    if ((name != null) && (value != null))
      addProperty(name, value);

    else if (file != null)
      loadFile(file);

    else if (env != null)
      loadEnvironment(env);
  }

  /**
   * load properties from a file
   * @param file file to load
   */
  protected void loadFile(File file) throws CompilerException
  {
    Properties props = new Properties();
    compiler.getPackagerListener().packagerMsg("Loading " + file.getAbsolutePath()); // VERBOSE
    try
    {
      if (file.exists())
      {
        FileInputStream fis = new FileInputStream(file);
        try
        {
          props.load(fis);
        }
        finally
        {
          if (fis != null)
            fis.close();
        }
        addProperties(props);
      }
      else
      {
        compiler.getPackagerListener().packagerMsg("Unable to find property file: " + file.getAbsolutePath()); // VERBOSE
      }
    }
    catch (IOException ex)
    {
      compiler.parseError(xmlProp, "Faild to load file: " +
                          file.getAbsolutePath(), ex);
    }
  }

  /**
   * load the environment values
   * @param prefix prefix to place before them
   */
  protected void loadEnvironment(String prefix) throws CompilerException
  {
    Properties props = new Properties();
    compiler.getPackagerListener().packagerMsg("Loading Environment " + prefix); // VERBOSE
    Vector osEnv = Execute.getProcEnvironment();
    for (Enumeration e = osEnv.elements(); e.hasMoreElements();)
    {
      String entry = (String) e.nextElement();
      int pos = entry.indexOf('=');
      if (pos == -1)
      {
        compiler.getPackagerListener().packagerMsg("Ignoring " +
                                               prefix); // WARN
      }
      else
      {
        props.put(prefix + entry.substring(0, pos),
                  entry.substring(pos + 1));
      }
    }
    addProperties(props);
  }

  /**
   * Add a name value pair to the project property set
   * @param name name of property
   * @param value value to set
   */
  protected void addProperty(String name, String value) {
    compiler.addProperty(name, value);
  }


  /**
   * iterate through a set of properties,
   * resolve them then assign them
   */
  protected void addProperties(Properties props) throws CompilerException
  {
    resolveAllProperties(props);
    Enumeration e = props.keys();
    while (e.hasMoreElements())
    {
      String name = (String) e.nextElement();
      String value = props.getProperty(name);

      value = compiler.replaceProperties(value);

      if (prefix != null)
      {
        name = prefix + name;
      }

      addProperty(name, value);
    }
  }

  /**
   * resolve properties inside a properties object
   * @param props properties to resolve
   */
  private void resolveAllProperties(Properties props) throws CompilerException {
    VariableSubstitutor subs = new VariableSubstitutor(props);
    subs.setBracesRequired(true);
    
    for (Enumeration e = props.keys(); e.hasMoreElements();)
    {
      String name = (String) e.nextElement();
      String value = props.getProperty(name);

      int mods = -1;
      do
      {
        StringReader read = new StringReader(value);
        StringWriter write = new StringWriter();

        try
        {
          mods = subs.substitute(read, write, "at");
          // TODO: check for circular references. We need to know which
          // variables were substituted to do that
          props.put(name, value);
        }
        catch (IOException ex)
        {
          compiler.parseError(xmlProp, "Faild to load file: " +
                              file.getAbsolutePath(), ex);
        }
      }
      while (mods != 0);
    }
  }
}
