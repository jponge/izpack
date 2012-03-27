package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.merge.Mergeable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Try to resolve paths by searching inside the classpath or files with the corresponding name
 *
 * @author Anthonin Bonnefoy
 */
public class PathResolver
{
    /**
     * The mergeable resolver.
     */
    private final MergeableResolver mergeableResolver;


    /**
     * Constrcuts a <tt>PathResolver</tt>.
     *
     * @param mergeableResolver the mergeable resolver
     */
    public PathResolver(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
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
        Set<URL> result = findResources(sourcePath);
        if (result.isEmpty())
        {
            throw new IzPackException(
                    "The path '" + sourcePath + "' is not present inside the classpath.\n"
                            + "The current classpath is :" + ResolveUtils.getCurrentClasspath());
        }
        return result;
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

    public List<Mergeable> getMergeableFromPackageName(String dependPackage)
    {
        return getMergeableFromPath(dependPackage.replaceAll("\\.", "/") + "/");
    }

    public List<Mergeable> getMergeableJarFromPackageName(String packageName)
    {
        Set<URL> urlSet = ResolveUtils.getJarUrlForPackage(packageName);
        ArrayList<Mergeable> list = new ArrayList<Mergeable>();
        for (URL url : urlSet)
        {
            list.add(mergeableResolver.getMergeableFromURL(url));
        }
        return list;
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
        for (URL url : urlList)
        {
            result.add(mergeableResolver.getMergeableFromURLWithDestination(url, destination));
        }
        return result;
    }

    /**
     * Find all resources for the specified resource path.
     *
     * @param resourcePath the resource path
     * @return urls matching the resource path
     */
    protected Set<URL> findResources(String resourcePath)
    {
        Set<URL> result = new HashSet<URL>();
        URL path = ResolveUtils.getFileFromPath(resourcePath);
        if (path != null)
        {
            result.add(path);
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader instanceof URLClassLoader)
        {
            try
            {
                Enumeration<URL> iterator = ((URLClassLoader) loader).findResources(resourcePath);
                while (iterator.hasMoreElements())
                {
                    URL url = iterator.nextElement();
                    result.add(url);
                }
            }
            catch (IOException e)
            {
                throw new IzPackException(e);
            }
        }
        return result;
    }

    /**
     * Returns the mergeable resolver.
     *
     * @return the mergeable resolver
     */
    protected MergeableResolver getMergeableResolver()
    {
        return mergeableResolver;
    }


}
