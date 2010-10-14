package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.ClassResolver;
import com.izforge.izpack.util.FileUtil;

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

    private HashMap<String, Set<URL>> classPathContentCache;


    private static final List<String> acceptedJar = Arrays.asList(".*event.*", ".*panel.*", ".*izpack.*");

    public ClassPathCrawler(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
    }


    public String getCurrentClasspath()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (URL url : getClassPathUrl())
        {
            stringBuilder.append(FileUtil.convertUrlToFilePath(url));
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
        classPathContentCache = new HashMap<String, Set<URL>>();
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

    private Set<URL> getOrCreateList(HashMap<String, Set<URL>> classPathContentCache, String key)
    {
        String newKey = key;
        if (key.contains("jar!"))
        {
            newKey = key.substring(key.indexOf("!") + 1);
        }
        if (!classPathContentCache.containsKey(newKey))
        {
            classPathContentCache.put(newKey, new HashSet<URL>());
        }
        return classPathContentCache.get(newKey);
    }

    public Class searchClassInClassPath(final String className)
    {
        if (ClassResolver.isFullClassName(className))
        {
            try
            {
                return Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                throw new MergeException("The class name " + className + " is not a valid", e);
            }
        }

        try
        {
            final String fileToSearch = className + ".class";
            processClassPath();
            Set<URL> urlList = classPathContentCache.get(fileToSearch);
            if (urlList != null)
            {
                String fullClassName = ClassResolver.processURLToClassName(urlList.iterator().next());
                return Class.forName(fullClassName);
            }
        }
        catch (ClassNotFoundException ignored)
        {
        }
        throw new MergeException("Could not find class " + className + " : Current classpath is " + getCurrentClasspath());
    }


    public Set<Class> searchAllClassesInPackage(final Package aPackage) throws ClassNotFoundException
    {
        Set<String> stringSet = classPathContentCache.keySet();
        HashSet<Class> setClasses = new HashSet<Class>();
        for (String key : stringSet)
        {
            if (key.contains(aPackage.getName()))
            {
                URL url = classPathContentCache.get(key).iterator().next();
                String className = ClassResolver.processURLToClassName(url);
                setClasses.add(Class.forName(className));
            }
        }
        return setClasses;
    }

    public Set<URL> searchPackageInClassPath(final String packageName)
    {
        processClassPath();
        String formatPackageName = packageName.replaceAll("/", ".").replaceAll("\\.$", "");
        String expectedEndPath = formatPackageName.replaceAll("\\.", "/");

        String[] parts = formatPackageName.replaceAll("/", ".").split("\\.");
        if (parts.length == 1)
        {
            return classPathContentCache.get(formatPackageName);
        }

        return getAndFilterUrlList(formatPackageName, expectedEndPath);
    }

    private Set<URL> getAndFilterUrlList(String packageName, String expectedEndPath)
    {
        Set<URL> elligibleList = getUrlsForLastPackage(packageName);
        Set<URL> result = new HashSet<URL>();
        for (URL url : elligibleList)
        {
            if (FileUtil.convertUrlToFilePath(url).endsWith(expectedEndPath))
            {
                result.add(url);
            }
        }
        return result;
    }


    public Set<URL> getUrlsForLastPackage(String packageName)
    {
        String[] packages = packageName.split("\\.");
        return classPathContentCache.get(packages[packages.length - 1]);
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
        return filterUrl(result, acceptedJar);
    }


    private Collection<URL> filterUrl(Collection<URL> urlCollection, List<String> acceptedRegexp)
    {
        HashSet<URL> result = new HashSet<URL>();
        for (URL url : urlCollection)
        {
            for (String regexp : acceptedRegexp)
            {
                if (FileUtil.convertUrlToFilePath(url).matches(regexp))
                {
                    result.add(url);
                }
            }
        }
        return result;
    }
}
