package com.izforge.izpack.ant;

/*
 * IzPackTask V 2002.2.21
 * Copyright (C) 2002 Paul Wilkinson
 *
 * File :               IzPacktask.java
 * Description :        An ant task to invoke the IZPack compiler.
 * Author's email :     paulw@wilko.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;

import com.izforge.izpack.compiler.Compiler;

public class IzPackTask extends org.apache.tools.ant.Task implements com.izforge.izpack.compiler.PackagerListener {
    
    /** Holds value of property input. */
    private String input;
    
    /** Holds value of property basedir. */
    private String basedir;
    
    /** Holds value of property output. */
    private String output;
    
    /** Holds value of property installerType. */
    private InstallerType installerType;
    
    /** Holds value of property izPackDir.
     *  This should point at the IzPack directory
     */
    private String izPackDir;
    
    /** Creates new IZPackTask */
    public IzPackTask() {
        basedir=null;
        input=null;
        output=null;
        installerType=null;
        izPackDir=null;
    }

    public void packagerMsg(java.lang.String str) {
        log(str);       // Log the message to the Ant log
    }
    
    public void packagerStart() {
        log(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("Packager_starting"),Project.MSG_DEBUG);
    }
    
    public void packagerStop() {
        log(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("Packager_ended"),Project.MSG_DEBUG);
    }
    
    public void execute() throws org.apache.tools.ant.BuildException {
        if (input==null)
        {
            throw new BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("input_must_be_specified"));
        }
        
        if (output==null)
        {
            throw new BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("output_must_be_specified"));
        }
        
        if (installerType==null)
        {
            throw new BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("installerType_must_be_specified"));
        }
        
        if (basedir == null)
        {
             throw new BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("basedir_must_be_specified"));
        }
        
        if (izPackDir == null)
        {
            throw new BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("izPackDir_must_be_specified"));
        }
        
        
        Compiler c=new Compiler(input,basedir,installerType.getValue(),output);  // Create the compiler
        Compiler.IZPACK_HOME=izPackDir;
        
        c.setPackagerListener(this);  // Listen to the compiler messages
        
        
        try
        {
                c.executeCompiler(true); //  Invoke the compiler in silent mode
        }
        catch (Exception e)
        {
                throw new org.apache.tools.ant.BuildException(e);  // Throw an exception if compilation failed
        }
    }
    
    /** Setter for property input.
     * @param input New value of property input.
     */
    public void setInput(String input) {
        this.input = input;
    }    

    /** Setter for property basedir.
     * @param basedir New value of property basedir.
     */
    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }
    
    /** Setter for property output.
     * @param output New value of property output.
     */
    public void setOutput(String output) {
        this.output = output;
    }
    
    /** Setter for property installerType.
     * @param installerType New value of property installerType.
     */
    public void setInstallerType(InstallerType installerType) {
        this.installerType = installerType;
    }
    
    /** Setter for property izPackDir.
     * @param izPackDir New value of property izPackDir.
     */
    public void setIzPackDir(String izPackDir) {
        this.izPackDir = izPackDir;
    }
    
      /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     */
    public static class InstallerType extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {Compiler.STANDARD, Compiler.STANDARD_KUNSTSTOFF, Compiler.WEB,Compiler.WEB_KUNSTSTOFF};
        }
    }
}
