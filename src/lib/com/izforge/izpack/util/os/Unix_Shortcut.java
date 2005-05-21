/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Marc Eppelmann
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

/*
 * This represents a Implementation of the KDE/GNOME DesktopEntry.
 * which is standard from
 * "Desktop Entry Standard"
 *  "The format of .desktop files, supported by KDE and GNOME."
 *  http://www.freedesktop.org/standards/desktop-entry-spec/
 * 
 *  [Desktop Entry]
 //  Comment=$Comment
 //  Comment[de]=
 //  Encoding=$UTF-8
 //  Exec=$'/home/marc/CPS/tomcat/bin/catalina.sh' run
 //  GenericName=$
 //  GenericName[de]=$
 //  Icon=$inetd
 //  MimeType=$
 //  Name=$Start Tomcat
 //  Name[de]=$Start Tomcat
 //  Path=$/home/marc/CPS/tomcat/bin/
 //  ServiceTypes=$
 //  SwallowExec=$
 //  SwallowTitle=$
 //  Terminal=$true
 //  TerminalOptions=$
 //  Type=$Application
 //  X-KDE-SubstituteUID=$false
 //  X-KDE-Username=$
 *
 */
package com.izforge.izpack.util.os;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.StringTool;

/**
 * This is the Implementation of the RFC-Based Desktop-Link. Used in KDE and GNOME.
 * 
 * @author marc.eppelmann&#064;reddot.des
 */
public class Unix_Shortcut extends Shortcut implements Unix_ShortcutConstants
{

    //~ Static fields/initializers
    // *******************************************************************************************************************************

    /** version = "$Id$" */
    private static String version = "$Id$";

    /** rev = "$Revision$" */
    private static String rev = "$Revision$";

    /** DESKTOP_EXT = ".desktop" */
    private static String DESKTOP_EXT = ".desktop";

    /** template = "" */
    private static String template = "";

    /** N = "\n" */
    private final static String N = "\n";

    /** H = "#" */
    private final static String H = "#";

    /** S = " " */
    private final static String S = " ";

    /** C = Comment = H+S = "# " */
    private final static String C = H + S;
    
    /** QM = "\"" : <b>Q</b>uotation<b>M</b>ark */
    private final static String QM = "\"";

    //~ Instance fields
    // ******************************************************************************************************************************************

    /** internal String createdDirectory */
    private String createdDirectory;

    /** internal int itsShow */
    private int itsShow;

    /** internal int itsUserType */
    private int itsUserType;

    /** internal int itsType */
    private int itsType;

    /** internal int itsIconIndex */
    private int itsIconIndex;

    /** internal String itsWorkingDirectory */
    private String itsWorkingDirectory;

    /** internal String itsGroupName */
    private String itsGroupName;

    /** internal String itsTargetPath */
    private String itsTargetPath;

    /** internal String itsIconPath */
    private String itsIconPath;

    /** internal String itsDescription */
    private String itsDescription;

    /** internal String itsArguments */
    private String itsArguments;

    /** internal String itsName */
    private String itsName;

    /** internal String itsFileName */
    private String itsFileName;

    /** internal String itsApplnkFolder = "applnk" */
    private String itsApplnkFolder = "applnk";

    /** internal Properties Set */
    private Properties props;

    /** forAll = new Boolean(false): A flag to indicate that this should created for all users. */
    private Boolean forAll = new Boolean(false);

    //~ Constructors
    // *********************************************************************************************************************************************

    /**
     * Creates a new Unix_Shortcut object.
     */
    public Unix_Shortcut()
    {
        StringBuffer hlp = new StringBuffer();

        String userLanguage = System.getProperty("user.language", "en");

        hlp.append("[Desktop Entry]" + N);

        hlp.append("Comment=" + $Comment + N);
        hlp.append("Comment[" + userLanguage + "]=" + $Comment + N);
        hlp.append("Encoding=" + $Encoding + N);
        
        hlp.append("Exec="+ $E_QUOT + $Exec + $E_QUOT + S + $Arguments + N);
        hlp.append("GenericName=" + $GenericName + N);

        hlp.append("GenericName[" + userLanguage + "]=" + $GenericName + N);
        hlp.append("Icon=" + $Icon + N);
        hlp.append("MimeType=" + $MimeType + N);
        hlp.append("Name=" + $Name + N);
        hlp.append("Name[" + userLanguage + "]=" + $Name + N);

        hlp.append("Path="+ $P_QUOT + $Path + $P_QUOT + N);
        hlp.append("ServiceTypes=" + $ServiceTypes + N);
        hlp.append("SwallowExec=" + $SwallowExec + N);
        hlp.append("SwallowTitle=" + $SwallowTitle + N);
        hlp.append("Terminal=" + $Terminal + N);

        hlp.append("TerminalOptions=" + $Options_For_Terminal + N);
        hlp.append("Type=" + $Type + N);
        hlp.append("URL=" + $URL + N);
        hlp.append("X-KDE-SubstituteUID=" + $X_KDE_SubstituteUID + N);
        hlp.append("X-KDE-Username=" + $X_KDE_Username + N);
        hlp.append(N);
        hlp.append(C + "created by" + S + getClass().getName() + S + rev + N );
        hlp.append(C + version );

        template = hlp.toString();

        props = new Properties();

        initProps();
    }

    //~ Methods
    // **************************************************************************************************************************************************

    /**
     * This initialisizes all Properties Values with null.
     */
    private void initProps()
    {
        String[] propsArray = { $Comment, $$LANG_Comment, $Encoding, $Exec, $Arguments,
                $GenericName, $$LANG_GenericName, $MimeType, $Name, $$LANG_Name, $Path,
                $ServiceTypes, $SwallowExec, $SwallowTitle, $Terminal, $Options_For_Terminal,
                $Type, $X_KDE_SubstituteUID, $X_KDE_Username, $Icon, $URL, $E_QUOT, $P_QUOT };

        for (int i = 0; i < propsArray.length; i++)
        {
            props.put(propsArray[i], "");
        }
    }

    /**
     * Overridden Method
     * 
     * @see com.izforge.izpack.util.os.Shortcut#initialize(int, java.lang.String)
     */
    public void initialize(int aType, String aName) throws Exception
    {
        this.itsType = aType;
        this.itsName = aName;
        props.put($Name, aName);
    }

    /**
     * This indicates that Unix will be supported.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#supported()
     */
    public boolean supported()
    {
        return true;
    }

    /**
     * Dummy
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getDirectoryCreated()
     */
    public String getDirectoryCreated()
    {
        return this.createdDirectory; //while not stored...
    }

    /**
     * Dummy
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getFileName()
     */
    public String getFileName()
    {
        return (this.itsFileName);
    }

    /**
     * Overridden compatibility method. Returns all directories in $USER/.kde/share/applink.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getProgramGroups(int)
     */
    public Vector getProgramGroups(int userType)
    {
        Vector groups = new Vector();

        File kdeShareApplnk = getKdeShareApplnkFolder(userType);

        try
        {
            File[] listing = kdeShareApplnk.listFiles();

            for (int i = 0; i < listing.length; i++)
            {
                if (listing[i].isDirectory())
                {
                    groups.add(listing[i].getName());
                }
            }
        }
        catch (Exception e)
        {
            // ignore and return an empty vector.
        }

        return (groups);
    }

    /**
     * Gets the Programsfolder for the given User (non-Javadoc).
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getProgramsFolder(int)
     */
    public String getProgramsFolder(int current_user)
    {
        String result = new String();

        // 
        result = getKdeShareApplnkFolder(current_user).toString();

        return result;
    }

    /**
     * Gets the kde/share/applink - Folder for the given user and for the currently known and
     * supported distribution.
     * 
     * @param userType to get for.
     * 
     * @return the users or the systems kde share/applink(-redhat/-mdk)
     */
    private File getKdeShareApplnkFolder(int userType)
    {
        File kdeBase = getKdeBase(userType);

        File result = new File(kdeBase + File.separator + "share" + File.separator
                + getKdeApplinkFolderName());

        return result;
    }

    /**
     * Gets the name of the applink folder for the currently used distribution. Currently
     * "applnk-redhat for RedHat, "applnk-mdk" for Mandrake, and simply "applnk" for all others.
     * 
     * @return result
     */
    private String getKdeApplinkFolderName()
    {
        String applinkFolderName = "applnk";

        if (OsVersion.IS_REDHAT_LINUX)
        {
            applinkFolderName = "applnk-redhat";
        }

        if (OsVersion.IS_MANDRAKE_LINUX)
        {
            applinkFolderName = "applnk-mdk";
        }

        return applinkFolderName;
    }

    /**
     * Gets the KDEBasedir for the given User.
     * 
     * @param userType one of root or regular user
     * 
     * @return the basedir
     */
    private File getKdeBase(int userType)
    {
        File result = null;

        if (userType == Shortcut.ALL_USERS)
        {
            FileExecutor fe = new FileExecutor();

            String[] execOut = new String[2];

            int execResult = fe.executeCommand(new String[] { "/usr/bin/env", "kde-config",
                    "--prefix"}, execOut);

            result = new File(execOut[0].trim());
        }
        else
        {
            result = new File(System.getProperty("user.home").toString() + File.separator + ".kde");
        }
        return result;
    }

    /**
     * overridden method
     * 
     * @return true
     * 
     * @see com.izforge.izpack.util.os.Shortcut#multipleUsers()
     */
    public boolean multipleUsers()
    {
        // EVER true for UNIXes ;-)
        return (true);
    }

    /**
     * Creates and stores the shortcut-files.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#save()
     */
    public void save() throws Exception
    {
        String FS = File.separator;
        String target = null;

        String shortCutDef = this.replace();

        boolean rootUser4All = this.getUserType() == Shortcut.ALL_USERS;
        boolean create4All = this.getCreateForAll().booleanValue();
        
        if ("".equals(this.itsGroupName) && this.getLinkType() == Shortcut.DESKTOP)
        {
            target = System.getProperty("user.home") + FS + "Desktop" + FS + this.itsName
                    + DESKTOP_EXT;
            this.itsFileName = target;

            File source = writeShortCut(target, shortCutDef);

            if (rootUser4All && create4All)
            {
                File dest = null;
                File[] userHomesList = new File(FS + "home" + FS).listFiles();

                File aHomePath = null;

                if (userHomesList != null)
                {
                    for (int idx = 0; idx < userHomesList.length; idx++)
                    {
                        if (userHomesList[idx].isDirectory())
                        {

                            try
                            {
                                aHomePath = userHomesList[idx];
                                dest = new File(aHomePath.toString() + FS + "Desktop" + FS
                                        + source.getName());

                                copyTo(source, dest);
                            }
                            catch (Exception rex)
                            {
                                /* ignore */// most distros does not allow root to access any user
                                          // home (ls -la /home/user drwx------)
                                // But try it anyway...
                            }

                            try
                            {
                                String[] output = new String[2];
                                FileExecutor fe = new FileExecutor();
                                int result = fe.executeCommand(new String[] { "/bin/chown",
                                        aHomePath.getName(), aHomePath.toString()}, output);
                                if (result != 0)
                                {}
                            }
                            catch (RuntimeException rexx)
                            {}
                        }
                    }
                }
            }
        }
        else
        {
            File kdeHomeShareApplnk = getKdeShareApplnkFolder(this.getUserType());
            target = kdeHomeShareApplnk.toString() + FS + this.itsGroupName + FS + this.itsName
                    + DESKTOP_EXT;
            this.itsFileName = target;

            if (rootUser4All && !create4All) { return; }
            writeShortCut(target, shortCutDef);
        }
    }

    /**
     * Copies the inFile file to outFile using cbuff as buffer.
     * 
     * @param inFile The File to read from.
     * @param outFile The targetFile to write to.
     * 
     * @throws IOException If an IO Error occurs
     */
    public static void copyTo(File inFile, File outFile) throws IOException
    {
        char[] cbuff = new char[32768];
        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        int readedBytes = 0;
        long absWrittenBytes = 0;

        while ((readedBytes = reader.read(cbuff, 0, cbuff.length)) != -1)
        {
            writer.write(cbuff, 0, readedBytes);
            absWrittenBytes += readedBytes;
        }

        reader.close();
        writer.close();
    }

    /**
     * Writes the given Shortcutdefinition to the given Target.
     * Returns the written File.  
     * 
     * @param target
     * @param shortCutDef
     * 
     * @return the File of the written shortcut. 
     */
    private File writeShortCut(String target, String shortCutDef)
    {
        File targetPath = new File(target.toString().substring(0,
                target.toString().lastIndexOf(File.separatorChar)));

        if (!targetPath.exists())
        {
            targetPath.mkdirs();
            this.createdDirectory = targetPath.toString();
        }

        File targetFileName = new File( target );
        File backupFile = new File( targetPath + File.separator + "." + targetFileName.getName() + System.currentTimeMillis() );
        if( targetFileName.exists() )
        {
          try
          {
            // create a hidden backup.file of the existing shortcut with a timestamp name.           
            copyTo( targetFileName, backupFile  );// + System.e );
            targetFileName.delete();
          }
          catch (IOException e3)
          {
            System.out.println("cannot create backup file " + backupFile + " of " + targetFileName );//  e3.printStackTrace();
          }
        }
        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter( targetFileName );
        }
        catch (IOException e1)
        {
            System.out.println( e1.getMessage() );
        }

        try
        {
            fileWriter.write(shortCutDef);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            fileWriter.close();
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }

        return targetFileName;
    }

    /**
     * Set the Commandline Arguments
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setArguments(java.lang.String)
     */
    public void setArguments(String args)
    {
        this.itsArguments = args;
        props.put($Arguments, args);
    }

    /**
     * Sets the Description
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        this.itsDescription = description;
        props.put($Comment, description);
    }

    /**
     * Sets The Icon Path
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setIconLocation(java.lang.String, int)
     */
    public void setIconLocation(String path, int index)
    {
        this.itsIconPath = path;
        this.itsIconIndex = index;
        props.put($Icon, path);

        //
    }

    /**
     * Sets the Name of this Shortcut
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setLinkName(java.lang.String)
     */
    public void setLinkName(String aName)
    {
        this.itsName = aName;
        props.put($Name, aName);
    }

    /**
     * Sets the type of this Shortcut
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setLinkType(int)
     */
    public void setLinkType(int aType) throws IllegalArgumentException
    {
        this.itsType = aType;
    }

    /**
     * Sets the ProgramGroup
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setProgramGroup(java.lang.String)
     */
    public void setProgramGroup(String aGroupName)
    {
        this.itsGroupName = aGroupName;
    }

    /**
     * Sets the ShowMode
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setShowCommand(int)
     */
    public void setShowCommand(int show)
    {
        this.itsShow = show;
    }

    /**
     * Sets The TargetPath
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTargetPath(java.lang.String)
     */
    public void setTargetPath(String aPath)
    {
        this.itsTargetPath = aPath;
        
        StringTokenizer whiteSpaceTester = new StringTokenizer( aPath );
        
        if( whiteSpaceTester.countTokens() > 1 )
          props.put( $E_QUOT,QM );

        props.put($Exec, aPath);
    }

    /**
     * Sets the usertype.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setUserType(int)
     */
    public void setUserType(int aUserType)
    {
        this.itsUserType = aUserType;
    }

    /**
     * Sets the working-directory
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setWorkingDirectory(java.lang.String)
     */
    public void setWorkingDirectory(String aDirectory)
    {
        this.itsWorkingDirectory = aDirectory;
        
        StringTokenizer whiteSpaceTester = new StringTokenizer( aDirectory );
        
        if( whiteSpaceTester.countTokens() > 1 )
          props.put( $P_QUOT,QM );

        props.put($Path, aDirectory);
    }

    /**
     * Dumps the Name to console.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return this.itsName + N + template;
    }

    /**
     * Creates the Shortcut String which will be stored as File.
     * 
     * @return contents of the shortcut file
     */
    public String replace()
    {
        String result = template;
        Enumeration enum = props.keys();

        while (enum.hasMoreElements())
        {
            String key = (String) enum.nextElement();

            result = StringTool.replace(result, key, props.getProperty(key));
        }

        return result;
    }

    /**
     * Test Method
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        Unix_Shortcut aSample = new Unix_Shortcut();

        try
        {
            aSample.initialize(APPLICATIONS, "Start Tomcat");
        }
        catch (Exception exc)
        {
            System.err.println("Could not init Unix_Shourtcut");
        }

        aSample.replace();
        System.out.println(aSample);

        File targetFileName = new File(System.getProperty("user.home") + File.separator
                + "Start Tomcat" + DESKTOP_EXT);
        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(targetFileName);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        try
        {
            fileWriter.write(template);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            fileWriter.close();
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }
    }

    /**
     * Sets The Encoding
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setEncoding(java.lang.String)
     */
    public void setEncoding(String aEncoding)
    {
        props.put($Encoding, aEncoding);
    }

    /**
     * Sets The KDE Specific subst UID property
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
    public void setKdeSubstUID(String aKDESubstUID)
    {
        props.put($X_KDE_SubstituteUID, aKDESubstUID);
    }

    /**
     * Sets the MimeType
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setMimetype(java.lang.String)
     */
    public void setMimetype(String aMimetype)
    {
        props.put($MimeType, aMimetype);
    }

    /**
     * Sets the terminal
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTerminal(java.lang.String)
     */
    public void setTerminal(String trueFalseOrNothing)
    {
        props.put($Terminal, trueFalseOrNothing);
    }

    /**
     * Sets the terminal options
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTerminalOptions(java.lang.String)
     */
    public void setTerminalOptions(String someTerminalOptions)
    {
        props.put($Options_For_Terminal, someTerminalOptions);
    }

    /**
     * Sets the Shortcut type (one of Application, Link or Device)
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setType(java.lang.String)
     */
    public void setType(String aType)
    {
        props.put($Type, aType);
    }

    /**
     * Sets the Url for type Link. Can be also a apsolute file/path
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setURL(java.lang.String)
     */
    public void setURL(String anUrl)
    {
        props.put($URL, anUrl);
    }

    /**
     * Gets the Usertype of the Shortcut.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getUserType()
     */
    public int getUserType()
    {
        return itsUserType;
    }
}
