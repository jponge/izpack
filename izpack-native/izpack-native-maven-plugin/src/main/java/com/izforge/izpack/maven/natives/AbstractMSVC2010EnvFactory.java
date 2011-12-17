package com.izforge.izpack.maven.natives;

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.msvc.AbstractMSVCEnvFactory;
import org.codehaus.mojo.natives.msvc.RegQuery;
import org.codehaus.mojo.natives.util.EnvUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMSVC2010EnvFactory extends AbstractMSVCEnvFactory
{
    private static final String VS100COMNTOOLS_ENV_KEY = "VS100COMNTOOLS";

    @SuppressWarnings("unchecked")
    protected Map createEnvs()
            throws NativeBuildException
    {
        Map envs = new HashMap();

        File vsCommonToolDir = getCommonToolDirectory();

        File vsInstallDir = getVisualStudioInstallDirectory(vsCommonToolDir);

        if (!vsInstallDir.isDirectory())
        {
            throw new NativeBuildException(vsInstallDir.getPath() + " is not a directory.");
        }
        envs.put("VSINSTALLDIR", vsInstallDir.getPath());

        File vcInstallDir = getVSInstallDir(vsInstallDir);
        if (!vcInstallDir.isDirectory())
        {
            throw new NativeBuildException(vcInstallDir.getPath() + " is not a directory.");
        }
        envs.put("VCINSTALLDIR", vcInstallDir.getPath());

        File frameworkDir = getFrameworkDir();
        envs.put("FrameworkDir", frameworkDir.getPath());

        File windowsSDKDir = getWindowsSDKDir();
        envs.put("WindowsSdkDir", windowsSDKDir.getPath());

        String frameworkVersion = "v4.0.50727";
        envs.put("FrameworkVersion", frameworkVersion);

        String devEnvDir = vsCommonToolDir + "\\..\\IDE";
        envs.put("DevEnvDir", devEnvDir);

        //setup new PATH
        String currentPathEnv = System.getProperty("java.library.path");

        String newPathEnv = devEnvDir + ";" + getVCBinDir(vcInstallDir) + ";" + vsCommonToolDir + ";"
                + frameworkDir + "\\" + frameworkVersion + ";"
                + vcInstallDir.getPath() + "\\VCPackages" + ";" + windowsSDKDir.getPath() + "\\bin;" + currentPathEnv;

        envs.put("PATH", newPathEnv);

        //setup new INCLUDE PATH
        String currentIncludeEnv = EnvUtil.getEnv("INCLUDE");
        String newIncludeEnv = vcInstallDir.getPath() + "\\ATLMFC\\INCLUDE;" + vcInstallDir.getPath() + "\\INCLUDE;"
                + windowsSDKDir.getPath() + "\\include;" + currentIncludeEnv;

        envs.put("INCLUDE", newIncludeEnv);

        //
        //setup new LIB PATH
        //
        String currentLibEnv = EnvUtil.getEnv("LIB");
        String newLibEnv = getLibEnv(vcInstallDir, windowsSDKDir) + ";" + currentLibEnv;
        envs.put("LIB", newLibEnv);

        String currentLibPathEnv = EnvUtil.getEnv("LIBPATH");
        String newLibPathEnv = getLibPathEnv(vcInstallDir, windowsSDKDir, frameworkDir, frameworkVersion) + ";" + currentLibPathEnv;
        envs.put("LIBPATH", newLibPathEnv);

        for (Object key : envs.keySet())
        {
            System.out.println(key + " = " + envs.get(key));
        }
        return envs;
    }

    protected File getWindowsSDKDir()
    {
        File windowsSDKDir = new File(getProgramFiles(), "\\Microsoft SDKs\\Windows\\v7.1");
        System.out.println("WINDOWS SDK 7.1=" + windowsSDKDir + ", exists=" + windowsSDKDir.exists());
        if (!windowsSDKDir.exists())
        {
            // RegQuery doesn't handle returned keys.
            String value = RegQuery.getValue("REG_SZ", "HKLM\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows",
                    "CurrentInstallFolder");
            if (value != null)
            {
                windowsSDKDir = new File(value);
            }
        }
        return windowsSDKDir;
    }

    protected File getCommonToolDirectory()
            throws NativeBuildException
    {
        String envValue = System.getenv(VS100COMNTOOLS_ENV_KEY);
        if (envValue == null)
        {
            throw new NativeBuildException("Environment variable: " + VS100COMNTOOLS_ENV_KEY + " not available.");
        }

        return new File(envValue);
    }


    protected File getVisualStudioInstallDirectory(File commonToolDir)
            throws NativeBuildException
    {
        try
        {
            return new File(commonToolDir, "../..").getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new NativeBuildException("Unable to construct Visual Studio install directory using: " + commonToolDir, e);
        }
    }

    protected File getFrameworkDir()
    {
        return new File(getSystemRoot() + "\\Microsoft.NET\\Framework");
    }

    protected File getVSInstallDir(File vsInstallDir)
    {
        return new File(vsInstallDir.getPath() + "\\VC");
    }

    protected String getVCBinDir(File vcInstallDir)
    {
        return vcInstallDir.getPath() + "\\bin";
    }

    protected String getLibEnv(File vcInstallDir, File windowsSDKDir)
    {
        return vcInstallDir.getPath() + "\\ATLMFC\\LIB;" + vcInstallDir.getPath() + "\\LIB;"
                + windowsSDKDir.getPath() + "\\LIB";
    }

    protected String getLibPathEnv(File vcInstallDir, File windowsSDKDir, File frameworkDir, String frameworkVersion)
    {
        return frameworkDir + "\\" + frameworkVersion
                + ";" + vcInstallDir.getPath() + "\\ATLMFC\\LIB;" + vcInstallDir.getPath() + "\\LIB;";
    }

}
