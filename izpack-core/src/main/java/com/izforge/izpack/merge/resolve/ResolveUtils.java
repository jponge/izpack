package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * Helper methods for resolve
 *
 * @author Anthonin Bonnefoy
 */
public class ResolveUtils
{
    public static final String CLASSNAME_PREFIX = "com.izforge.izpack.panels";
    public static final String BASE_CLASSNAME_PATH = CLASSNAME_PREFIX.replaceAll("\\.", "/") + "/";

    /**
     * Extract file name from url and test jar file
     *
     * @param url url to test. If it is a jar content, the jar file is extracted and treated as a jar
     * @return true if the file is a jar
     */
    public static boolean isJar(URL url)
    {
        String file = FileUtil.convertUrlToFilePath(url);
        file = file.substring(file.indexOf(":") + 1);
        if (file.contains("!"))
        {
            file = file.substring(0, file.lastIndexOf('!'));
        }
        File classFile = new File(file);
        return isJar(classFile);
    }

    public static boolean isJar(File classFile)
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = new ZipFile(classFile);
            zipFile.getName();
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
        return true;
    }

    public static String getCurrentClasspath()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (URL url : getClassPathUrl())
        {
            stringBuilder.append(FileUtil.convertUrlToFilePath(url));
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    static Collection<URL> getClassPathUrl()
    {
        Collection<URL> result = new HashSet<URL>();
        java.net.URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        result.addAll(Arrays.asList(loader.getURLs()));
        try
        {
            Enumeration<URL> urlEnumeration = loader.getResources("");
            result.addAll(Collections.list(urlEnumeration));
            urlEnumeration = loader.getResources("META-INF/");
            result.addAll(Collections.list(urlEnumeration));
        }
        catch (IOException ignored)
        {
        }
        return result;
    }

    /**
     * Search if the given path exist in the classpath and return it. If nothing is found,
     * try to load it as a file and return it if exists.
     *
     * @param path The path of File to load.
     * @return The file or null if nothing found.
     */
    public static URL getFileFromPath(String path)
    {
        try
        {
            File file = new File(path);
            if (file.exists())
            {
                return file.toURI().toURL();
            }
        }
        catch (MalformedURLException e)
        {
            throw new IzPackException(e);
        }
        return null;
    }

    public static String processUrlToJarPath(URL resource)
    {
        String res = FileUtil.convertUrlToFilePath(resource);
        res = res.replaceAll("file:", "");
        if (res.contains("!"))
        {
            return res.substring(0, res.lastIndexOf("!"));
        }
        return res;
    }

    public static String processUrlToJarPackage(URL resource)
    {
        String res = FileUtil.convertUrlToFilePath(resource);
        res = res.replaceAll("file:", "");
        res = res.substring(res.lastIndexOf("!") + 1);
        res = res.replaceAll("^/", "");
        if (res.endsWith("/"))
        {
            return res;
        }
        return res + "/";
    }

    /**
     * Simply return the left side of the last .<br />
     * com.izpack.Aclass return com.izpack <br />
     * If the is no '.' in the charsequence, return the default package for panels.
     *
     * @param className className to process.
     * @return Extracted package from classname or the default package
     */
    public static String getPanelsPackagePathFromClassName(String className)
    {
        if (className.contains("."))
        {
            return className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/") + "/";
        }
        return BASE_CLASSNAME_PATH;
    }

    public static boolean isFile(URL url)
    {
        return !FileUtil.convertUrlToFile(url).isDirectory();
    }

    public static String convertPathToPosixPath(String path)
    {
        return path.replaceAll("\\\\", "/");
    }

    public static String convertPathToPosixPath(File file) {
        return convertPathToPosixPath(file.getAbsolutePath());
    }
}
