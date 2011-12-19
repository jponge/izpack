package com.izforge.izpack.maven.natives;

import java.io.File;

/**
 * Factory for Microsoft Visual C++ 2010 environment variables for a x64 environment.
 * <p/>
 * This makes the following assumptions:
 * <ul>
 * <li>.NET 4.0 is being used</li>
 * <li>Windows SDK 7.1 is being used</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class MSVC2010x64EnvFactory extends AbstractMSVC2010EnvFactory
{

    /**
     * Returns the Microsoft Windows SDK dir.
     * <p/>
     * Note that if multiple versions of the Windows SDK have been installed, this may return the wrong directory.
     * <p/>
     * It will return <tt>c:\\Program Files\\Miscrosoft SDKs\\Windows\\v7.1</tt> if it exists, in preference to any
     * other.
     *
     * @return the Microsoft Windows SDK dir
     */
    @Override
    protected File getWindowsSDKDir()
    {
        File windowsSDKDir = super.getWindowsSDKDir();
        String path = windowsSDKDir.getPath();
        String x86 = getProgramFilesX86();
        if (path.startsWith(x86))
        {
            path = path.substring(x86.length());
        }
        File result = new File(getProgramFiles(), path);
        if (!result.exists())
        {
            result = windowsSDKDir;
        }
        return result;
    }

    /**
     * Returns the LIB environment variable path.
     * <p/>
     * E.g.:
     * <pre>
     * c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\ATLMFC\\lib\\amdy64;
     * c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\lib\\amd64;
     * c:\\Program Files\\Microsoft SDKs\\Windows\\7.1\\x64</pre>
     *
     * @param vcInstallDir  the Visual C++ install dir
     * @param windowsSDKDir the Windows SDK dir
     * @return the LIB environment variable path
     */
    @Override
    protected String getLibEnv(File vcInstallDir, File windowsSDKDir)
    {
        return vcInstallDir.getPath() + "\\ATLMFC\\LIB\\amd64;" + vcInstallDir.getPath() + "\\LIB\\amd64;"
                + windowsSDKDir.getPath() + "\\LIB\\x64";
    }

    /**
     * Returns the LIBPATH environment variable path.
     * <p/>
     * E.g.:
     * <pre>
     * C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319;
     * C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319;
     * C:\\Windows\Microsoft.NET\Framework64\v3.5;
     * C:\\Windows\\Microsoft.NET\\Framework\\v3.5;;
     * C:\\Program Files (x86)\\Microsoft Visual Studio 10.0\\VC\\Lib\\amd64;
     * </pre>
     *
     * @param frameworkDir      the .NET framework directory
     * @param frameworkVersions the .NET framework versions
     * @param vcInstallDir      the Visual C++ install dir
     * @param windowsSDKDir     the Windows SDK dir
     * @return the LIBPATH
     */
    @Override
    protected String getLibPathEnv(File frameworkDir, String[] frameworkVersions, File vcInstallDir, File windowsSDKDir)
    {
        StringBuilder result = new StringBuilder();
        result.append(getFrameworkPaths(frameworkDir, frameworkVersions));
        result.append(vcInstallDir.getPath()).append("\\ATLMFC\\lib\\amd64;");
        result.append(vcInstallDir.getPath()).append("\\lib\\amd64;");
        result.append(windowsSDKDir.getPath()).append("\\lib\\amd64;");
        return result.toString();
    }

    /**
     * Creates a path for .NET framework versions.
     *
     * @param frameworkDir      the base .NET Framework directory
     * @param frameworkVersions the .NET Framework versions
     * @return the paths
     */
    @Override
    protected String getFrameworkPaths(File frameworkDir, String[] frameworkVersions)
    {
        StringBuilder result = new StringBuilder();
        String framework64Dir = frameworkDir + "64";
        for (String version : frameworkVersions)
        {
            result.append(framework64Dir).append('\\').append(version).append(';');
            result.append(frameworkDir).append('\\').append(version).append(';');
        }
        return result.toString();
    }

    /**
     * Returns the Visual C++ bin dir.
     * <p/>
     * E.g.: <pre>c:\\Program Files (x86)\\Microsoft Visual Studio 10.0\\VC\\bin\\amd64</pre>
     *
     * @param vcInstallDir the Visual C++ install dir
     * @return the Visual C++ bin dir
     */
    @Override
    protected String getVCBinDir(File vcInstallDir)
    {
        return vcInstallDir.getPath() + "\\Bin\\amd64;";
    }

}
