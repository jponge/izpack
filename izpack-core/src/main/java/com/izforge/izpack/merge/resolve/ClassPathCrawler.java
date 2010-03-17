package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.ClassResolver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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

    private MergeableResolver mergeableResolver;
    private HashMap<String, List<URL>> classPathContentCache;

    public ClassPathCrawler(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
    }


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

    public void processClassPath()
    {
        if (classPathContentCache != null)
        {
            return;
        }
        classPathContentCache = new HashMap<String, List<URL>>();
        try
        {
            Collection<URL> urls = getClassPathUrl();
            for (URL url : urls)
            {
                Mergeable mergeable = mergeableResolver.getMergeableFromURL(url);
                final List<File> files = mergeable.recursivelyListFiles(new FileFilter()
                {
                    public boolean accept(File pathname)
                    {
                        return true;
                    }
                });
                if (files != null)
                {
                    for (File file : files)
                    {
                        getOrCreateList(classPathContentCache, file.getName()).add(file.toURI().toURL());
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new MergeException(e);
        }
    }

    private List<URL> getOrCreateList(HashMap<String, List<URL>> classPathContentCache, String key)
    {
        if (!classPathContentCache.containsKey(key))
        {
            classPathContentCache.put(key, new ArrayList<URL>());
        }
        return classPathContentCache.get(key);
    }

    public Class searchClassInClassPath(final String className) throws ClassNotFoundException
    {
        final String fileToSearch = className + ".class";
        processClassPath();
        List<URL> urlList = classPathContentCache.get(fileToSearch);
        if (urlList != null)
        {
            String fullClassName = ClassResolver.processURLToClassName(urlList.get(0));
            return Class.forName(fullClassName);
        }
        throw new IzPackException("Could not find class " + className + " : Current classpath is " + getCurrentClasspath());
    }


    public Collection<URL> searchPackageInClassPath(final String packageName)
    {
        processClassPath();
        return classPathContentCache.get(packageName);
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
