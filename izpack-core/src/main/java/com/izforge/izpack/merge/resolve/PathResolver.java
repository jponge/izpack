package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.panel.PanelMerge;

import java.io.IOException;
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
    public MergeableResolver mergeableResolver;
    private ClassPathCrawler classPathCrawler;
    private Properties panelDependencies;

    public PathResolver(MergeableResolver mergeableResolver, ClassPathCrawler classPathCrawler, Properties panelDependencies) throws IOException
    {
        this.mergeableResolver = mergeableResolver;
        this.classPathCrawler = classPathCrawler;
        this.panelDependencies = panelDependencies;
    }

    /**
     * Search for the sourcePath in classpath (inside jar or directory) or as a normal path and then return the type or File.
     * Ignore all path containing test-classes.
     *
     * @param sourcePath Source path to search
     * @return url list
     */
    public Set<URL> resolvePath(String sourcePath)
    {
        HashSet<URL> result = new HashSet<URL>();
        URL path = ResolveUtils.getFileFromPath(sourcePath);
        if (path != null)
        {
            result.add(path);
        }
        try
        {
            URLClassLoader contextClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
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
        // No chance with get resource, use classpath crawler from here
        Set<URL> urlList = classPathCrawler.searchPackageInClassPath(sourcePath);
        if (urlList != null)
        {
            return urlList;
        }
        throw new IzPackException("The path " + sourcePath + " is not present inside the classpath.\n The current classpath is :" + ResolveUtils.getCurrentClasspath());
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public List<Mergeable> getMergeableFromPath(String resourcePath)
    {
        Set<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<Mergeable>();
        for (URL url : urlList)
        {
            result.add(mergeableResolver.getMergeableFromURL(url, resourcePath));
        }
        return result;
    }

    public List<Mergeable> getMergeableFromPackage(String dependPackage)
    {
        return getMergeableFromPath(dependPackage.replaceAll("\\.", "/") + "/");
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
        Set<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<Mergeable>();
//        String fileDestination = (destination + "/" + resourcePath).replaceAll("//", "/");
        for (URL url : urlList)
        {
            result.add(mergeableResolver.getMergeableFromURLWithDestination(url, destination));
        }
        return result;
    }

    public PanelMerge getPanelMerge(String className)
    {
        Class aClass = classPathCrawler.searchClassInClassPath(className);
        return getPanelMerge(aClass);

    }

    public PanelMerge getPanelMerge(Class panelClass)
    {
        List<Mergeable> mergeableForClass = getMergeablePackageFromClass(panelClass);
        if (panelDependencies.containsKey(panelClass.getSimpleName()))
        {
            String dependPackage = (String) panelDependencies.get(panelClass.getSimpleName());
            mergeableForClass.addAll(getMergeableFromPackage(dependPackage));
        }
        return new PanelMerge(panelClass, mergeableForClass);
    }


    private List<Mergeable> getMergeablePackageFromClass(Class aClass)
    {
        List<Mergeable> mergeables = new ArrayList<Mergeable>();
        Package aPackage = aClass.getPackage();
        String destination = aPackage.getName().replaceAll("\\.", "/") + "/";
        String[] listPart = aPackage.getName().split("\\.");
        Set<URL> obtainPackages = classPathCrawler.searchPackageInClassPath(listPart[listPart.length - 1]);
        for (URL obtainPackage : obtainPackages)
        {
            mergeables.add(mergeableResolver.getMergeableFromURLWithDestination(obtainPackage, destination));
        }
        return mergeables;
    }
}
