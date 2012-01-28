/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2008 Julien Ponge
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

package com.izforge.izpack.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for allowing the installer to re-launch itself with administrator permissions.
 * The way of achieving this greatly varies among the platforms. The JDK classes are of not help here as there
 * is no way to tell a JVM to run as a different user but to launch a new one.
 * <p>
 * TODO - this class has an implicit dependency on izpack-installer as it requires
 * <em>/com/izforge/izpack/installer/elevate.js</em> and
 * <em>/com/izforge/izpack/installer/run-with-privileges-on-osx</em>
 * </p>
 *
 * @author Julien Ponge
 */
public class PrivilegedRunner
{
    /**
     * Determines if elevation should be vetoed.
     */
    private boolean vetoed;

    /**
     * Builds a default privileged runner.
     */
    public PrivilegedRunner()
    {
        this(false);
    }

    /**
     * Builds a privileged runner with a vetoing parameter.
     *
     * @param vetoed should the elevation be vetoed?
     */
    public PrivilegedRunner(boolean vetoed)
    {
        this.vetoed = vetoed;
    }

    /**
     * Tells whether the elevation is vetoed by some of the invoker logic.
     *
     * @return <code>true</code> if the elevation is to be vetoed.
     */
    public boolean isVetoed()
    {
        return vetoed;
    }

    /**
     * Checks if the current platform is supported.
     *
     * @return <code>true</code> if the platform is supported, <code>false</code> otherwise.
     */
    public boolean isPlatformSupported()
    {
        return OsVersion.IS_MAC || OsVersion.IS_UNIX || OsVersion.IS_WINDOWS;
    }

    /**
     * Determines if elevated rights are required to install/uninstall the application.
     *
     * @return <code>true</code> if elevation is needed to have administrator permissions, <code>false</code> otherwise.
     */
    public boolean isElevationNeeded()
    {
        return isElevationNeeded(null);
    }

    /**
     * Determines if elevated rights are required to install/uninstall the application.
     *
     * @param path the installation path, or <tt>null</tt> if the installation path is unknown
     * @return <tt>true</tt> if elevation is needed to have administrator permissions, <tt>false</tt> otherwise.
     */
    public boolean isElevationNeeded(String path)
    {
        boolean result = false;
        if (!vetoed)
        {
            if (OsVersion.IS_WINDOWS)
            {
                if (path == null || path.trim().length() == 0)
                {
                    path = getProgramFiles();
                }
                result = !isPrivilegedMode() && !canWrite(path);
            }
            else
            {
                if (path != null)
                {
                    result = !canWrite(path);
                }
                else
                {
                    result = !System.getProperty("user.name").equals("root");
                }
            }
        }
        return result;
    }

    /**
     * Relaunches the installer with elevated rights.
     *
     * @return the status code returned by the launched process (by convention, 0 means a success).
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the launch was interrupted
     */
    public int relaunchWithElevatedRights() throws IOException, InterruptedException
    {
        String javaCommand = getJavaCommand();
        String installer = getInstallerJar();
        ProcessBuilder builder = new ProcessBuilder(getElevator(javaCommand, installer));
        builder.environment().put("izpack.mode", "privileged");
        return builder.start().waitFor();
    }

    private List<String> getElevator(String javaCommand, String installer) throws IOException, InterruptedException
    {
        List<String> elevator = new ArrayList<String>();

        if (OsVersion.IS_OSX)
        {
            elevator.add(extractMacElevator().getCanonicalPath());
            elevator.add(javaCommand);
            elevator.add("-jar");
            elevator.add(installer);
        }
        else if (OsVersion.IS_UNIX)
        {
            elevator.add("xterm");
            elevator.add("-title");
            elevator.add("Installer");
            elevator.add("-e");
            elevator.add("sudo");
            elevator.add(javaCommand);
            elevator.add("-jar");
            elevator.add(installer);
        }
        else if (OsVersion.IS_WINDOWS)
        {
            elevator.add("wscript");
            elevator.add(extractVistaElevator().getCanonicalPath());
            elevator.add(javaCommand);
            elevator.add("-Dizpack.mode=privileged");
            elevator.add("-jar");
            elevator.add(installer);
        }

        return elevator;
    }

    private File extractVistaElevator() throws IOException
    {
        String path = System.getProperty("java.io.tmpdir") + File.separator + "Installer.js";
        File elevator = new File(path);

        FileOutputStream out = new FileOutputStream(elevator);
        InputStream in = getClass().getResourceAsStream("/com/izforge/izpack/installer/elevate.js");
        copyStream(out, in);
        in.close();
        out.close();

        elevator.deleteOnExit();
        return elevator;
    }

    private File extractMacElevator() throws IOException, InterruptedException
    {
        String path = System.getProperty("java.io.tmpdir") + File.separator + "Installer";
        File elevator = new File(path);

        FileOutputStream out = new FileOutputStream(elevator);
        InputStream in = getClass().getResourceAsStream("/com/izforge/izpack/installer/run-with-privileges-on-osx");
        copyStream(out, in);
        in.close();
        out.close();

        makeExecutable(path);

        elevator.deleteOnExit();
        return elevator;
    }

    private void makeExecutable(String path) throws InterruptedException, IOException
    {
        new ProcessBuilder("/bin/chmod", "+x", path).start().waitFor();
    }

    private void copyStream(OutputStream out, InputStream in) throws IOException
    {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0)
        {
            out.write(buffer, 0, bytesRead);
        }
    }

    private String getInstallerJar()
    {
        try
        {
            URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            if (!"file".equals(uri.getScheme()))
            {
                throw new Exception("Unexpected scheme in JAR file URI: " + uri);
            }
            return new File(uri.getSchemeSpecificPart()).getCanonicalPath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String getJavaCommand()
    {
        return new StringBuilder(System.getProperty("java.home"))
                .append(File.separator)
                .append("bin")
                .append(File.separator)
                .append(getJavaExecutable())
                .toString();
    }

    private String getJavaExecutable()
    {
        if (OsVersion.IS_WINDOWS)
        {
            return "javaw.exe";
        }
        else
        {
            return "java";
        }
    }

    public static boolean isPrivilegedMode()
    {
        return "privileged".equals(System.getenv("izpack.mode")) || "privileged".equals(System.getProperty("izpack.mode"));
    }

    /**
     * Determines if the specified path can be written to.
     *
     * @param path the path to check
     * @return <tt>true</tt> if the path can be written to, otherwise <tt>false</tt>
     */
    private boolean canWrite(String path)
    {
        File file = new File(path);
        boolean canWrite = file.canWrite();
        if (canWrite)
        {
            // make sure that the path can actually be written to, for IZPACK-727
            try
            {
                File test = File.createTempFile(".izpackwritecheck", null, file);
                if (!test.delete())
                {
                    test.deleteOnExit();
                }
            }
            catch (IOException exception)
            {
                canWrite = false;
            }
        }
        return canWrite;
    }

    /**
     * Tries to determine the Windows Program Files directory.
     *
     * @return the Windows Program Files directory
     */
    private String getProgramFiles()
    {
        String path = System.getenv("ProgramFiles");
        if (path == null)
        {
            path = "C:\\Program Files";
        }
        return path;
    }

}
