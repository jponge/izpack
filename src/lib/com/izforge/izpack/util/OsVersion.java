/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Hani Suleiman
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is a convienient class, which helps you to detect / identify the running OS/Distribution
 * 
 * Created at: Date: Nov 9, 2004 Time: 8:53:22 PM
 * 
 * @author hani, Marc.Eppelmann&#064;reddot.de
 */
public final class OsVersion
{

    //~ Static fields/initializers
    // *******************************************************************************************************************************

    /** OS_NAME = System.getProperty( "os.name" ) */
    public static final String OS_NAME = System.getProperty("os.name");

    /** True if this is FreeBSD. */
    public static final boolean IS_FREEBSD = startsWithIgnoreCase(OS_NAME, "FreeBSD");

    /** True if this is Linux. */
    public static final boolean IS_LINUX = startsWithIgnoreCase(OS_NAME, "Linux");

    /** True if this is HP-UX. */
    public static final boolean IS_HPUX = startsWithIgnoreCase(OS_NAME, "HP-UX");

    /** True if this is AIX. */
    public static final boolean IS_AIX = startsWithIgnoreCase(OS_NAME, "AIX");

    /** True if this is SunOS. */
    public static final boolean IS_SUNOS = startsWithIgnoreCase(OS_NAME, "SunOS");

    /** True if this is OS/2. */
    public static final boolean IS_OS2 = startsWith(OS_NAME, "OS/2");

    /** True if this is the Mac OS X. */
    public static final boolean IS_OSX = startsWith(OS_NAME, "Mac") && OS_NAME.endsWith("X");

    /** True if this is Windows. */
    public static final boolean IS_WINDOWS = startsWith(OS_NAME, "Windows");

    /** True if this is some variant of Unix (OSX, Linux, Solaris, FreeBSD, etc). */
    public static final boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS;

    /** True if RedHat Linux was detected */
    public static final boolean IS_REDHAT_LINUX = IS_LINUX
            && (fileContains(getReleaseFileName(), "RedHat") || fileContains(getReleaseFileName(),
                    "Red Hat"));

    /** True if RedHat Linux was detected */
    public static final boolean IS_FEDORA_LINUX = IS_LINUX
            && fileContains(getReleaseFileName(), "Fedora");

    /** True if Mandrake/Mandriva Linux was detected */
    public static final boolean IS_MANDRIVA_LINUX = IS_LINUX
            && fileContains(getReleaseFileName(), "Mandrake");

    /** True if Mandriva(Mandrake) Linux was detected */
    public static final boolean IS_MANDRAKE_LINUX = IS_MANDRIVA_LINUX;

    /** True if SuSE Linux was detected */
    public static final boolean IS_SUSE_LINUX = IS_LINUX
            && fileContains(getReleaseFileName(), "SuSE");

    /** True if Debian Linux was detected */
    public static final boolean IS_DEBIAN_LINUX = IS_LINUX
            && (fileContains("/proc/version", "Debian") || fileContains("/proc/version", "Debian"));

    //~ Methods
    // **************************************************************************************************************************************************

    /**
     * True if a given string starts with the another given String
     * 
     * @param str The String to search in
     * @param prefix The string to search for
     * 
     * @return True if str starts with prefix
     */
    private static boolean startsWith(String str, String prefix)
    {
        return (str != null) && str.startsWith(prefix);
    }

    /**
     * The same as startsWith but ignores the case.
     * 
     * @param str The String to search in
     * @param prefix The string to search for
     * 
     * @return rue if str starts with prefix
     */
    private static boolean startsWithIgnoreCase(String str, String prefix)
    {
        return (str != null) && str.toUpperCase().startsWith(prefix.toUpperCase());
    }

    /**
     * Searches case sensitively, and returns true if the given SearchString occurs in the
     * first File with the given Filename.
     * 
     * @param aFileName A files name
     * @param aSearchString the string search for
     * 
     * @return true if found in the file otherwise false
     */
    private static boolean fileContains(String aFileName, String aSearchString)
    {
        return (fileContains(aFileName, aSearchString, false));
    }

    /**
     * Tests if the given File contains the given Search String
     * 
     * @param aFileName A files name
     * @param aSearchString the String to search for
     * @param caseInSensitiveSearch If false the Search is casesensitive
     * 
     * @return true if found in the file otherwise false
     */
    private static boolean fileContains(String aFileName, String aSearchString,
            boolean caseInSensitiveSearch)
    {
        boolean result = false;
        
        String searchString = new String(caseInSensitiveSearch ? aSearchString.toLowerCase()
                : aSearchString);
        
        ArrayList etcReleaseContent = new ArrayList();

        try
        {
            etcReleaseContent = getFileContent(aFileName);
        }
        catch (IOException e)
        {
            // TODO handle Exception
            e.printStackTrace();
        }

        Iterator linesIter = etcReleaseContent.iterator();

        while (linesIter.hasNext())
        {
            String currentline = (String) linesIter.next();

            if (caseInSensitiveSearch == true)
            {
                currentline = currentline.toLowerCase();
            }

            if (currentline.indexOf(searchString) > -1)
            {
                result = true;

                break;
            }
        }

        return result;
    }

    /**
     * Gets the content from a File as StringArray List.
     * 
     * @param fileName A file to read from.
     * 
     * @return List of individual line of the specified file. List may be empty but not null.
     * 
     * @throws IOException
     */
    private static ArrayList getFileContent(String fileName) throws IOException
    {
        ArrayList result = new ArrayList();

        File releaseFile = new File(fileName);

        if (!releaseFile.isFile())
        {
            //throw new IOException( fileName + " is not a regular File" );
            return result; // None
        }

        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new FileReader(releaseFile));
        }
        catch (FileNotFoundException e1)
        {
            // TODO handle Exception
            e1.printStackTrace();

            return result;
        }

        String aLine = null;

        while ((aLine = reader.readLine()) != null)
        {
            result.add(aLine + "\n");
        }

        reader.close();

        return result;
    }

    /**
     * Gets the etc Release Filename
     * 
     * @return name of the file the release info is stored in for Linux distributions
     */
    private static String getReleaseFileName()
    {
        String result = new String();

        File[] etcList = new File("/etc").listFiles();

        for (int idx = 0; idx < etcList.length; idx++)
        {
            File etcEntry = etcList[idx];

            if (etcEntry.isFile())
            {
                if (etcEntry.getName().endsWith("-release"))
                {
                    //match :-)
                    return result = etcEntry.toString();
                }
            }
        }

        return result;
    }

    /**
     * Gets the Details of a Linux Distribution
     * 
     * @return description string of the Linux distribution
     */
    private static String getLinuxDistribution()
    {
        String result = null;

        if (IS_SUSE_LINUX)
        {
            try
            {
                result = "SuSE Linux\n" + ArrayListToString(getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_REDHAT_LINUX)
        {
            try
            {
                result = "RedHat Linux\n" + ArrayListToString(getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }

        else if (IS_FEDORA_LINUX)
        {
            try
            {
                result = "Fedora Core Linux\n"
                        + ArrayListToString(getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_MANDRAKE_LINUX)
        {
            try
            {
                result = "Mandrake Linux\n"
                        + ArrayListToString(getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_MANDRIVA_LINUX)
        {
            try
            {
                result = "Mandriva Linux\n"
                        + ArrayListToString(getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_DEBIAN_LINUX)
        {
            try
            {
                result = "Debian Linux\n"
                        + ArrayListToString(getFileContent("/etc/debian_version"));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else
        {
            try
            {
                result = "Unknown Linux Distribution\n"
                        + ArrayListToString(getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }

        return result;
    }

    /**
     * Transforms a (Array)List of Strings into a printable Stringlist
     * 
     * @param aStringList
     * 
     * @return a printable list
     */
    private static String ArrayListToString(ArrayList aStringList)
    {
        StringBuffer temp = new StringBuffer();

        for (int idx = 0; idx < aStringList.size(); idx++)
        {
            temp.append(aStringList.get(idx) + "\n");
        }

        return temp.toString();
    }

    /**
     * Testmain
     * 
     * @param args Commandline Args
     */
    public static void main(String[] args)
    {
        System.out.println("OS_NAME=" + OS_NAME);

        if (IS_UNIX)
        {
            if (IS_LINUX)
            {
                System.out.println(getLinuxDistribution());
            }
            else
            {
                try
                {
                    System.out.println(getFileContent(getReleaseFileName()));
                }
                catch (IOException e)
                {
                    // TODO handle or ignore
                }
            }
        }

        if (IS_WINDOWS)
        {
            System.out.println(System.getProperty("os.name") + " "
                    + System.getProperty("sun.os.patch.level", ""));
        }
    }
}
