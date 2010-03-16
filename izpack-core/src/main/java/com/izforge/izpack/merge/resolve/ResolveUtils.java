package com.izforge.izpack.merge.resolve;

import java.io.File;
import java.io.IOException;
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
    /**
     * Extract file name from url and test jar file
     *
     * @param url url to test. If it is a jar content, the jar file is extracted and treated as a jar
     * @return true if the file is a jar
     */
    public static boolean isJar(URL url)
    {
        String file = url.getFile();
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
            stringBuilder.append(url.getPath());
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
}
