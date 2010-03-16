package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.ClassResolver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Crawl and store a map of all files in classpath when we can't get package directly
 *
 * @author Anthonin Bonnefoy
 */
public class ClassPathCrawler
{

    public String getCurrentClasspath()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (URL url : getClassPathUrl())
        {
            stringBuilder.append(url.getPath());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    public Class searchFullClassNameInClassPath(final String className)
    {
        final String fileToSearch = className + ".class";
        try
        {
            Collection<URL> urls = getClassPathUrl();
            for (URL url : urls)
            {
                Mergeable mergeable = null;
//                Mergeable mergeable = getMergeableFromURL(url);
                final File file = mergeable.find(new FileFilter()
                {
                    public boolean accept(File pathname)
                    {
                        return pathname.isDirectory() || pathname.getName().equals(fileToSearch);
                    }
                });
                if (file != null)
                {
                    return Class.forName(ClassResolver.processFileToClassName(file));
                }
            }
        }
        catch (Exception e)
        {
            throw new MergeException(e);
        }
        throw new IzPackException("Could not find class " + className + " : Current classpath is " + getCurrentClasspath());
    }


    public Collection<URL> searchPackageInClassPath(final String packageName)
    {
        Collection<URL> result = new HashSet<URL>();
        for (URL url : getClassPathUrl())
        {
            Mergeable mergeable = null;
//            Mergeable mergeable = getMergeableFromURL(url);
            final File file = mergeable.find(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.isDirectory() || pathname.getAbsolutePath().contains(packageName);
                }
            });
            if (file != null)
            {
                try
                {
                    result.add(file.toURI().toURL());
                }
                catch (MalformedURLException ignored)
                {
                }
            }
        }
        return result;
    }

    private Collection<URL> getClassPathUrl()
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
