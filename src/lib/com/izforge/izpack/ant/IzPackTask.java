/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Paul Wilkinson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.ant;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.EnumeratedAttribute;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.PackagerListener;

/**
 * A IzPack Ant task.
 * 
 * @author Paul Wilkinson
 */
public class IzPackTask extends Task implements PackagerListener
{

    /** Holds value of property input. */
    private String input;

    /** Holds value of property basedir. */
    private String basedir;

    /** Holds value of property output. */
    private String output;

    /** Holds value of property installerType. */
    private InstallerType installerType;

    /**
     * Holds value of property izPackDir. This should point at the IzPack directory
     */
    private String izPackDir;

    /** Holds properties used to make substitutions in the install file */
    private Properties properties;

    /** should we inherit properties from the Ant file? */
    private boolean inheritAll = false;

    /** Creates new IZPackTask */
    public IzPackTask()
    {
        basedir = null;
        input = null;
        output = null;
        installerType = null;
        izPackDir = null;
    }

    /**
     * Logs a message to the Ant log at default priority (MSG_INFO).
     * 
     * @param str The message to log.
     */
    public void packagerMsg(String str)
    {
        packagerMsg(str, MSG_INFO);
    }

    /**
     * Logs a message to the Ant log at the specified priority.
     * 
     * @param str The message to log.
     * @param priority The priority of the message.
     */
    public void packagerMsg(String str, int priority)
    {
        final int antPriority;
        switch (priority)
        // No guarantee of a direct conversion. It's an enum
        {
        case MSG_DEBUG:
            antPriority = Project.MSG_DEBUG;
            break;
        case MSG_ERR:
            antPriority = Project.MSG_ERR;
            break;
        case MSG_INFO:
            antPriority = Project.MSG_INFO;
            break;
        case MSG_VERBOSE:
            antPriority = Project.MSG_VERBOSE;
            break;
        case MSG_WARN:
            antPriority = Project.MSG_WARN;
            break;
        default: // rather than die...
            antPriority = Project.MSG_INFO;
        }
        log(str, antPriority);
    }

    /** Called when the packaging starts. */
    public void packagerStart()
    {
        log(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString(
                "Packager_starting"), Project.MSG_DEBUG);
    }

    /** Called when the packaging stops. */
    public void packagerStop()
    {
        log(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString(
                "Packager_ended"), Project.MSG_DEBUG);
    }

    /**
     * Packages.
     * 
     * @exception BuildException Description of the Exception
     */
    public void execute() throws org.apache.tools.ant.BuildException
    {
        if (input == null)
            throw new BuildException(ResourceBundle.getBundle(
                    "com/izforge/izpack/ant/langpacks/messages").getString(
                    "input_must_be_specified"));

        if (output == null)
            throw new BuildException(ResourceBundle.getBundle(
                    "com/izforge/izpack/ant/langpacks/messages").getString(
                    "output_must_be_specified"));

        // if (installerType == null) now optional

        if (basedir == null)
            throw new BuildException(ResourceBundle.getBundle(
                    "com/izforge/izpack/ant/langpacks/messages").getString(
                    "basedir_must_be_specified"));

        // if (izPackDir == null)
        // throw new
        // BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("izPackDir_must_be_specified"));

        String kind = (installerType == null ? null : installerType.getValue());
        Compiler c = new Compiler(input, basedir, kind, output);// Create the
        // compiler
        Compiler.IZPACK_HOME = izPackDir;

        c.setPackagerListener(this);// Listen to the compiler messages

        if (properties != null)
        {
            Enumeration e = properties.keys();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                String value = properties.getProperty(name);
                c.addProperty(name, value);
            }
        }

        try
        {
            c.executeCompiler();
        }
        catch (Exception e)
        {
            throw new BuildException(e);// Throw an exception if compilation
            // failed
        }
    }

    /**
     * Setter for property input.
     * 
     * @param input New value of property input.
     */
    public void setInput(String input)
    {
        this.input = input;
    }

    /**
     * Setter for property basedir.
     * 
     * @param basedir New value of property basedir.
     */
    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    /**
     * Setter for property output.
     * 
     * @param output New value of property output.
     */
    public void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * Setter for property installerType.
     * 
     * @param installerType New value of property installerType.
     */
    public void setInstallerType(InstallerType installerType)
    {
        this.installerType = installerType;
    }

    /**
     * Setter for property izPackDir.
     * 
     * @param izPackDir New value of property izPackDir.
     */
    public void setIzPackDir(String izPackDir)
    {
        if (!(izPackDir.endsWith("/"))) izPackDir += "/";
        this.izPackDir = izPackDir;
    }

    /**
     * If true, pass all Ant properties to IzPack. Defaults to false;
     */
    public void setInheritAll(boolean value)
    {
        inheritAll = value;
    }

    /**
     * Ant will call this for each &lt;property&gt; tag to the IzPack task.
     */
    public void addConfiguredProperty(Property property)
    {
        if (properties == null) properties = new Properties();

        properties.setProperty(property.getName(), property.getValue());
    }

    /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     * 
     * @author Paul Wilkinson
     */
    public static class InstallerType extends EnumeratedAttribute
    {

        public String[] getValues()
        {
            return new String[] { Compiler.STANDARD, Compiler.WEB};
        }
    }
}
