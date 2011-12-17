package com.izforge.izpack.maven.natives;

import java.io.File;

public class MSVC2010x64EnvFactory extends AbstractMSVC2010EnvFactory
{

    @Override
    protected File getWindowsSDKDir()
    {
        File windowsSDKDir = super.getWindowsSDKDir();
        System.out.println("WINDOWS SDK DIR=" + windowsSDKDir);
        String path = windowsSDKDir.getPath();
        String x86 = getProgramFilesX86();
        System.out.println("X86=" + x86);
        if (path.startsWith(x86))
        {
            path = path.substring(x86.length());
        }
        File result = new File(getProgramFiles(), path);
        System.out.println("ADAPTED=" + result);
        if (!result.exists())
        {
            result = windowsSDKDir;
        }
        System.out.println("USING=" + result);

        return result;
    }

    @Override
    protected String getLibEnv(File vcInstallDir, File windowsSDKDir)
    {
        return vcInstallDir.getPath() + "\\ATLMFC\\LIB\\amd64;" + vcInstallDir.getPath() + "\\LIB\\amd64;"
                + windowsSDKDir.getPath() + "\\LIB\\x64";
    }

    @Override
    protected String getLibPathEnv(File vcInstallDir, File windowsSDKDir, File frameworkDir, String frameworkVersion)
    {
        return frameworkDir + "\\" + frameworkVersion
                + ";" + vcInstallDir.getPath() + "\\ATLMFC\\LIB\\amd64;" + vcInstallDir.getPath() + "\\LIB\\amd64;"
                + ";" + windowsSDKDir + "\\LIB\\amd64";
    }

    @Override
    protected String getVCBinDir(File vcInstallDir)
    {
        return vcInstallDir.getPath() + "\\bin\\x86_amd64;";
    }

}
