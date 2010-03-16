package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.ClassResolver;
import com.izforge.izpack.merge.file.FileMerge;
import com.izforge.izpack.merge.jar.JarMerge;
import com.izforge.izpack.merge.panel.PanelMerge;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Try to resolve paths by searching inside the classpath or files with the corresponding name
 *
 * @author Anthonin Bonnefoy
 */
public class PathResolver
{

    public Map<OutputStream, List<String>> mergeContent;
    private MergeableResolver mergeableResolver;

    public PathResolver(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
        mergeContent = new HashMap<OutputStream, List<String>>();
    }

    /**
     * Search for the sourcePath in classpath (inside jar or directory) or as a normal path and then return the type or File.
     * Ignore all path containing test-classes.
     *
     * @param sourcePath Source path to search
     * @return url list
     */
    public List<URL> resolvePath(String sourcePath)
    {
        List<URL> result = new ArrayList<URL>();
        URL path = ResolveUtils.getFileFromPath(sourcePath);
        if (path != null)
        {
            result.add(path);
        }
        try
        {
            java.net.URLClassLoader contextClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urlEnumeration = contextClassLoader.findResources(sourcePath);
            while (urlEnumeration.hasMoreElements())
            {
                URL url = urlEnumeration.nextElement();
                result.add(url);
            }
        }
        catch (IOException e)
        {
            throw new IzPackException(e);
        }

        if (!result.isEmpty())
        {
            return result;
        }
        throw new IzPackException("The path " + sourcePath + " is not present inside the classpath.\n The current classpath is :" + ResolveUtils.getCurrentClasspath());
    }

    public PanelMerge getPanelMerge(String className)
    {
        return new PanelMerge(className, getMergeableFromPath(ResolveUtils.getPackagePathFromClassName(className)), mergeContent);
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public List<Mergeable> getMergeableFromPath(String resourcePath)
    {
        List<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<Mergeable>();
        for (URL url : urlList)
        {
            result.add(getMergeableFromURL(url, resourcePath));
        }
        return result;
    }

    public Mergeable getMergeableFromURLWithDestination(URL url, String destination)
    {
        if (ResolveUtils.isJar(url))
        {
            return new JarMerge(ResolveUtils.processUrlToJarPath(url), ResolveUtils.processUrlToJarPackage(url), destination, mergeContent);
        }
        else
        {
            return new FileMerge(url, destination, mergeContent);
        }
    }

    public Mergeable getMergeableFromURL(URL url)
    {
        if (!ResolveUtils.isJar(url))
        {
            return new FileMerge(url, mergeContent);
        }
        return new JarMerge(url, ResolveUtils.processUrlToJarPath(url), mergeContent);
    }

    public Mergeable getMergeableFromURL(URL url, String resourcePath)
    {
        if (ResolveUtils.isJar(url))
        {
            return new JarMerge(url, ResolveUtils.processUrlToJarPath(url), mergeContent);
        }
        else
        {
            return new FileMerge(url, resourcePath, mergeContent);
        }
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @param destination  The destination of resources when merging will ocure.
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public List<Mergeable> getMergeableFromPath(String resourcePath, String destination)
    {
        List<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<Mergeable>();
//        String fileDestination = (destination + "/" + resourcePath).replaceAll("//", "/");
        for (URL url : urlList)
        {
            result.add(getMergeableFromURLWithDestination(url, destination));
        }
        return result;
    }

    public Class searchFullClassNameInClassPath(final String className)
    {
        final String fileToSearch = className + ".class";
        try
        {
            Collection<URL> urls = ResolveUtils.getClassPathUrl();
            for (URL url : urls)
            {
                Mergeable mergeable = getMergeableFromURL(url);
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
        throw new IzPackException("Could not find class " + className + " : Current classpath is " + ResolveUtils.getCurrentClasspath());
    }

}
